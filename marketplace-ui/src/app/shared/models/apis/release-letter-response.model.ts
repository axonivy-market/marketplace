export interface ReleaseLetterApiResponse {
  content: string;
  sprint: string;
  _links?: {
    self: {
      href: string;
    };
  };
}
