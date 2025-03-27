import { FeedbackStatus } from "../enums/feedback-status.enum";
import { DisplayValue } from "./display-value.model";

export interface Feedback {
  id?: string;
  username?: string;
  userId?: string;
  userAvatarUrl?: string;
  userProvider?: string;
  createdAt?: Date;
  updatedAt?: Date;
  content: string;
  rating: number;
  productId: string;
  productNames: DisplayValue;
  isExpanded?: boolean;
  feedbackStatus: FeedbackStatus;
  moderatorName: string;
  reviewDate?: Date;
  version: number;
}
