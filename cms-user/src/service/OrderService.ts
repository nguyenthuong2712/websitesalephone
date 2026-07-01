import type { AxiosResponse } from 'axios'
import type { Search } from '../models/Search'
import type { OrderRequest } from '../models/OrderRequest'
import type { OrderByUserRequest } from '../models/OrderByUserRequest'
import type { OrderCountRequest } from '../models/OrderCountRequest'
import api from '../api/api'

class OrderService {
    private readonly ROOT_API = `${import.meta.env.VITE_ROOT_API}/api/order/`

    public search(orderSearch: Search): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}search`, orderSearch.toPayload())
    }

    public detail(id: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}detail/${id}`)
    }

    public update(orderRequest: OrderRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update`, orderRequest)
    }

    public getListHistory(id: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}history/${id}`)
    }

    public getListOrderByUser(orderByUserRequest: OrderByUserRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}order-by-user`, orderByUserRequest)
    }

    public countOrderByUser(orderCountRequest: OrderCountRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}count-order-user`, orderCountRequest)
    }

    public countOrderByStaff(orderCountRequest: OrderCountRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}count-order-staff`, orderCountRequest)
    }

    public getDashboard(searchText: string, range: string = 'ALL'): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}dashboard/${searchText}?range=${range}`)
    }

    public downloadPdf(id: string): Promise<AxiosResponse<Blob>> {
        return api.get(`${this.ROOT_API}pdf/generate/${id}`, {
            responseType: 'blob',
        })
    }

    public buyNow(buyNowRequest: {
        variantId: string
        quantity: number
        addressLine: string
        paymentMethod: string
    }): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}buy-now`, buyNowRequest)
    }
}

export const orderService = new OrderService()
