export interface IPagingRequest {
    page: number;
    size: number;
}

export class PagingRequest {
    public page: number;
    public size: number;

    constructor(page: number = 1, size: number = 10) {
        this.page = page;
        this.size = size;
    }

    toPayload(): IPagingRequest {
        return {
            page: this.page,
            size: this.size,
        };
    }
}

export interface IOrderSearch extends IPagingRequest {
    searchText?: string;
    status?: string;
    minPrice?: number | null;
    maxPrice?: number | null;
    ramId?: string | null;
    cameraId?: string | null;
    originId?: string | null;
    shopId?: string | null;
    sortBy?: string | null;
    sortDesc?: boolean;
}

export class Search extends PagingRequest {
    public searchText?: string;
    private status?: string;
    public minPrice?: number | null;
    public maxPrice?: number | null;
    public ramId?: string | null;
    public cameraId?: string | null;
    public originId?: string | null;
    public shopId?: string | null;
    public sortBy?: string | null;
    public sortDesc?: boolean;

    constructor(
        page: number = 1,
        size: number = 10,
        searchText?: string,
        status?: string,
        minPrice?: number | null,
        maxPrice?: number | null,
        ramId?: string | null,
        cameraId?: string | null,
        originId?: string | null,
        shopId?: string | null,
        sortBy?: string | null,
        sortDesc: boolean = false
    ) {
        super(page, size);
        this.searchText = searchText?.trim();
        this.status = status?.trim();
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.ramId = ramId;
        this.cameraId = cameraId;
        this.originId = originId;
        this.shopId = shopId;
        this.sortBy = sortBy;
        this.sortDesc = sortDesc;
    }

    toPayload(): IOrderSearch {
        return {
            ...super.toPayload(),
            searchText: this.searchText,
            status: this.status,
            minPrice: this.minPrice,
            maxPrice: this.maxPrice,
            ramId: this.ramId,
            cameraId: this.cameraId,
            originId: this.originId,
            shopId: this.shopId,
            sortBy: this.sortBy,
            sortDesc: this.sortDesc
        };
    }
}
