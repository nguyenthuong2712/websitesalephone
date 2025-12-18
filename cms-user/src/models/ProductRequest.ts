export interface IProductRequest {
    idProduct: string;
    productName: string;
    description?: string;
    status?: string;
}

export class ProductRequest {
    public idProduct: string;
    public productName: string;
    public description?: string;
    public status?: string;

    constructor(data: {
        idProduct: string;
        productName: string;
        description?: string;
        status?: string;

    }) {
        this.idProduct = data.idProduct.trim();
        this.productName = data.productName.trim();
        this.description = data.description?.trim();
        this.status = data.status?.trim();
    }

    toPayload(): IProductRequest {
        return {
            idProduct: this.idProduct,
            productName: this.productName,
            description: this.description,
            status: this.status,
        };
    }
}
