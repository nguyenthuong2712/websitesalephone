export class LoginDetail {
    public email?: string;
    public isLogin?: boolean;
    public isLoginSuccess?: boolean;

    constructor(init?: LoginDetail) {
        Object.assign(this, init);
    }
}
