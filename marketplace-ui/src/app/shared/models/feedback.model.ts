export interface Feedback {
  username?: string;
  userAvatarUrl?: string;
  userProvider?: string;
  createdDate?: Date;
  updatedDate?: Date;
  content: string;
  rating: number;
  productId: string;
}
