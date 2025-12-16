import axios, { AxiosResponse } from 'axios';
import type {Search} from "../models/Search.ts";
import type {OrderRequest} from "../models/OrderRequest.ts";
import api from "../api/api.ts";
import type {OrderByUserRequest} from "../models/OrderByUserRequest.ts";
import type {OrderCountRequest} from "../models/OrderCountRequest.ts";

class OrderService {
    private ROOT_API = import.meta.env.VITE_ROOT_API + '/api/order/';

    public search(orderSearch: Search): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}search`, orderSearch);
    }

    public detail(id: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}detail/${id}`);
    }

    public update(orderRequest: OrderRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update`, orderRequest);
    }

    public getListHistory(id: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}history/${id}`);
    }

    public getListOrderByUser(orderByUserRequest: OrderByUserRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}order-by-user`, orderByUserRequest);
    }

    public countOrderByUser(orderCountRequest: OrderCountRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}count-order-user`, orderCountRequest);
    }

    public countOrderByStaff(orderCountRequest: OrderCountRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}count-order-staff`, orderCountRequest);
    }

    public getDashboard(searchText: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}dashboard/${searchText}`);
    }

    public downloadPdf(id: string) {
        return api.get(`${this.ROOT_API}pdf/generate/${id}`, {
            responseType: "blob", // nhận file dạng blob
        });
    }
}

export const orderService = new OrderService();
