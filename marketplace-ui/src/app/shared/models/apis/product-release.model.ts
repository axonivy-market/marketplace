export interface ProductRelease {
    name: string;
    body: string;
    publishedAt: string;
    _links?: {
      self: {
        href: string;
      };
    };
}