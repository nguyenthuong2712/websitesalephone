export interface IProductDetailRequest {
    idProduct?: string;
}

export class ProductDetailRequest {
    public idProduct?: string;

    constructor(data: {
        idProduct?: string;
    }) {
        this.idProduct = data.idProduct?.trim();
    }

    // Chuyển sang payload gửi API
    toPayload(): IProductDetailRequest {
        return {
            idProduct: this.idProduct
        };
    }
}
