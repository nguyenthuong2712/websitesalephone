export interface ChatRoomDto {
  id: string;
  customerId: string;
  customerName: string;
  customerAvatar?: string;
  adminId?: string;
  adminName?: string;
  status: string;
  unreadCount: number;
  lastMessage?: ChatMessageDto;
  createdAt: string;
}

export interface ChatMessageDto {
  id?: string;
  roomId: string;
  senderId: string;
  senderRole: string; // 'CUSTOMER' | 'ADMIN' | 'STAFF'
  content: string;
  status: string; // 'SENT' | 'DELIVERED' | 'READ'
  deliveredAt?: string;
  readAt?: string;
  createdAt?: string;
}
