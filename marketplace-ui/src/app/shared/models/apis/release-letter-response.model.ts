export interface ReleaseLetterApiResponse {
  content: string;
  sprint: string;
  createdAt: string;
  latest: boolean;
  _links?: {
    self: {
      href: string;
    };
  };
}
