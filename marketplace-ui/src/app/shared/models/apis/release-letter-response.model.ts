export interface ReleaseLetterApiResponse {
  content: string;
  sprint: string;
  active: boolean;
  _links?: {
    self: {
      href: string;
    };
  };
}
