import axios, { AxiosResponse } from 'axios';
import {CreateUserDto} from "../models/CreateUserDto.ts";
import type {UserSearchForm} from "../models/UserSearchForm.ts";
import api from "../api/api.ts";
import type {ProfileUserRequest} from "../models/ProfileUserRequest.ts";

class UserService {
    private ROOT_API = import.meta.env.VITE_ROOT_API + '/api/user/';

    public getUserByLoginId(loginId: string): Promise<AxiosResponse> {
        return api.get(`${this.ROOT_API}get/user/${loginId}`);
    }

    public createUser(createUserDto: CreateUserDto): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}create`, createUserDto);
    }

    public updateUser(updateUserDto: ICreateUserDto): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update`, updateUserDto);
    }

    public deleteUser(userId: string): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}delete/${userId}`);
    }

    public search(searchForm: UserSearchForm): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}search`, searchForm);
    }

    public updateProfileUser(profileUserRequest: ProfileUserRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update-profile-user`, profileUserRequest);
    }
}

export const userService = new UserService();
