import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { DisplayValue } from '../../../shared/models/display-value.model';
import { HttpClient, HttpContext } from '@angular/common/http';
import { LoadingService } from '../../../core/services/loading/loading.service';
import { Observable } from 'rxjs';
import { API_URI } from '../../../shared/constants/api.constant';
import { ForwardingError } from '../../../core/interceptors/api.interceptor';

@Injectable({
  providedIn: 'root'
})
export class ProductDetailService {
  productId: WritableSignal<string> = signal('');
  productNames: WritableSignal<DisplayValue> = signal({} as DisplayValue);
  httpClient = inject(HttpClient);
  loadingService = inject(LoadingService);

  
  getExteralDocumentLinkForProductByVersion(productId: string, version: string): Observable<String> {
    return this.httpClient.get<String>(
      `${API_URI.EXTERNAL_DOCUMENT}/${productId}/${version}`, { context: new HttpContext().set(ForwardingError, true)}
    );
  }
}
