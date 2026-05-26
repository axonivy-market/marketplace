export interface ReleaseLetterApiResponse {
  id: string;
  sprint: string;
  content?: string;
  createdAt: string;
  updatedAt: string;
  latest: boolean;
  _links?: {
    self: {
      href: string;
    };
  };
}
