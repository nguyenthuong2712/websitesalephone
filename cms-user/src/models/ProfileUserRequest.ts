export interface IProfileUserRequest {
    fullName: string
    telNo: string
    email: string
    address: string
    gender: string
}

export class ProfileUserRequest implements IProfileUserRequest {
    fullName: string
    telNo: string
    email: string
    address: string
    gender: string

    constructor(fullName: string, telNo: string, email: string, address: string, gender: string) {
        this.fullName = fullName
        this.telNo = telNo
        this.email = email
        this.address = address
        this.gender = gender
    }

    static from(data: Partial<IProfileUserRequest>): ProfileUserRequest {
        return new ProfileUserRequest(
            data.fullName ?? '',
            data.telNo ?? '',
            data.email ?? '',
            data.address ?? '',
            data.gender ?? '',
        )
    }
}
