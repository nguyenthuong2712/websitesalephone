import type { AxiosResponse } from 'axios'
import type { Search } from '../models/Search'
import type { ProductVariantRequest } from '../models/ProductVariantRequest'
import type { ProductDetailRequest } from '../models/ProductDetailRequest'
import type { CreateCartRequest } from '../models/CreateCartRequest'
import type { ProductImageRequest } from '../models/ProductImageRequest'
import api from '../api/api'
import { ProductRequest } from '../models/ProductRequest'

class ProductService {
    private readonly ROOT_API = `${import.meta.env.VITE_ROOT_API}/api/product/`

    public search(productSearch: Search): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}search`, productSearch.toPayload())
    }

    public createProduct(productRequest: ProductRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}create-product`, productRequest.toPayload())
    }

    public createProductDetail(productVariantRequest: ProductVariantRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}create-product-detail`, productVariantRequest.toPayload())
    }

    public update(productRequest: ProductVariantRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update`, productRequest.toPayload())
    }

    public detail(productDetailRequest: ProductDetailRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}detail`, productDetailRequest)
    }

    public getQuantity(cartRequest: CreateCartRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}get-quantity`, cartRequest)
    }

    public createImage(request: ProductImageRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}create-image`, request)
    }

    public getAllImage(productId: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}list/${productId}`)
    }

    public updateImage(request: ProductImageRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update-image`, request)
    }

    public deleteProduct(id: string): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}deleted/${id}`)
    }

    public deleteImage(id: string): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}deleted-image/${id}`)
    }

    public deleteProductDetail(id: string): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}deleted-product-detail/${id}`)
    }

    public getAllProductVariant(id: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}get-all-product-variant/${id}`)
    }

    public getProductVariantDetail(id: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}get-product-variant-detail/${id}`)
    }

    public getAllNewProduct(): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}new-product`)
    }
}

export const productService = new ProductService()
