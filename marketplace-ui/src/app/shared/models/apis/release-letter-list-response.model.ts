import { Link } from "./link.model";
import { Page } from "./page.model";
import { ReleaseLetterResponse } from "./release-letter-response.model";

export interface ReleaseLetterListApiResponse {
    _embedded: {
        releaseLetterModelList: ReleaseLetterResponse[];
    };
    _links: Link;
    page: Page;
}