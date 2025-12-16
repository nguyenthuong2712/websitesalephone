export interface CommonResponse<T> {
    code: number;
    message: string;
    errorFileLog: string | null;
    data: T | null;
    totalPage: number | null;
    totalRecordsPickupDelivery: number | null;
}
