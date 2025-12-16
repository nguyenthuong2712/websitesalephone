import axios, { AxiosResponse } from 'axios';
import type {Search} from "../models/Search.ts";
import type {ProductVariantRequest} from "../models/ProductVariantRequest.ts";
import type {ProductDetailRequest} from "../models/ProductDetailRequest.ts";
import type {CreateCartRequest} from "../models/CreateCartRequest.ts";
import api from "../api/api.ts";
import {ProductRequest} from "../models/ProductRequest.ts";
import type {ProductImageRequest} from "../models/ProductImageRequest.ts";

class ProductService {
    private ROOT_API = import.meta.env.VITE_ROOT_API + '/api/product/';

    public search(productSearch: Search): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}search`, productSearch);
    }

    public createProduct(productRequest: ProductRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}create-product`, productRequest);
    }

    public createProductDetail(productVariantRequest: ProductVariantRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}create-product-detail`, productVariantRequest);
    }

    public update(productRequest: ProductVariantRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update`, productRequest);
    }

    public detail(productDetailRequest: ProductDetailRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}detail`,productDetailRequest);
    }

    public getQuantity(cartRequest: CreateCartRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}get-quantity`,cartRequest);
    }

    public createImage(request: ProductImageRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}create-image`, request);
    }

    public getAllImage(productId: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}list/${productId}`);
    }

    public updateImage(request: ProductImageRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update-image`, request);
    }

    public deleteProduct(id: string): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}deleted/${id}` );
    }

    public deleteImage(id: string): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}deleted-image/${id}` );
    }

    public deleteProductDetail(id: string): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}deleted-product-detail/${id}`);
    }

    public getAllProductVariant(id: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}get-all-product-variant/${id}`);
    }

    public getProductVariantDetail(id: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}get-product-variant-detail/${id}`);
    }

    public getAllNewProduct(): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}new-product`);
    }

}

export const productService = new ProductService();
