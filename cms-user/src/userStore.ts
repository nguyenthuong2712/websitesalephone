import { defineStore } from 'pinia'
import api from './api/api'

type UserProfile = Record<string, unknown> | null

export const useUserStore = defineStore('userStore', {
    state: () => ({
        user: null as UserProfile,
        ROOT_API: `${import.meta.env.VITE_ROOT_API}/api/user/`,
    }),

    actions: {
        async getUserByLoginId() {
            const res = await api.get(`${this.ROOT_API}get/user-detail`)
            this.user = res.data.data
            return res
        },
        async updateUser(profile: Record<string, unknown>) {
            const res = await api.put(`${this.ROOT_API}update-profile-user`, profile)
            this.user = {
                ...(this.user ?? {}),
                ...profile,
            }
            return res
        },
    },
})
