package org.example.websitesalephone.dto.auth;

import lombok.Builder;
import lombok.Data;
import org.example.websitesalephone.entity.PasswordResetToken;
import org.example.websitesalephone.enums.RsTokenStatus;

import java.time.OffsetDateTime;

@Data
@Builder
public class ResetPasswordTokenDto {

    /** id. */
    private Long id;

    /** userId. */
    private String userId;

    /** createdTime. */
    private OffsetDateTime createdTime;

    /** expTime. */
    private OffsetDateTime expTime;

    /** status. */
    private RsTokenStatus status;

    /** token. */
    private String token;

    /** updatedTime. */
    private OffsetDateTime updatedTime;

    private String userLoginId;

    public static ResetPasswordTokenDto fromEntity(PasswordResetToken entity) {
        return ResetPasswordTokenDto
                .builder()
                .id(entity.getId())
                .status(entity.getStatus())
                .userId(entity.getUserId())
                .expTime(entity.getExpireTime())
                .token(entity.getToken())
                .build();
    }

}
