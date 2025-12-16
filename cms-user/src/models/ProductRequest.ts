export interface IProductRequest {
    name: string;
    description?: string;
}

export class ProductRequest {
    public name: string;
    public description?: string;

    constructor(data: {
        name: string;
        description?: string;

    }) {
        this.name = data.name.trim();
        this.description = data.description?.trim();
    }

    toPayload(): IProductVariantRequest {
        return {
            name: this.name,
            description: this.description,
        };
    }
}
