export interface ReleaseLetter {
  id: string;
  sprint: string;
  content?: string;
  draftContent?: string;
  latest: boolean;
  createdAt: string;
  updatedAt: string;
}
