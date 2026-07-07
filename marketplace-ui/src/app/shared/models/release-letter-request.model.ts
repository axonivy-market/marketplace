export interface ReleaseLetter {
  id: string;
  sprint: string;
  content?: string;
  draftContent?: string;
  hasDraft?: boolean;
  latest: boolean;
  createdAt: string;
  updatedAt: string;
}
