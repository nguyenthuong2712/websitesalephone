export interface ICheckOutRequest {
    addressLine: string;
    paymentMethod?: string;
    cartItemIds?: string[];
}

export class CheckOutRequest {
    public addressLine: string;
    public paymentMethod?: string;
    public cartItemIds?: string[];

    constructor(
        addressLine: string,
        paymentMethod?: string,
        cartItemIds?: string[]
    ) {
        this.addressLine = addressLine.trim();
        this.paymentMethod = paymentMethod;
        this.cartItemIds = cartItemIds;
    }

    toPayload(): ICheckOutRequest {
        return {
            addressLine: this.addressLine,
            paymentMethod: this.paymentMethod,
            cartItemIds: this.cartItemIds
        };
    }
}
