import { Injectable, signal, WritableSignal } from '@angular/core';
import { DisplayValue } from '../../../shared/models/display-value.model';

@Injectable({
  providedIn: 'root'
})
export class ProductDetailService {
  productId: WritableSignal<string> = signal('');
  productNames: WritableSignal<DisplayValue> = signal({} as DisplayValue);
}
