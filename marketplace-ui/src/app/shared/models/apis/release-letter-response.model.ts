export interface ReleaseLetterApiResponse {
  content: string;
  releaseVersion: string;
  _links?: {
    self: {
      href: string;
    };
  };
}
