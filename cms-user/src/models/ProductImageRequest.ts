export class ProductImageRequest {
    productImageId?: string;
    productId?: string;
    url?: string;
    isActive?: boolean;

    constructor(data?: Partial<ProductImageRequest>) {
        if (data) {
            this.productImageId = data.productImageId;
            this.productId = data.productId;
            this.url = data.url;
            this.isActive = data.isActive;
        }
    }

    toPayload() {
        return {
            productImageId: this.productImageId,
            productId: this.productId,
            url: this.url,
            isActive: this.isActive,
        };
    }
}
