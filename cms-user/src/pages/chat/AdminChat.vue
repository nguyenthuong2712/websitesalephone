<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { authService } from '../../service/AuthService'
import { chatService } from '../../service/ChatService'
import type { ChatRoomDto, ChatMessageDto } from '../../models/ChatModels'
import SockJS from 'sockjs-client'
import Stomp from 'stompjs'

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

const currentAdminId = getUserIdFromToken()
const rooms = ref<ChatRoomDto[]>([])
const selectedRoom = ref<ChatRoomDto | null>(null)
const messages = ref<ChatMessageDto[]>([])
const newMessage = ref('')
const messagesContainer = ref<HTMLElement | null>(null)

let stompClient: Stomp.Client | null = null
let socketConn: any = null
let reconnectTimeout: any = null

const loadRooms = async () => {
  try {
    const res = await chatService.getAdminRooms()
    rooms.value = res.data.data
  } catch (e) {
    console.error('Failed to load admin rooms', e)
  }
}

const selectRoom = async (room: ChatRoomDto) => {
  selectedRoom.value = room
  await loadMessages(room.id)
  await chatService.markAsRead(room.id)
  // Update unread locally
  room.unreadCount = 0

  // Trực tiếp cập nhật hiển thị tin nhắn của Admin gửi sang trạng thái READ
  messages.value.forEach((m) => {
    if (m.senderId !== currentAdminId) {
      m.status = 'READ'
    }
  })

  setTimeout(scrollToBottom, 100)
}

const loadMessages = async (roomId: string) => {
  try {
    const res = await chatService.getMessages(roomId)
    messages.value = res.data.data
  } catch (e) {
    console.error('Failed to load messages', e)
  }
}

const connectWebSocket = () => {
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

      // Mark all messages as delivered for active rooms
      rooms.value.forEach((r) => {
        chatService.markAsDelivered(r.id)
      })

      // Subscribe to status updates (SENT -> DELIVERED -> READ)
      rooms.value.forEach((r) => {
        stompClient?.subscribe(`/topic/chat/room/${r.id}/status`, (statusOutput) => {
          const update: Partial<ChatMessageDto> = JSON.parse(statusOutput.body)
          if (selectedRoom.value?.id === r.id) {
            messages.value.forEach((m) => {
              if (m.senderId === currentAdminId) {
                if (update.status === 'READ') {
                  m.status = 'READ'
                  m.readAt = update.readAt
                } else if (update.status === 'DELIVERED' && m.status === 'SENT') {
                  m.status = 'DELIVERED'
                  m.deliveredAt = update.deliveredAt
                }
              }
            })
          }
        })
      })

      // Subscribe to all chat rooms for admins
      stompClient?.subscribe('/topic/chat/admins', (messageOutput) => {
        const msg: ChatMessageDto = JSON.parse(messageOutput.body)

        // Find existing room
        const roomIdx = rooms.value.findIndex((r) => r.id === msg.roomId)
        if (roomIdx !== -1) {
          const room = rooms.value[roomIdx]
          if (room) {
            room.lastMessage = msg

            if (selectedRoom.value?.id === msg.roomId) {
              const exists = messages.value.some(m => m.id === msg.id)
              if (!exists) {
                messages.value.push(msg)
              }
              chatService.markAsRead(msg.roomId)
              scrollToBottom()
            } else {
              room.unreadCount++
              // Since admin is online and received this, automatically mark it as DELIVERED
              chatService.markAsDelivered(msg.roomId)
            }
            // Move room to top
            rooms.value.splice(roomIdx, 1)
            rooms.value.unshift(room)
          }
        } else {
          // Refresh list if new room
          loadRooms().then(() => {
            // Subscribe to status updates for new room
            const newRoom = rooms.value.find(r => r.id === msg.roomId)
            if (newRoom) {
              stompClient?.subscribe(`/topic/chat/room/${newRoom.id}/status`, (statusOutput) => {
                const update: Partial<ChatMessageDto> = JSON.parse(statusOutput.body)
                if (selectedRoom.value?.id === newRoom.id) {
                  messages.value.forEach((m) => {
                    if (m.senderId === currentAdminId) {
                      if (update.status === 'READ') {
                        m.status = 'READ'
                        m.readAt = update.readAt
                      } else if (update.status === 'DELIVERED' && m.status === 'SENT') {
                        m.status = 'DELIVERED'
                        m.deliveredAt = update.deliveredAt
                      }
                    }
                  })
                }
              })
            }
          })
        }
      })
    },
    (err) => {
      console.error('Admin WebSocket connection error:', err)
      reconnectTimeout = setTimeout(connectWebSocket, 5000)
    }
  )
}

