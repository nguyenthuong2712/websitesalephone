import type { AxiosResponse } from 'axios'
import api from '../api/api'
import type { ICartRequest } from '../models/CartRequest'
import type { ICheckOutRequest } from '../models/CheckOutRequest'

class CartService {
    private readonly ROOT_API = `${import.meta.env.VITE_ROOT_API}/api/cart/`

    public addToCart(request: ICartRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}add`, request)
    }

    public updateCartItem(request: ICartRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update`, request)
    }

    public getCartItems(search: Record<string, unknown>): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}items`, search)
    }

    public checkoutCart(checkOutRequest: ICheckOutRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}checkout`, checkOutRequest)
    }

    public buyNow(buyNowCartRequest: {
        variantId: string
        quantity: number
        addressLine: string
        paymentMethod: string
    }): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}buy-now`, buyNowCartRequest)
    }
}

export const cartService = new CartService()
