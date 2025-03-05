export interface ProductRelease {
    name: string;
    body: string;
    publishedAt: string;
    htmlUrl: string;
    latestRelease: boolean;
    _links?: {
      self: {
        href: string;
      };
    };
}