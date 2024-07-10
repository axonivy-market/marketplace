import { Observable, of } from 'rxjs';
import { Product } from '../models/product.model';
import { Criteria } from '../models/criteria.model';
import { TypeOption } from '../enums/type-option.enum';
import { MOCK_PRODUCTS, MOCK_PRODUCTS_FILTER_CONNECTOR, MOCK_PRODUCTS_NEXT_PAGE } from './mock-data';
import { ProductApiResponse } from '../models/apis/product-response.model';

const products = MOCK_PRODUCTS._embedded.products as Product[];
export class MockProductService {

  getProductById(id: string) {
    return of(products.find(product => product.id === id));
  }

  findProductsByCriteria(criteria: Criteria): Observable<ProductApiResponse> {
    let response = MOCK_PRODUCTS;
    if (criteria.nextPageHref) {
      response = MOCK_PRODUCTS_NEXT_PAGE;
    } else if (criteria.type == TypeOption.CONNECTORS) {
      response = MOCK_PRODUCTS_FILTER_CONNECTOR;
    }
    return of(response);
  }
}
