package org.example.websitesalephone.controller;

import lombok.RequiredArgsConstructor;
import org.example.websitesalephone.auth.UserDetail;
import org.example.websitesalephone.comon.CommonResponse;
import org.example.websitesalephone.dto.chat.ChatMessageDto;
import org.example.websitesalephone.service.chat.ChatService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/room/get-or-create")
    public CommonResponse getOrCreateRoom() {
        UserDetail currentUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.getOrCreateRoom(currentUser.getUserId());
    }

    @GetMapping("/room/{roomId}/messages")
    public CommonResponse getMessages(@PathVariable String roomId) {
        UserDetail currentUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.getMessagesByRoom(roomId, currentUser.getUserId());
    }

    @GetMapping("/admin/rooms")
    public CommonResponse getActiveRooms() {
        UserDetail currentUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.getActiveRooms(currentUser.getUserId());
    }

    @PostMapping("/room/{roomId}/read")
    public CommonResponse markAsRead(@PathVariable String roomId) {
        UserDetail currentUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.markAsRead(roomId, currentUser.getUserId());
    }

    @PostMapping("/room/{roomId}/delivered")
    public CommonResponse markAsDelivered(@PathVariable String roomId) {
        UserDetail currentUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.markAsDelivered(roomId, currentUser.getUserId());
    }

    @PostMapping("/room/{roomId}/send")
    public CommonResponse sendMessageRest(@PathVariable String roomId, @RequestBody ChatMessageDto messageDto) {
        UserDetail currentUser = (UserDetail) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return chatService.sendMessage(roomId, currentUser.getUserId(), currentUser.getRole(), messageDto.getContent());
    }

    // WebSocket direct message support
    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendMessageWebSocket(@DestinationVariable String roomId, @Payload ChatMessageDto messageDto) {
        chatService.sendMessage(roomId, messageDto.getSenderId(), messageDto.getSenderRole(), messageDto.getContent());
    }
}
