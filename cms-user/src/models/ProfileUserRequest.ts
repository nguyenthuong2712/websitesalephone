export class ProfileUserRequest {
    constructor(
        public fullName: string,
        public telNo: string,
        public email: string,
        public address: string,
        public gender: string
    ) {}

    static from(data: Partial<ProfileUserRequest>) {
        return new ProfileUserRequest(
            data.fullName ?? "",
            data.telNo ?? "",
            data.email ?? "",
            data.address ?? "",
            data.gender ?? ""
        );
    }
}