const send = async () => {
  if (!newMessage.value.trim() || !selectedRoom.value) return

  const payload: Partial<ChatMessageDto> = {
    roomId: selectedRoom.value.id,
    senderId: currentAdminId,
    senderRole: authService.getRole() || 'ADMIN',
    content: newMessage.value.trim(),
  }

  try {
    await chatService.sendMessageRest(selectedRoom.value.id, payload)
    newMessage.value = ''
  } catch (error) {
    console.error('Failed to send message', error)
  }
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
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
  loadRooms()
  connectWebSocket()
})

onUnmounted(() => {
  if (reconnectTimeout) clearTimeout(reconnectTimeout)
  if (stompClient?.connected) {
    stompClient.disconnect(() => {})
  }
})
</script>

<template>
  <div class="chat-admin-container">
    <div class="chat-sidebar">
      <div class="sidebar-header">
        <h2>Hội thoại tư vấn</h2>
        <span class="active-badge">{{ rooms.length }} Khách hàng</span>
      </div>
      <div class="rooms-list">
        <div v-if="rooms.length === 0" class="no-conversations">
          Không có cuộc hội thoại nào.
        </div>
        <div
          v-for="room in rooms"
          :key="room.id"
          class="room-item"
          :class="{ active: selectedRoom?.id === room.id, 'has-unread': room.unreadCount > 0 }"
          @click="selectRoom(room)"
        >
          <div class="room-avatar">
            {{ room.customerName.charAt(0).toUpperCase() }}
          </div>
          <div class="room-details">
            <div class="room-meta">
              <span class="customer-name">{{ room.customerName }}</span>
              <span v-if="room.lastMessage" class="last-message-time">
                {{ formatTime(room.lastMessage.createdAt) }}
              </span>
            </div>
            <div class="room-last-msg">
              <span class="msg-content">{{ room.lastMessage?.content || 'Chưa có tin nhắn' }}</span>
              <span v-if="room.unreadCount > 0" class="unread-count-badge">{{ room.unreadCount }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-main">
      <div v-if="selectedRoom" class="chat-area">
        <div class="chat-main-header">
          <div class="customer-profile">
            <div class="customer-avatar">
              {{ selectedRoom.customerName.charAt(0).toUpperCase() }}
            </div>
            <div>
              <h3>{{ selectedRoom.customerName }}</h3>
              <p class="status">Đang chat trực tuyến</p>
            </div>
          </div>
        </div>

        <div class="chat-main-messages" ref="messagesContainer">
          <div
            v-for="msg in messages"
            :key="msg.id"
            class="message-wrapper"
            :class="msg.senderRole === 'CUSTOMER' ? 'incoming' : 'outgoing'"
          >
            <div class="message-bubble">
              {{ msg.content }}
            </div>
            <span class="message-time">
              {{ formatTime(msg.createdAt) }}
              <span v-if="msg.senderRole !== 'CUSTOMER'" class="read-status">
                • {{ getStatusLabel(msg) }}
              </span>
            </span>
          </div>
        </div>

        <form @submit.prevent="send" class="chat-main-input">
          <input
            v-model="newMessage"
            type="text"
            placeholder="Nhập tin nhắn trả lời..."
            class="reply-input"
          />
          <button type="submit" class="reply-send-btn" :disabled="!newMessage.trim()">
            Gửi
          </button>
        </form>
      </div>

      <div v-else class="chat-empty-state">
        <div class="empty-icon">💬</div>
        <h3>Chọn một cuộc hội thoại</h3>
        <p>Chọn khách hàng từ danh sách bên trái để bắt đầu tư vấn trực tuyến.</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-admin-container {
  display: flex;
  height: calc(100vh - 150px);
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.chat-sidebar {
  width: 320px;
  border-right: 1px solid #edf2f7;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid #edf2f7;
}

.sidebar-header h2 {
  font-size: 18px;
  font-weight: 700;
  margin-bottom: 5px;
}

.active-badge {
  font-size: 12px;
  background: #ebf8ff;
  color: #2b6cb0;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
}

.rooms-list {
  flex: 1;
  overflow-y: auto;
}

.no-conversations {
  padding: 30px;
  text-align: center;
  color: #a0aec0;
  font-size: 14px;
}

.room-item {
  display: flex;
  padding: 15px 20px;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.2s;
  border-bottom: 1px solid #f7fafc;
}

.room-item:hover {
  background: #f7fafc;
}

.room-item.active {
  background: #ebf8ff;
}

.room-avatar {
  width: 44px;
  height: 44px;
  background: #cbd5e0;
  color: #4a5568;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 16px;
}

.room-item.active .room-avatar {
  background: #3182ce;
  color: white;
}

.room-details {
  flex: 1;
  min-width: 0;
}

.room-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.customer-name {
  font-weight: 600;
  color: #2d3748;
  font-size: 14px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 170px;
}

.last-message-time {
  font-size: 11px;
  color: #a0aec0;
}

.room-last-msg {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.msg-content {
  font-size: 13px;
  color: #718096;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 170px;
}

.room-item.has-unread .msg-content {
  font-weight: 700;
  color: #2d3748;
}

.unread-count-badge {
  background: #e53e3e;
  color: white;
  font-size: 10px;
  font-weight: 700;
  border-radius: 10px;
  padding: 2px 6px;
}

.chat-main {
  flex: 1;
  display: flex;
  background: #f7fafc;
}

.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.chat-main-header {
  background: white;
  padding: 15px 30px;
  border-bottom: 1px solid #edf2f7;
}

.customer-profile {
  display: flex;
  align-items: center;
  gap: 15px;
}

.customer-avatar {
  width: 48px;
  height: 48px;
  background: #3182ce;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 18px;
}

.customer-profile h3 {
  font-size: 16px;
  font-weight: 700;
}

.customer-profile .status {
  font-size: 12px;
  color: #48bb78;
}

.chat-main-messages {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  max-width: 60%;
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
  padding: 12px 18px;
  border-radius: 18px;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
}

.outgoing .message-bubble {
  background: #3182ce;
  color: white;
  border-bottom-right-radius: 4px;
}

.incoming .message-bubble {
  background: white;
  color: #2d3748;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.message-time {
  font-size: 10px;
  color: #a0aec0;
  margin-top: 4px;
}

.chat-main-input {
  background: white;
  padding: 20px 30px;
  border-top: 1px solid #edf2f7;
  display: flex;
  gap: 15px;
}

.reply-input {
  flex: 1;
  border: 1px solid #e2e8f0;
  padding: 12px 20px;
  border-radius: 24px;
  outline: none;
  font-size: 14px;
  background: #f7fafc;
}

.reply-input:focus {
  background: white;
  border-color: #3182ce;
}

.reply-send-btn {
  background: #3182ce;
  color: white;
  border: none;
  padding: 0 25px;
  border-radius: 24px;
  font-weight: 600;
  cursor: pointer;
}

.reply-send-btn:hover {
  background: #2b6cb0;
}

.reply-send-btn:disabled {
  background: #cbd5e0;
  cursor: not-allowed;
}

.chat-empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #718096;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 15px;
}

.chat-empty-state h3 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #2d3748;
}
</style>
