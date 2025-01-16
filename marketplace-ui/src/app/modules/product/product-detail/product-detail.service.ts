import { inject, Injectable, signal, WritableSignal } from '@angular/core';
import { DisplayValue } from '../../../shared/models/display-value.model';
import { HttpClient, HttpContext } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URI } from '../../../shared/constants/api.constant';
import { ForwardingError } from '../../../core/interceptors/api.interceptor';
import { ExternalDocument } from '../../../shared/models/external-document.model';

@Injectable({
  providedIn: 'root'
})
export class ProductDetailService {
  productId: WritableSignal<string> = signal('');
  productNames: WritableSignal<DisplayValue> = signal({} as DisplayValue);
  productLogoUrl: WritableSignal<string> = signal('');
  httpClient = inject(HttpClient);
  ratingBtnLabel: WritableSignal<string> = signal('');
  noFeedbackLabel: WritableSignal<string> = signal('');

  getExternalDocumentForProductByVersion(productId: string, version: string): Observable<ExternalDocument> {
    return this.httpClient.get<ExternalDocument>(
      `${API_URI.EXTERNAL_DOCUMENT}/${productId}/${version}`, { context: new HttpContext().set(ForwardingError, true)}
    );
  }
}
