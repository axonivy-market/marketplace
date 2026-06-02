import { HttpClient, HttpContext, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AdminAuthService } from '../admin-auth.service';
import { LoadingComponent } from '../../../core/interceptors/api.interceptor';
import { LoadingComponentId } from '../../../shared/enums/loading-component-id';
import { API_URI } from '../../../shared/constants/api.constant';

export interface AppSetting {
  settingKey: string;
  settingValue: string;
  category: string;
  description: string;
  encrypted: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AppSettingsService {

  constructor(
    private readonly http: HttpClient,
    private readonly adminAuth: AdminAuthService
  ) {}

  getSettings(searchText = ''): Observable<AppSetting[]> {

    let params = new HttpParams();

    if (searchText?.trim()) {
      params = params.set('search', searchText);
    }

    return this.http.get<AppSetting[]>(
      API_URI.APP_SETTINGS,
      {
        params,
        headers: this.adminAuth.getAuthHeaders(),
        context: new HttpContext().set(
          LoadingComponent,
          LoadingComponentId.APP_SETTINGS
        )
      }
    );
  }

  updateSetting(setting: AppSetting): Observable<AppSetting> {
    return this.http.put<AppSetting>(
      `${API_URI.APP_SETTINGS}/${encodeURIComponent(setting.settingKey)}`,
      {
        settingValue: setting.settingValue
      },
      {
        headers: this.adminAuth.getAuthHeaders(),
        context: new HttpContext().set(
          LoadingComponent,
          LoadingComponentId.APP_SETTINGS
        )
      }
    );
  }
}