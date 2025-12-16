export interface IRegisterRequest {
    fullName: string;
    username: string;
    password: string;
    email: string;
}

export class RegisterRequest implements IRegisterRequest {
    fullName: string;
    username: string;
    password: string;
    email: string;

    constructor(fullName: string, username: string, password: string, email: string) {
        this.fullName = fullName.trim();
        this.username = username.trim();
        this.password = password.trim();
        this.email = email.trim();
    }

    toPayload(): IRegisterRequest {
        return {
            fullName: this.fullName,
            username: this.username,
            password: this.password,
            email: this.email,
        };
    }
}
