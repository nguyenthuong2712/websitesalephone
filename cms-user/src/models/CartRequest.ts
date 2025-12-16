export interface ICartRequest {
    idCartItem: string;
    productId: string;
    quantity: number;
    add
}

export class CartRequest {
    public idCartItem: string;
    public productId: string;
    public quantity: number;

    constructor(idCartItem: string, productId: string, quantity: number) {
        this.idCartItem = idCartItem.trim();
        this.productId = productId.trim();
        this.quantity = quantity;
    }

    // Chuyển sang payload gửi API
    toPayload(): ICartRequest {
        return {
            idCartItem: this.idCartItem,
            productId: this.productId,
            quantity: this.quantity,
        };
    }
}
