export interface IAuthUser {
    loginId: string;
    password: string;
}

export class AuthUser {
    public loginId: string;
    public password: string;

    constructor(loginId: string, password: string) {
        this.loginId = loginId.trim();
        this.password = password.trim();
    }

    getPassword(): string | null {
        return this.password || null;
    }

    getPasswordLogin(): string | null {
        return this.password || null;
    }

    toAuthPayload(): IAuthUser {
        return {
            loginId: this.loginId,
            password: this.password,
        };
    }
}
