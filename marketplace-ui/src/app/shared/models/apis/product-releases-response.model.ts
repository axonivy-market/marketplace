import { Link } from "./link.model";
import { Page } from "./page.model";
import { ProductRelease } from "./product-release.model";

export interface ProductReleasesApiResponse {
    _embedded: {
        gitHubReleaseModelList: ProductRelease[];
    };
    _links: Link;
    page: Page;
}
