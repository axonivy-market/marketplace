export interface ReleaseLetter {
  id: string;
  sprint: string;
  content?: string;
  latest: boolean;
  createdAt: string;
  updatedAt: string;
}
