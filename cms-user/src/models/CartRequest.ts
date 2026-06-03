export interface ICartRequest {
    idCartItem: string
    productId: string
    quantity: number
}

export class CartRequest {
    public idCartItem: string
    public productId: string
    public quantity: number

    constructor(idCartItem: string, productId: string, quantity: number) {
        this.idCartItem = idCartItem.trim()
        this.productId = productId.trim()
        this.quantity = quantity
    }

    toPayload(): ICartRequest {
        return {
            idCartItem: this.idCartItem,
            productId: this.productId,
            quantity: this.quantity,
        }
    }
}
