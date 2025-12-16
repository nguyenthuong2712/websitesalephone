export interface IResetPasswordRequest {
    userId: string;
    oldPassword: string;
    newPassword: string;
    newPasswordConfirm: string;
    resetToken?: string; // có thể optional nếu không dùng
}

export class ResetPasswordRequest {
    public userId: string;
    public oldPassword: string;
    public newPassword: string;
    public newPasswordConfirm: string;
    public resetToken?: string;

    constructor(
        userId: string,
        oldPassword: string,
        newPassword: string,
        newPasswordConfirm: string,
        resetToken?: string
    ) {
        this.userId = userId.trim();
        this.oldPassword = oldPassword.trim();
        this.newPassword = newPassword.trim();
        this.newPasswordConfirm = newPasswordConfirm.trim();
        this.resetToken = resetToken?.trim();
    }

    // Chuyển sang payload gửi lên API
    toPayload(): IResetPasswordRequest {
        return {
            userId: this.userId,
            oldPassword: this.oldPassword,
            newPassword: this.newPassword,
            newPasswordConfirm: this.newPasswordConfirm,
            resetToken: this.resetToken,
        };
    }

    // Kiểm tra password mới và confirm có khớp không
    isPasswordMatch(): boolean {
        return this.newPassword === this.newPasswordConfirm;
    }
}
