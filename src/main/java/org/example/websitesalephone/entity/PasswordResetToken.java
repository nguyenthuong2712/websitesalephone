package org.example.websitesalephone.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.websitesalephone.enums.RsTokenStatus;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@Table(name = "password_reset_token")
public class PasswordResetToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "token", nullable = false)
    private String token;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private RsTokenStatus status;

    @Column(name = "expire_time")
    private OffsetDateTime expireTime;
}
