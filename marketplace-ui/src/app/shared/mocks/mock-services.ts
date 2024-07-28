import { Observable, of } from 'rxjs';
import { Criteria } from '../models/criteria.model';
import { TypeOption } from '../enums/type-option.enum';
import {
  MOCK_PRODUCTS,
  MOCK_PRODUCTS_FILTER_CONNECTOR,
  MOCK_PRODUCTS_NEXT_PAGE,
  MOCK_PRODUCT_DETAIL
} from './mock-data';
import { ProductApiResponse } from '../models/apis/product-response.model';

export class MockProductService {
  findProductsByCriteria(criteria: Criteria): Observable<ProductApiResponse> {
    let response = MOCK_PRODUCTS;
    if (criteria.nextPageHref) {
      response = MOCK_PRODUCTS_NEXT_PAGE;
    } else if (criteria.type == TypeOption.CONNECTORS) {
      response = MOCK_PRODUCTS_FILTER_CONNECTOR;
    }
    return of(response);
  }

  getProductDetails(productId: string, tag: string) {
    console.log(2)

    return of(MOCK_PRODUCT_DETAIL);
  }

  getProductDetailsWithVersion(productId: string, version: string) {
    console.log(1)
    return of(MOCK_PRODUCT_DETAIL);
  }
}
