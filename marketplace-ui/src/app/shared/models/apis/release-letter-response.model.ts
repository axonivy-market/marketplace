export interface ReleaseLetterApiResponse {
  content: string;
  sprint: string;
  createdAt: string;
  active: boolean;
  _links?: {
    self: {
      href: string;
    };
  };
}
