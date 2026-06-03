package org.example.websitesalephone.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.websitesalephone.enums.MessageStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat_messages")
public class ChatMessage extends BaseEntity {

    @Id
    @Column(length = 50)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @Column(name = "sender_id", length = 50, nullable = false)
    private String senderId;

    @Column(name = "sender_role", length = 50, nullable = false)
    private String senderRole; // CUSTOMER, ADMIN, STAFF

    @Column(columnDefinition = "NVARCHAR(MAX) COLLATE Vietnamese_CI_AS", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = true)
    private MessageStatus status = MessageStatus.SENT;

    @Column(name = "delivered_at")
    private OffsetDateTime deliveredAt;

    @Column(name = "read_at")
    private OffsetDateTime readAt;
}
