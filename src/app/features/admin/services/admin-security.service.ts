import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {ApiService} from '../../../shared/service/api.service';

export interface DataApiResponse<T> {
    status: string;
    'status-code': number;
    'status-message': string;
    data: T;
}

export interface SecurityEndpointConfig {
    id?: number;
    path: string;
    accessLevel: 'PUBLIC' | 'USER' | 'ADMIN';
    isActive: boolean;
    description: string;
    createdAt?: string;
    updatedAt?: string;
}

@Injectable({
    providedIn: 'root'
})
export class AdminSecurityService {
    private readonly ENDPOINT = 'admin/security-configs';

    constructor(private readonly apiService: ApiService) {
    }

    getAll(): Observable<DataApiResponse<SecurityEndpointConfig[]>> {
        return this.apiService.get<DataApiResponse<SecurityEndpointConfig[]>>(this.ENDPOINT);
    }

    getById(id: number): Observable<DataApiResponse<SecurityEndpointConfig>> {
        return this.apiService.getOne<DataApiResponse<SecurityEndpointConfig>>(this.ENDPOINT, id);
    }

    create(config: SecurityEndpointConfig): Observable<DataApiResponse<SecurityEndpointConfig>> {
        return this.apiService.create<DataApiResponse<SecurityEndpointConfig>>(this.ENDPOINT, config);
    }

    update(id: number, config: SecurityEndpointConfig): Observable<DataApiResponse<SecurityEndpointConfig>> {
        return this.apiService.update<DataApiResponse<SecurityEndpointConfig>>(this.ENDPOINT, id, config);
    }

    toggle(id: number): Observable<DataApiResponse<SecurityEndpointConfig>> {
        return this.apiService.patch<DataApiResponse<SecurityEndpointConfig>>(`${this.ENDPOINT}/${id}/toggle`, {});
    }

    delete(id: number): Observable<DataApiResponse<void>> {
        return this.apiService.remove<DataApiResponse<void>>(this.ENDPOINT, id);
    }

    reload(): Observable<DataApiResponse<void>> {
        return this.apiService.post<DataApiResponse<void>>(`${this.ENDPOINT}/reload`, {});
    }
}
