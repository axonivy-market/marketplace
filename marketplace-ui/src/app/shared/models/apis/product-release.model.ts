export interface ProductRelease {
    name: string;
    body: string;
    publishedAt: string;
    htmlUrl: string;
    _links?: {
      self: {
        href: string;
      };
    };
}