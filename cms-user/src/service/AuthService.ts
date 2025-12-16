import api from '../api/api.ts';
import type {apiResponse} from "api";
import {ResetPasswordRequest, IResetPasswordRequest} from '../models/ResetPasswordRequest';

class AuthService {
    private ROOT_API = import.meta.env.VITE_ROOT_API + '/api/auth/';

    private tokenKey: string = 'Authorization';
    private roleKey: string = 'USER-ROLE';

    public isAuthenticated(): boolean {
        const t = localStorage.getItem(this.tokenKey);
        return t !== null && t !== undefined && t !== '' && t !== 'null';
    }

    public getRole(): string | null {
        const role = localStorage.getItem(this.roleKey);
        if (role && role !== 'null' && role !== '') {
            return role;
        }
        return null;
    }

    public getToken(): string | null {
        const token = localStorage.getItem(this.tokenKey);
        if (token && token !== 'null' && token !== '') {
            return token;
        }
        return null;
    }

    public async login(request: any): Promise<apiResponse> {
        return api.post(`${this.ROOT_API}login`, request);
    }

    public saveToken(token: string) {
        localStorage.setItem(this.tokenKey, token);
        console.log(token)
    }

    public saveRole(role: string) {
        localStorage.setItem(this.roleKey, role);
    }

    public removeTokenAndRole() {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.roleKey);
    }


    // Logout
    public logout(token: string): Promise<apiResponse> {
        this.removeTokenAndRole()
        return api.post(`${this.ROOT_API}logout`, null, {params: {token}});
    }

    // Quên mật khẩu
    public forgotPassword(email: string, tabletOrPc: string): Promise<apiResponse> {
        return api.post(`${this.ROOT_API}forgot-password`, null, {
            params: {email, tabletOrPc}
        });
    }

    // Reset mật khẩu
    public resetPassword(request: ResetPasswordRequest | IResetPasswordRequest): Promise<apiResponse> {
        const payload = request instanceof ResetPasswordRequest ? request.toPayload() : request;
        return api.post(`${this.ROOT_API}reset-password`, payload);
    }

    // Kiểm tra token reset mật khẩu
    public checkResetToken(token: string): Promise<apiResponse> {
        return api.get(`${this.ROOT_API}check-reset-token`, {params: {token}});
    }

    public register(request: RegisterRequest): Promise<apiResponse> {
        return api.post(`${this.ROOT_API}register`, request.toPayload());
    }
}

export const authService = new AuthService();
