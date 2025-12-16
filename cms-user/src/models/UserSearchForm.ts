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

export interface IUserSearchForm extends IPagingRequest {
    role?: string;
    searchText?: string;
    checkLoginId?: string;
}

export class UserSearchForm extends PagingRequest {
    public role?: string;
    public searchText?: string;
    public checkLoginId?: string;

    constructor(page: number = 1, size: number = 10, role?: string, searchText?: string, checkLoginId?: string) {
        super(page, size);
        this.role = role?.trim();
        this.searchText = searchText?.trim();
        this.checkLoginId = checkLoginId?.trim();
    }

    toPayload(): IUserSearchForm {
        return {
            ...super.toPayload(),
            role: this.role,
            searchText: this.searchText,
            checkLoginId: this.checkLoginId,
        };
    }
}
