package org.example.websitesalephone.repository;

import org.example.websitesalephone.entity.ChatMessage;
import org.example.websitesalephone.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(String roomId);

    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.room.id = :roomId AND m.senderId != :userId AND m.status != 'READ'")
    long countUnreadMessages(@Param("roomId") String roomId, @Param("userId") String userId);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = 'READ', m.readAt = :now WHERE m.room.id = :roomId AND m.senderId != :userId AND m.status != 'READ'")
    void markMessagesAsRead(@Param("roomId") String roomId, @Param("userId") String userId, @Param("now") OffsetDateTime now);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = 'DELIVERED', m.deliveredAt = :now WHERE m.room.id = :roomId AND m.senderId != :userId AND m.status = 'SENT'")
    void markMessagesAsDelivered(@Param("roomId") String roomId, @Param("userId") String userId, @Param("now") OffsetDateTime now);

    List<ChatMessage> findByRoomIdAndSenderIdNotAndStatus(String roomId, String senderId, MessageStatus status);
}
