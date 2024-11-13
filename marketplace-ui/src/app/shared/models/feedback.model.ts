export interface Feedback {
  username?: string;
  userAvatarUrl?: string;
  userProvider?: string;
  createdAt?: Date;
  updatedAt?: Date;
  content: string;
  rating: number;
  productId: string;
  isExpanded?: boolean;
}
