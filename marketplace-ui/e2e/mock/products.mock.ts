import type { Product } from '../../src/app/shared/models/product.model';
import type { ProductApiResponse } from '../../src/app/shared/models/apis/product-response.model';

const DEFAULT_PAGE_SIZE = 20;

type ProductFixture = Pick<
  Product,
  'id' | 'names' | 'shortDescriptions' | 'type' | 'logoUrl' | 'tags' | 'marketDirectory'
> &
  Partial<Product>;

function createProduct(fixture: ProductFixture): Product {
  return {
    version: '10.0.0',
    cost: 'Free',
    platformReview: '4.5',
    vendor: 'Axon Ivy AG',
    vendorImage: 'https://example.test/vendor.png',
    vendorUrl: 'https://www.axonivy.com',
    sourceUrl: `https://github.com/axonivy-market/${fixture.id}`,
    statusBadgeUrl: `https://github.com/axonivy-market/${fixture.id}/actions/workflows/ci.yml/badge.svg`,
    language: 'English',
    industry: 'Cross-Industry',
    listed: true,
    validate: true,
    versionDisplay: '10.0.0',
    installMatcher: fixture.id,
    contactUs: false,
    mavenArtifacts: [],
    ...fixture,
    _links: {
      self: {
        href: `/api/product-details/${fixture.id}?type=${fixture.type}`
      }
    }
  };
}

export const PRODUCTS = [
  createProduct({
    id: 'amazon-comprehend',
    names: { en: 'Amazon Comprehend', de: 'Amazon Comprehend' },
    shortDescriptions: {
      en: 'Amazon Comprehend description',
      de: 'Amazon Comprehend description'
    },
    logoUrl: 'https://example.test/logo-amazon.png',
    type: 'connector',
    tags: ['AI'],
    marketDirectory: 'market/connector/amazon-comprehend/'
  }),
  createProduct({
    id: 'a-trust',
    names: { en: 'A-Trust', de: 'A-Trust' },
    shortDescriptions: {
      en: 'A-Trust description',
      de: 'A-Trust description'
    },
    logoUrl: 'https://example.test/logo-a-trust.png',
    type: 'connector',
    tags: ['e-signature'],
    marketDirectory: 'market/connector/a-trust/'
  }),
  createProduct({
    id: 'mailstore-connector',
    names: { en: 'Mailstore', de: 'Mailstore' },
    shortDescriptions: {
      en: 'Mailstore description',
      de: 'Mailstore description'
    },
    logoUrl: 'https://example.test/logo-mailstore.png',
    type: 'connector',
    tags: ['office', 'email'],
    marketDirectory: 'market/connector/mailstore-connector/'
  })
] as const;

function normalizeType(type: string): string | undefined {
  const normalized = type.trim().toLowerCase();
  if (!normalized || normalized === 'all') {
    return undefined;
  }

  const typeMap: Record<string, string> = {
    connectors: 'connector',
    utilities: 'util',
    demos: 'demo',
    solutions: 'solution'
  };

  return typeMap[normalized] ?? normalized;
}

function buildProductHref(pageNumber: number, pageSize: number, keyword: string, type: string): string {
  const params = new URLSearchParams();
  params.set('type', type);
  params.set('page', `${pageNumber}`);
  params.set('size', `${pageSize}`);

  const normalizedKeyword = keyword.trim();
  if (normalizedKeyword) {
    params.set('keyword', normalizedKeyword);
  }

  return `/api/product?${params.toString()}`;
}

function matchesKeyword(product: Product, keyword: string): boolean {
  const normalizedKeyword = keyword.trim().toLowerCase();
  if (!normalizedKeyword) {
    return true;
  }

  return [
    product.id,
    ...Object.values(product.names ?? {}),
    ...Object.values(product.shortDescriptions ?? {}),
    ...(product.tags ?? [])
  ].some(value => `${value}`.toLowerCase().includes(normalizedKeyword));
}

export function buildProductsPageWithSearch(
  pageNumber: number,
  pageSize: number,
  keyword: string,
  products: readonly Product[] = PRODUCTS,
  type = 'all'
): ProductApiResponse {
  const normalizedType = normalizeType(type);
  const filteredProducts = products.filter(product => {
    const matchesType = !normalizedType || product.type === normalizedType;
    return matchesType && matchesKeyword(product, keyword);
  });
  const effectivePageSize = pageSize > 0 ? pageSize : DEFAULT_PAGE_SIZE;
  const totalElements = filteredProducts.length;
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / effectivePageSize);
  const start = pageNumber * effectivePageSize;
  const end = start + effectivePageSize;
  const pagedProducts = filteredProducts.slice(start, end);

  return {
    _embedded: {
      products: pagedProducts
    },
    _links:
      totalElements === 0
        ? {
            self: { href: buildProductHref(pageNumber, effectivePageSize, keyword, type) }
          }
        : {
            first: { href: buildProductHref(0, effectivePageSize, keyword, type) },
            self: { href: buildProductHref(pageNumber, effectivePageSize, keyword, type) },
            ...(pageNumber < totalPages - 1
              ? {
                  next: {
                    href: buildProductHref(pageNumber + 1, effectivePageSize, keyword, type)
                  }
                }
              : {}),
            last: {
              href: buildProductHref(Math.max(totalPages - 1, 0), effectivePageSize, keyword, type)
            }
          },
    page: {
      size: effectivePageSize,
      totalElements,
      totalPages,
      number: pageNumber
    }
  };
}

export const PRODUCTS_PAGE = buildProductsPageWithSearch(0, DEFAULT_PAGE_SIZE, '');
export const PRODUCTS_EMPTY_PAGE = buildProductsPageWithSearch(0, DEFAULT_PAGE_SIZE, '', []);

export function buildProductsPage(pageNumber: number, pageSize: number, keyword = '', type = 'all') {
  return buildProductsPageWithSearch(pageNumber, pageSize, keyword, PRODUCTS, type);
}
