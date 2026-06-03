import type { AxiosResponse } from 'axios'
import api from '../api/api'
import type { ChatMessageDto } from '../models/ChatModels'

class ChatService {
  private readonly ROOT_API = `${import.meta.env.VITE_ROOT_API}/api/chat`

  public getOrCreateRoom(): Promise<AxiosResponse> {
    return api.get(`${this.ROOT_API}/room/get-or-create`)
  }

  public getMessages(roomId: string): Promise<AxiosResponse> {
    return api.get(`${this.ROOT_API}/room/${roomId}/messages`)
  }

  public getAdminRooms(): Promise<AxiosResponse> {
    return api.get(`${this.ROOT_API}/admin/rooms`)
  }

  public markAsRead(roomId: string): Promise<AxiosResponse> {
    return api.post(`${this.ROOT_API}/room/${roomId}/read`)
  }

  public markAsDelivered(roomId: string): Promise<AxiosResponse> {
    return api.post(`${this.ROOT_API}/room/${roomId}/delivered`)
  }

  public sendMessageRest(roomId: string, message: Partial<ChatMessageDto>): Promise<AxiosResponse> {
    return api.post(`${this.ROOT_API}/room/${roomId}/send`, message)
  }
}

export const chatService = new ChatService()
