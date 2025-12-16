export interface IDynamicRequest {
    id?: string;
    name: string;
    dynamicName: string;
}

export class DynamicRequest {
    public id?: string;
    public name: string;
    public dynamicName: string;

    constructor(name: string, dynamicName: string, id?: string) {
        this.name = name.trim();
        this.dynamicName = dynamicName.trim();
        if (id) this.id = id.trim();
    }

    // Chuyển sang payload gửi API
    toPayload(): IDynamicRequest {
        return {
            id: this.id,
            name: this.name,
            dynamicName: this.dynamicName,
        };
    }
}
