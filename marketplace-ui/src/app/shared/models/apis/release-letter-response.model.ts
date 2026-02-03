export interface ReleaseLetterResponse {
  content: string;
  releaseVersion: string;
  _links?: {
    self: {
      href: string;
    };
  };
}
