import type { AxiosResponse } from 'axios'
import type { DynamicSearch } from '../models/DynamicSearch'
import type { DynamicRequest } from '../models/DynamicRequest'
import api from '../api/api'

class DynamicService {
    private readonly ROOT_API = `${import.meta.env.VITE_ROOT_API}/api/dynamic/`

    public search(dynamicSearch: DynamicSearch): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}search`, dynamicSearch)
    }

    public create(dynamicRequest: DynamicRequest): Promise<AxiosResponse> {
        return api.post(`${this.ROOT_API}create`, dynamicRequest)
    }

    public update(dynamicRequest: DynamicRequest): Promise<AxiosResponse> {
        return api.put(`${this.ROOT_API}update`, dynamicRequest)
    }

    public delete(dynamicRequest: DynamicRequest): Promise<AxiosResponse> {
        return api.delete(`${this.ROOT_API}delete`, { data: dynamicRequest })
    }
}

export const dynamicService = new DynamicService()
