export interface IProductVariantRequest {
    productName: string;
    description?: string;
    quantity: number;
    price: number;
    productVariantId?: string;
    idProduct?: string;
    colorId?: string;
    cameraId?: string;
    imageId?: string;
    screenId?: string;
    originId?: string;
    ramId?: string;
}

export class ProductVariantRequest {
    public productName: string;
    public description?: string;
    public quantity: number;
    public price: number;
    public productVariantId?: string;
    public idProduct?: string;
    public colorId?: string;
    public cameraId?: string;
    public imageId?: string;
    public screenId?: string;
    public originId?: string;
    public ramId?: string;

    constructor(data: {
        productName: string;
        description?: string;
        quantity: number;
        price: number;
        productVariantId?: string;
        idProduct?: string;
        colorId?: string;
        cameraId?: string;
        imageId?: string;
        screenId?: string;
        originId?: string;
        ramId?: string;
    }) {
        this.productName = data.productName.trim();
        this.description = data.description?.trim();
        this.quantity = data.quantity;
        this.price = data.price;
        this.productVariantId = data.productVariantId?.trim();
        this.idProduct = data.idProduct?.trim();
        this.colorId = data.colorId?.trim();
        this.cameraId = data.cameraId?.trim();
        this.imageId = data.imageId?.trim();
        this.screenId = data.screenId?.trim();
        this.originId = data.originId?.trim();
        this.ramId = data.ramId?.trim();
    }

    toPayload(): IProductVariantRequest {
        return {
            productName: this.productName,
            description: this.description,
            quantity: this.quantity,
            price: this.price,
            productVariantId: this.productVariantId,
            idProduct: this.idProduct,
            colorId: this.colorId,
            cameraId: this.cameraId,
            imageId: this.imageId,
            screenId: this.screenId,
            originId: this.originId,
            ramId: this.ramId,
        };
    }
}
