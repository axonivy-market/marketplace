import { FeedbackStatus } from "../enums/feedback-status.enum";

export interface Feedback {
  id?: string;
  username?: string;
  userAvatarUrl?: string;
  userProvider?: string;
  createdAt?: Date;
  updatedAt?: Date;
  content: string;
  rating: number;
  productId: string;
  isExpanded?: boolean;
  feedbackStatus: FeedbackStatus;
  moderatorId: string;
  reviewDate?: Date;
}
