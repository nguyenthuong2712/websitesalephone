import type { AxiosResponse } from 'axios'
import api from '../api/api'

class PaymentService {
    private readonly ROOT_API = `${import.meta.env.VITE_ROOT_API}/api/payment/`

    public createPayment(orderId: string): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}create?orderId=${orderId}`)
    }
}

export const paymentService = new PaymentService()
