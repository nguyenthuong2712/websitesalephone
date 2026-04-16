import axios, { AxiosResponse } from 'axios';

class ProductImageService {
    private ROOT_API = import.meta.env.VITE_ROOT_API + '/api/product-images/';

    public uploadImages(files: File[], productId: string): Promise<AxiosResponse> {
        const formData = new FormData();
        files.forEach(file => formData.append('files', file));
        formData.append('productId', productId);

        return axios.post(`${this.ROOT_API}upload`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
    }

    public getImagesByProduct(productId: string): Promise<AxiosResponse> {
        return axios.get(`${this.ROOT_API}${productId}`);
    }

    public deleteImage(imageId: string): Promise<AxiosResponse> {
        return axios.delete(`${this.ROOT_API}${imageId}`);
    }

    public updateImage(imageId: string, productId: string): Promise<AxiosResponse> {
        const params = new URLSearchParams();
        params.append('imageId', imageId);
        params.append('productId', productId);

        return axios.put(`${this.ROOT_API}update`, params, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
        });
    }
}

export const productImageService = new ProductImageService();
