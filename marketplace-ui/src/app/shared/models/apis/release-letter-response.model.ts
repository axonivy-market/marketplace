export interface ReleaseLetterApiResponse {
  sprint: string;
  content?: string;
  createdAt: string;
  latest: boolean;
  _links?: {
    self: {
      href: string;
    };
  };
}
