import api from "@/api/api";
import type { AxiosResponse } from "axios";

class ShopService {
    private ROOT_API = import.meta.env.VITE_ROOT_API + '/api/shop/';

    public registerShop(payload: {
        username: string;
        shopName: string;
        description: string;
        paymentMethods: Array<{ method: string; qrCode?: string; note?: string }>;
    }, avatarShop?: File | null, bannerImage?: File | null, cccdImage?: File | null): Promise<AxiosResponse> {
        const formData = new FormData();
        formData.append('request', JSON.stringify(payload));

        if (avatarShop) formData.append('avatarShop', avatarShop);
        if (bannerImage) formData.append('bannerImage', bannerImage);
        if (cccdImage) formData.append('cccdImage', cccdImage);

        return api.post(`${this.ROOT_API}register`, formData, {
            headers: {
                'Content-Type': 'multipart/form-data'
            }
        });
    }
}

export const shopService = new ShopService();
