package org.example.websitesalephone.service.chat.impl;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.chat.ChatMessageDto;
import org.example.websitesalephone.dto.chat.ChatRoomDto;
import org.example.websitesalephone.entity.ChatMessage;
import org.example.websitesalephone.entity.ChatRoom;
import org.example.websitesalephone.entity.User;
import org.example.websitesalephone.enums.MessageStatus;
import org.example.websitesalephone.repository.ChatMessageRepository;
import org.example.websitesalephone.repository.ChatRoomRepository;
import org.example.websitesalephone.repository.UserRepository;
import org.example.websitesalephone.service.chat.ChatService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public CommonResponse getOrCreateRoom(String customerId) {
        User customer = userRepository.findByIdAndIsDeleted(customerId, false)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Optional<ChatRoom> existingRoom = chatRoomRepository.findByCustomerIdAndStatus(customerId, "ACTIVE");
        ChatRoom room;

        if (existingRoom.isPresent()) {
            room = existingRoom.get();
        } else {
            room = new ChatRoom();
            room.setId(UUID.randomUUID().toString());
            room.setCustomer(customer);
            room.setStatus("ACTIVE");
            room = chatRoomRepository.save(room);
        }

        ChatRoomDto dto = convertToRoomDto(room, customerId);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(dto)
                .message("Room loaded or created successfully")
                .build();
    }

    @Override
    @Transactional
    public CommonResponse sendMessage(String roomId, String senderId, String senderRole, String content) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        ChatMessage message = new ChatMessage();
        message.setId(UUID.randomUUID().toString());
        message.setRoom(room);
        message.setSenderId(senderId);
        message.setSenderRole(senderRole);
        message.setContent(content);
        message.setStatus(MessageStatus.SENT);
        message = chatMessageRepository.save(message);

        ChatMessageDto dto = convertToMessageDto(message);

        // Notify room via WebSocket (1. Specific room queue)
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, dto);

        // 2. Notify all admins of a new message or activity
        messagingTemplate.convertAndSend("/topic/chat/admins", dto);

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(dto)
                .message("Message sent successfully")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponse getMessagesByRoom(String roomId, String userId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        List<ChatMessageDto> dtos = messages.stream()
                .map(this::convertToMessageDto)
                .toList();

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(dtos)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CommonResponse getActiveRooms(String userId) {
        List<ChatRoom> rooms = chatRoomRepository.findAllActiveRooms();
        List<ChatRoomDto> dtos = new ArrayList<>();

        for (ChatRoom room : rooms) {
            dtos.add(convertToRoomDto(room, userId));
        }

        // Sort by last message time descending
        dtos.sort((a, b) -> {
            if (a.getLastMessage() == null && b.getLastMessage() == null) {
                return b.getCreatedAt().compareTo(a.getCreatedAt());
            }
            if (a.getLastMessage() == null) return 1;
            if (b.getLastMessage() == null) return -1;
            return b.getLastMessage().getCreatedAt().compareTo(a.getLastMessage().getCreatedAt());
        });

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .data(dtos)
                .build();
    }

    @Override
    @Transactional
    public CommonResponse markAsRead(String roomId, String userId) {
        OffsetDateTime now = OffsetDateTime.now();
        chatMessageRepository.markMessagesAsRead(roomId, userId, now);

        // Broad-cast message status updates to room (READ)
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId + "/status",
            ChatMessageDto.builder()
                .roomId(roomId)
                .status(MessageStatus.READ.name())
                .readAt(now)
                .build()
        );

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .message("Messages marked as read")
                .build();
    }

    @Override
    @Transactional
    public CommonResponse markAsDelivered(String roomId, String userId) {
        OffsetDateTime now = OffsetDateTime.now();
        chatMessageRepository.markMessagesAsDelivered(roomId, userId, now);

        // Broad-cast message status updates to room (DELIVERED)
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId + "/status",
            ChatMessageDto.builder()
                .roomId(roomId)
                .status(MessageStatus.DELIVERED.name())
                .deliveredAt(now)
                .build()
        );

        return CommonResponse.builder()
                .code(CommonResponse.CODE_SUCCESS)
                .message("Messages marked as delivered")
                .build();
    }

    private ChatRoomDto convertToRoomDto(ChatRoom room, String currentUserId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(room.getId());
        ChatMessageDto lastMsg = null;
        if (!messages.isEmpty()) {
            lastMsg = convertToMessageDto(messages.get(messages.size() - 1));
        }

        long unread = chatMessageRepository.countUnreadMessages(room.getId(), currentUserId);

        return ChatRoomDto.builder()
                .id(room.getId())
                .customerId(room.getCustomer().getId())
                .customerName(room.getCustomer().getFullName() != null ? room.getCustomer().getFullName() : room.getCustomer().getUsername())
                .customerAvatar(room.getCustomer().getAvatar())
                .adminId(room.getAdmin() != null ? room.getAdmin().getId() : null)
                .adminName(room.getAdmin() != null ? (room.getAdmin().getFullName() != null ? room.getAdmin().getFullName() : room.getAdmin().getUsername()) : null)
                .status(room.getStatus())
                .unreadCount(unread)
                .lastMessage(lastMsg)
                .createdAt(room.getCreatedAt())
                .build();
    }

    private ChatMessageDto convertToMessageDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .roomId(message.getRoom().getId())
                .senderId(message.getSenderId())
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .status(message.getStatus() != null ? message.getStatus().name() : MessageStatus.SENT.name())
                .deliveredAt(message.getDeliveredAt())
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
