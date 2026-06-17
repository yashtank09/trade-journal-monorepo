import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiService } from '../../shared/service/api.service';
import { User } from '../models/user.model';

export interface DataApiResponse<T> {
  status: string;
  'status-code': number;
  'status-message': string;
  data: T;
}

@Injectable({
  providedIn: 'root'
})
export class ProfileService {
  private readonly apiService = inject(ApiService);
  private readonly ENDPOINT = 'users/profile';

  /**
   * Fetches the user profile details from the backend
   */
  getProfile(): Observable<DataApiResponse<User>> {
    return this.apiService.get<DataApiResponse<User>>(this.ENDPOINT);
  }

  /**
   * Updates the user profile details on the backend
   */
  updateProfile(profileData: Partial<User>): Observable<DataApiResponse<User>> {
    return this.apiService.put<DataApiResponse<User>>(this.ENDPOINT, profileData);
  }
}
