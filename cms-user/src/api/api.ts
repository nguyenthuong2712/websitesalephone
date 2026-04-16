import axios from 'axios';
import { authService } from "../service/AuthService.ts";

const api = axios.create({
    baseURL: import.meta.env.VITE_ROOT_API + '/api',
});

// ADD TOKEN VÀO REQUEST
api.interceptors.request.use(
    config => {
        const token = authService.getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    error => Promise.reject(error)
);

api.interceptors.response.use(
    response => response,

    error => {
        if (error.response?.status === 401) {
            authService.removeTokenAndRole();

            if (router.currentRoute.value.path !== '/login') {
                router.push('/login');
            }
        }

        return Promise.reject(error);
    }
);

export default api;
