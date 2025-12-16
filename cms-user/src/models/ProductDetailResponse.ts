// models/ProductDetailResponse.ts
export interface IDynamicResponse {
    id: string;
    name: string;
}

export interface IProductDetailResponse {
    productName: string;
    description: string;
    quantity: number;
    price: number; // hoặc string nếu backend trả string
    colors: IDynamicResponse[];
    cameras: IDynamicResponse[];
    batterys: IDynamicResponse[];
    cpus: IDynamicResponse[];
    images: IDynamicResponse[];
    screens: IDynamicResponse[];
    origins: IDynamicResponse[];
    storages: IDynamicResponse[];
    operators: IDynamicResponse[];
    rams: IDynamicResponse[];
}

// Class để khởi tạo và xử lý nếu cần
export class ProductDetailResponse implements IProductDetailResponse {
    productName: string;
    description: string;
    quantity: number;
    price: number;
    colors: IDynamicResponse[];
    cameras: IDynamicResponse[];
    batterys: IDynamicResponse[];
    cpus: IDynamicResponse[];
    images: IDynamicResponse[];
    screens: IDynamicResponse[];
    origins: IDynamicResponse[];
    storages: IDynamicResponse[];
    operators: IDynamicResponse[];
    rams: IDynamicResponse[];

    constructor(data: Partial<IProductDetailResponse>) {
        this.productName = data.productName || "";
        this.description = data.description || "";
        this.quantity = data.quantity || 0;
        this.price = data.price || 0;
        this.colors = data.colors || [];
        this.cameras = data.cameras || [];
        this.batterys = data.batterys || [];
        this.cpus = data.cpus || [];
        this.images = data.images || [];
        this.screens = data.screens || [];
        this.origins = data.origins || [];
        this.storages = data.storages || [];
        this.operators = data.operators || [];
        this.rams = data.rams || [];
    }

    // Optional: method map từ API JSON sang class
    static fromApi(data: any): ProductDetailResponse {
        return new ProductDetailResponse({
            productName: data.productName,
            description: data.description,
            quantity: data.quantity,
            price: data.price,
            colors: data.colors || [],
            cameras: data.cameras || [],
            batterys: data.batterys || [],
            cpus: data.cpus || [],
            images: data.images || [],
            screens: data.screens || [],
            origins: data.origins || [],
            storages: data.storages || [],
            operators: data.operators || [],
            rams: data.rams || [],
        });
    }
}
