export interface IProductRequest {
    idProduct: string;
    productName: string;
    description?: string;
    location?: string;
    storage?: string;
    deviceMake?: string;
    status?: string;
}

export class ProductRequest {
    public idProduct: string;
    public productName: string;
    public description?: string;
    public location?: string;
    public storage?: string;
    public deviceMake?: string;
    public status?: string;

    constructor(data: {
        idProduct: string;
        productName: string;
        description?: string;
        location?: string;
        storage?: string;
        deviceMake?: string;
        status?: string;

    }) {
        this.idProduct = data.idProduct.trim();
        this.productName = data.productName.trim();
        this.description = data.description?.trim();
        this.location = data.location?.trim();
        this.storage = data.storage?.trim();
        this.deviceMake = data.deviceMake?.trim();
        this.status = data.status?.trim();
    }

    toPayload(): IProductRequest {
        return {
            idProduct: this.idProduct,
            productName: this.productName,
            description: this.description,
            location: this.location,
            storage: this.storage,
            deviceMake: this.deviceMake,
            status: this.status,
        };
    }
}
