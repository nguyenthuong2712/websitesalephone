import { AxiosResponse } from 'axios';
import api from '../api/api.ts';
import type {CartRequest} from "../models/CartRequest.ts";
import type {CheckOutRequest} from "../models/CheckOutRequest.ts";

class CartService {
    private ROOT_API = import.meta.env.VITE_ROOT_API + '/api/cart/';

    public addToCart(request: CartRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}add`, request);
    }

    public updateCartItem(request: CartRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update`, request);
    }

    public getCartItems(search: any): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}items`, search);
    }

    public checkoutCart(checkOutRequest: CheckOutRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}checkout`, checkOutRequest);
    }
}

export const cartService = new CartService();
