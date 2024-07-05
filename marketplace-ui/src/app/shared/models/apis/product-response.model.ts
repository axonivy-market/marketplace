import { Product } from "../product.model";
import { Link } from "./link.model";
import { Page } from "./page.model";

export interface ProductApiResponse {
    _embedded: {
        products: Product[];
    };
    _links: Link;
    page: Page;
}