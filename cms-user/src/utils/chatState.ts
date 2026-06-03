import { ref } from 'vue'

export const isChatOpen = ref(false)
export const chatUnreadCount = ref(0)

export const toggleChat = () => {
  isChatOpen.value = !isChatOpen.value
}
