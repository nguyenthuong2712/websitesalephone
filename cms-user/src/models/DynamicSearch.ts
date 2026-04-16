export interface IDynamicSearch {
    dynamicEnum: string;
}

export class DynamicSearch {
    public dynamicEnum: string;

    constructor(dynamicEnum: string) {
        this.dynamicEnum = dynamicEnum.trim();
    }

    // Chuyển sang payload gửi API
    toPayload(): IDynamicSearch {
        return {
            dynamicEnum: this.dynamicEnum,
        };
    }
}
