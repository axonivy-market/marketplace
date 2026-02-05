import { Link } from "./link.model";
import { Page } from "./page.model";
import { ReleaseLetterApiResponse } from "./release-letter-response.model";

export interface ReleaseLetterListApiResponse {
    _embedded: {
        releaseLetterModelList: ReleaseLetterApiResponse[];
    };
    _links: Link;
    page: Page;
}