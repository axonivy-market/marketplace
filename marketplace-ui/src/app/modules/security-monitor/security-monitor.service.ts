import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ProductSecurityInfo } from '../../shared/models/product-security-info-model';

@Injectable({
  providedIn: 'root'
})
export class SecurityMonitorService {

  private readonly apiUrl = environment.apiUrl + '/api/security-monitor';
  private readonly http = inject(HttpClient);

  getSecurityDetails(token: string): Observable<ProductSecurityInfo[]> {
    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);
    return this.http.get<ProductSecurityInfo[]>(this.apiUrl, { headers });
  }
}
