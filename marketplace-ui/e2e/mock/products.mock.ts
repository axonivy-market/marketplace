export const PRODUCTS_PAGE = {
  _embedded: {
    products: [
      {
        id: 'amazon-comprehend',
        names: { en: 'Amazon Comprehend', de: 'Amazon Comprehend' },
        shortDescriptions: {
          en: 'Amazon Comprehend description',
          de: 'Amazon Comprehend description'
        },
        logoUrl: 'https://example.test/logo-amazon.png',
        type: 'connector',
        tags: ['AI'],
        marketDirectory: 'market/connector/amazon-comprehend/',
        _links: {
          self: {
            href: '/api/product-details/amazon-comprehend?type=connector'
          }
        }
      },
      {
        id: 'a-trust',
        names: { en: 'A-Trust', de: 'A-Trust' },
        shortDescriptions: {
          en: 'A-Trust description',
          de: 'A-Trust description'
        },
        logoUrl: 'https://example.test/logo-a-trust.png',
        type: 'connector',
        tags: ['e-signature'],
        marketDirectory: 'market/connector/a-trust/',
        _links: {
          self: { href: '/api/product-details/a-trust?type=connector' }
        }
      }
    ]
  },
  _links: {
    first: { href: '/api/product?type=all&page=0&size=20' },
    self: { href: '/api/product?type=all&page=0&size=20' },
    last: { href: '/api/product?type=all&page=0&size=20' }
  },
  page: { size: 20, totalElements: 2, totalPages: 1, number: 0 }
} as const;

export const PRODUCTS_EMPTY_PAGE = {
  _embedded: { products: [] },
  _links: { self: { href: '/api/product?type=all&page=0&size=20' } },
  page: { size: 20, totalElements: 0, totalPages: 0, number: 0 }
} as const;
