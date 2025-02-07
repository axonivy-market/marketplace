import { Link } from "./link.model";
import { Page } from "./page.model";
import { ProductRelease } from "./product-release.model";

export interface ProductReleaseApiResponse {
    _embedded: {
        githubReleaseModelList: ProductRelease[];
    };
    _links: Link;
    page: Page;
}
