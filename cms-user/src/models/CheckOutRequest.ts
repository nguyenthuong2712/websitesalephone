export interface ICheckOutRequest {
    city: string;
}

export class CheckOutRequest {
    public addressLine: string;

    constructor(
        addressLine: string,
    ) {
        this.addressLine = addressLine.trim();
    }

    toPayload(): ICheckOutRequest {
        return {
            addressLine: this.addressLine,
        };
    }
}
