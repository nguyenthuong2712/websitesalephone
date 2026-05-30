export interface IOrderRequest {
    id?: string;
    status: string;
    shippingFee: number;
    description?: string;
}

export class OrderRequest {
    public id?: string;
    public status: string;
    public shippingFee: number;
    public description?: string;

    constructor(status: string, shippingFee: number, description?: string, id?: string) {
        this.status = status.trim();
        this.shippingFee = shippingFee;
        this.description = description?.trim();
        this.id = id;
    }

    toPayload(): IOrderRequest {
        return {
            id: this.id,
            status: this.status,
            shippingFee: this.shippingFee,
            description: this.description,
        };
    }
}
