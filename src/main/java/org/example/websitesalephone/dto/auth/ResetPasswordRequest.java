package org.example.websitesalephone.dto.auth;

import lombok.Data;

@Data
public class ResetPasswordRequest {

    /**
     * The email of the user whose password is to be reset.
     */
    private String userId;

    /**
     * The current password of the user.
     */
    private String oldPassword;

    /**
     * The new password to be set for the user.
     */
    private String newPassword;

    /**
     * The confirmation of the new password.
     */
    private String newPasswordConfirm;

    /**
     * The reset token generated for the user's password reset process.
     */
    private String resetToken;
}

