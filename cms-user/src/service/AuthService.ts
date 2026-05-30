import type { AxiosResponse } from 'axios'
import api from '../api/api'
import type { AuthUser } from '../models/AuthUser'
import { RegisterRequest } from '../models/RegisterRequest'
import { ResetPasswordRequest, type IResetPasswordRequest } from '../models/ResetPasswordRequest'

class AuthService {
    private readonly ROOT_API = `${import.meta.env.VITE_ROOT_API}/api/auth/`

    private readonly tokenKey = 'Authorization'
    private readonly roleKey = 'USER-ROLE'

    public isAuthenticated(): boolean {
        const token = this.getToken()
        return token !== null
    }

    public getRole(): string | null {
        const role = localStorage.getItem(this.roleKey)
        return role && role !== 'null' && role !== '' ? role : null
    }

    public getToken(): string | null {
        const token = localStorage.getItem(this.tokenKey)
        return token && token !== 'null' && token !== '' ? token : null
    }

    public login(request: AuthUser): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}login`, request.toAuthPayload())
    }

    public saveToken(token: string): void {
        localStorage.setItem(this.tokenKey, token)
    }

    public saveRole(role: string): void {
        localStorage.setItem(this.roleKey, role)
    }

    public removeTokenAndRole(): void {
        localStorage.removeItem(this.tokenKey)
        localStorage.removeItem(this.roleKey)
    }

    public logout(token?: string): Promise<AxiosResponse> {
        const authToken = token ?? this.getToken() ?? ''
        this.removeTokenAndRole()
        return api.post(`${this.ROOT_API}logout`, null, { params: { token: authToken } })
    }

    public forgotPassword(email: string, tabletOrPc: string): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}forgot-password`, null, {
            params: { email, tabletOrPc },
        })
    }

    public resetPassword(request: ResetPasswordRequest | IResetPasswordRequest): Promise<AxiosResponse> {
        const payload = request instanceof ResetPasswordRequest ? request.toPayload() : request
        return api.post(`${this.ROOT_API}reset-password`, payload)
    }

    public checkResetToken(token: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}check-reset-token`, { params: { token } })
    }

    public register(request: RegisterRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}register`, request.toPayload())
    }
}

export const authService = new AuthService()
