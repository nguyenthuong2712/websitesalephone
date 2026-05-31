package org.example.websitesalephone.service.chat;

import org.example.websitesalephone.comon.CommonResponse;

public interface ChatService {
    CommonResponse getOrCreateRoom(String customerId);
    CommonResponse sendMessage(String roomId, String senderId, String senderRole, String content);
    CommonResponse getMessagesByRoom(String roomId, String userId);
    CommonResponse getActiveRooms(String userId);
    CommonResponse markAsRead(String roomId, String userId);
    CommonResponse markAsDelivered(String roomId, String userId);
}
