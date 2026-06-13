import { Product } from '../product.model';
import { Link } from './link.model';
import { Page } from './page.model';

export interface ProductApiResponse {
    content: Product[];
    links: Link;
    page: Page;
}
