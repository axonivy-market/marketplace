import { Feedback } from '../feedback.model';
import { Link } from './link.model';
import { Page } from './page.model';

export interface FeedbackApiResponse {
  _embedded: {
    feedbacks: Feedback[];
  };
  _links: Link;
  page: Page;
}
