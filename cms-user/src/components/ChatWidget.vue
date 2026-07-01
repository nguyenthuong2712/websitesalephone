<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { authService } from '../service/AuthService'
import { chatService } from '../service/ChatService'
import type { ChatRoomDto, ChatMessageDto } from '../models/ChatModels'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'
import { isChatOpen, chatUnreadCount, toggleChat } from '@/utils/chatState'

const getUserIdFromToken = (): string => {
  const token = authService.getToken()
  if (!token) return ''
  try {
    const parts = token.split('.')
    if (parts.length < 2) return ''
    const base64Url = parts[1]
    if (!base64Url) return ''
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
        .join('')
    )
    const payload = JSON.parse(jsonPayload)
    return payload.id || ''
  } catch (e) {
    console.error('Failed to parse token', e)
    return ''
  }
}

const room = ref<ChatRoomDto | null>(null)
const messages = ref<ChatMessageDto[]>([])
const newMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)
const isAiTyping = ref(false)

let stompClient: Stomp.Client | null = null
let socketConn: any = null
let reconnectTimeout: any = null


// Khi khách hàng mở widget chat lên, tự động cập nhật hiển thị tin nhắn admin gửi sang trạng thái READ
const markCurrentMessagesAsReadLocally = () => {
  const currentUserId = getUserIdFromToken()
  messages.value.forEach((m) => {
    if (m.senderId !== currentUserId) {
      m.status = 'READ'
    }
  })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

const initChatRoom = async () => {
  if (!authService.isAuthenticated() || authService.getRole() !== 'CUSTOMER') return

  try {
    const res = await chatService.getOrCreateRoom()
    room.value = res.data.data

    if (room.value) {
      await loadMessages(room.value.id)
      connectWebSocket(room.value.id)
    }
  } catch (error) {
    console.error('Failed to init chat room', error)
  }
}

const loadMessages = async (roomId: string) => {
  try {
    const res = await chatService.getMessages(roomId)
    messages.value = res.data.data
    scrollToBottom()
    await chatService.markAsRead(roomId)
    markCurrentMessagesAsReadLocally()
  } catch (e) {
    console.error('Failed to load messages', e)
  }
}

const connectWebSocket = (roomId: string) => {
  if (stompClient?.connected) return

  const socketUrl = `${import.meta.env.VITE_ROOT_API}/ws`
  socketConn = new SockJS(socketUrl)
  stompClient = Stomp.over(socketConn)
  stompClient.debug = console.log // Enable logging for debugging

  stompClient.connect(
    {},
    () => {
      if (reconnectTimeout) {
        clearTimeout(reconnectTimeout)
        reconnectTimeout = null
      }

      // Notify backend we are online so it can trigger DELIVERED on any sent message from others
      chatService.markAsDelivered(roomId)

      // Subscribe to messages
      stompClient?.subscribe(`/topic/chat/room/${roomId}`, (messageOutput) => {
        const msg: ChatMessageDto = JSON.parse(messageOutput.body)

        // Turn off AI typing bubble if message received from STAFF / AI
        if (msg.senderRole === 'STAFF') {
          isAiTyping.value = false
        }

        // Avoid duplicate messages
        const exists = messages.value.some(m => m.id === msg.id)
        if (!exists) {
          messages.value.push(msg)
        } else {
          // Update status if it exists
          const idx = messages.value.findIndex(m => m.id === msg.id)
          if (idx !== -1) {
            messages.value[idx] = msg
          }
        }

        const currentUserId = getUserIdFromToken()
        if (msg.senderId !== currentUserId) {
          if (!isChatOpen.value) {
            chatUnreadCount.value++
            // Notification
            if (Notification.permission === 'granted') {
              new Notification('Tin nhắn mới từ cửa hàng', {
                body: msg.content,
              })
            }
          } else {
            chatService.markAsRead(roomId)
          }
        }
        scrollToBottom()
      })

      // Subscribe to status updates (SENT -> DELIVERED -> READ)
      stompClient?.subscribe(`/topic/chat/room/${roomId}/status`, (statusOutput) => {
        const update: Partial<ChatMessageDto> = JSON.parse(statusOutput.body)
        const currentUserId = getUserIdFromToken()

        // Update status of outgoing messages from Customer
        messages.value.forEach((m) => {
          if (m.senderId === currentUserId) {
            if (update.status === 'READ') {
              m.status = 'READ'
              m.readAt = update.readAt
            } else if (update.status === 'DELIVERED' && m.status === 'SENT') {
              m.status = 'DELIVERED'
              m.deliveredAt = update.deliveredAt
            }
          }
        })
      })
    },
    (err) => {
      console.error('WebSocket connection error:', err)
      reconnectTimeout = setTimeout(() => {
        connectWebSocket(roomId)
      }, 5000)
    }
  )
}

const send = async () => {
  if (!newMessage.value.trim() || !room.value) return

  const currentUserId = getUserIdFromToken()
  const payload: Partial<ChatMessageDto> = {
    roomId: room.value.id,
    senderId: currentUserId,
    senderRole: 'CUSTOMER',
    content: newMessage.value.trim(),
  }

  isAiTyping.value = true // Hiển thị trạng thái "AI đang gõ..."
  scrollToBottom()

  // Safety fallback: Tự động tắt typing bubble sau 15 giây nếu không nhận được phản hồi
  setTimeout(() => {
    isAiTyping.value = false
  }, 15000)

  try {
    await chatService.sendMessageRest(room.value.id, payload)
    newMessage.value = ''
  } catch (error) {
    console.error('Failed to send message', error)
    isAiTyping.value = false
  }
}

const formatTime = (timeStr?: string) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

const getStatusLabel = (msg: ChatMessageDto) => {
  if (msg.status === 'READ') return 'Đã xem'
  if (msg.status === 'DELIVERED') return 'Đã nhận'
  return 'Đã gửi'
}

onMounted(() => {
  if (authService.isAuthenticated() && authService.getRole() === 'CUSTOMER') {
    if (Notification.permission === 'default') {
      Notification.requestPermission()
    }
    chatService.getOrCreateRoom().then((res) => {
      const activeRoom = res.data.data
      if (activeRoom) {
        connectWebSocket(activeRoom.id)
      }
    }).catch(() => {})
  }
})

onUnmounted(() => {
  if (reconnectTimeout) clearTimeout(reconnectTimeout)
  if (stompClient?.connected) {
    stompClient.disconnect(() => {})
  }
})

watch(isChatOpen, (newVal) => {
  if (newVal) {
    chatUnreadCount.value = 0
    initChatRoom()
    setTimeout(scrollToBottom, 100)
  }
})
</script>

<template>
  <div v-if="authService.isAuthenticated() && authService.getRole() === 'CUSTOMER'" class="chat-widget-wrapper">
    <!-- Chat Box -->
    <div v-if="isChatOpen" class="chat-box animate-fade-in">
      <!-- Chat Header -->
      <div class="chat-header">
        <div class="store-info">
          <div class="avatar-holder">🛒</div>
          <div>
            <div class="store-name">Tư Vấn Khách Hàng</div>
            <div class="store-status"><span class="status-dot"></span> Trực tuyến</div>
          </div>
        </div>
        <button class="close-btn" @click="toggleChat">&times;</button>
      </div>

      <!-- Chat Messages -->
      <div class="chat-messages" ref="messagesContainer">
        <div v-if="messages.length === 0" class="empty-state">
          <div class="empty-icon">👋</div>
          <div class="empty-text">Chào bạn! Cửa hàng có thể giúp gì cho bạn hôm nay?</div>
        </div>
        <div
          v-for="msg in messages"
          :key="msg.id"
          class="message-wrapper"
          :class="msg.senderRole === 'CUSTOMER' ? 'outgoing' : 'incoming'"
        >
          <div class="message-bubble">
            {{ msg.content }}
          </div>
          <span class="message-time">
            {{ formatTime(msg.createdAt) }}
            <span v-if="msg.senderRole === 'CUSTOMER'" class="read-status">
              • {{ getStatusLabel(msg) }}
            </span>
          </span>
        </div>

        <!-- AI Typing Bubble Indicator -->
        <div v-if="isAiTyping" class="message-wrapper incoming ai-typing">
          <div class="message-bubble typing-bubble">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
          <span class="message-time">AI đang tư vấn...</span>
        </div>
      </div>

      <!-- Chat Input -->
      <form @submit.prevent="send" class="chat-input-area">
        <input
          v-model="newMessage"
          type="text"
          placeholder="Nhập tin nhắn..."
          class="chat-input"
        />
        <button type="submit" class="send-btn" :disabled="!newMessage.trim()">
          ✈️
        </button>
      </form>
    </div>
  </div>
</template>

<style scoped>
.chat-widget-wrapper {
  position: fixed;
  bottom: 30px;
  right: 30px;
  z-index: 10000;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  pointer-events: none;
}

.chat-box {
  position: absolute;
  bottom: 80px;
  right: 0;
  width: 380px;
  height: 500px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  border: 1px solid rgba(0, 0, 0, 0.05);
  pointer-events: auto;
}

@media (max-width: 480px) {
  .chat-box {
    position: fixed;
    bottom: 0;
    right: 0;
    width: 100%;
    height: 100%;
    border-radius: 0;
  }
}

.chat-header {
  background: #007aff;
  color: white;
  padding: 15px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.store-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-holder {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.store-name {
  font-weight: 600;
  font-size: 15px;
}

.store-status {
  font-size: 11px;
  opacity: 0.9;
  display: flex;
  align-items: center;
  gap: 4px;
}

.status-dot {
  width: 8px;
  height: 8px;
  background: #34c759;
  border-radius: 50%;
  display: inline-block;
}

.close-btn {
  background: transparent;
  border: none;
  color: white;
  font-size: 24px;
  cursor: pointer;
  opacity: 0.8;
}

.close-btn:hover {
  opacity: 1;
}

.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
  color: #8e8e93;
  padding: 40px;
}

.empty-icon {
  font-size: 40px;
  margin-bottom: 10px;
}

.empty-text {
  font-size: 14px;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 75%;
}

.message-wrapper.outgoing {
  align-self: flex-end;
  align-items: flex-end;
}

.message-wrapper.incoming {
  align-self: flex-start;
  align-items: flex-start;
}

.message-bubble {
  padding: 10px 16px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.4;
  word-break: break-word;
}

.outgoing .message-bubble {
  background: #007aff;
  color: white;
  border-bottom-right-radius: 4px;
}

.incoming .message-bubble {
  background: white;
  color: #1c1c1e;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

/* Typing Bubble styles */
.typing-bubble {
  display: flex;
  align-items: center;
  gap: 4px;
  min-height: 36px;
  background: #e9ecef !important;
}

.typing-bubble .dot {
  width: 6px;
  height: 6px;
  background: #8e8e93;
  border-radius: 50%;
  animation: typingDot 1.4s infinite both;
}

.typing-bubble .dot:nth-child(2) {
  animation-delay: .2s;
}

.typing-bubble .dot:nth-child(3) {
  animation-delay: .4s;
}

@keyframes typingDot {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.message-time {
  font-size: 10px;
  color: #8e8e93;
  margin-top: 4px;
}

.read-status {
  font-weight: 500;
}

.chat-input-area {
  padding: 12px 15px;
  background: white;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  gap: 10px;
}

.chat-input {
  flex: 1;
  border: 1px solid #e5e5ea;
  padding: 10px 16px;
  border-radius: 20px;
  outline: none;
  font-size: 14px;
  background: #f8f9fa;
  transition: all 0.2s;
}

.chat-input:focus {
  background: white;
  border-color: #007aff;
}

.send-btn {
  background: #007aff;
  color: white;
  border: none;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  transition: all 0.2s;
}

.send-btn:hover {
  transform: scale(1.05);
  background: #0056b3;
}

.send-btn:disabled {
  background: #e5e5ea;
  cursor: not-allowed;
  transform: none;
}

.animate-fade-in {
  animation: fadeIn 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(15px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(0, 122, 255, 0.7);
  }
  70% {
    box-shadow: 0 0 0 12px rgba(0, 122, 255, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(0, 122, 255, 0);
  }
}
</style>
