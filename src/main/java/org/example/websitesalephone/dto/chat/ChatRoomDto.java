package org.example.websitesalephone.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    private String id;
    private String customerId;
    private String customerName;
    private String customerAvatar;
    private String adminId;
    private String adminName;
    private String status;
    private long unreadCount;
    private ChatMessageDto lastMessage;
    private OffsetDateTime createdAt;
}
