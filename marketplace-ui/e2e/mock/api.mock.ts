import type { Page } from '@playwright/test';
import { buildProductsPageWithSearch, PRODUCTS_EMPTY_PAGE, PRODUCTS_PAGE } from './products.mock';
import type { Product } from '../../src/app/shared/models/product.model';

export type ProductsMockOptions = {
  products?: readonly Product[];
};

/**
 * Wire marketplace product API routes for a single test page.
 * The handler computes the response from the request query params so pagination
 * and keyword filtering behave like the real endpoint.
 */
export async function setupApiMocks(page: Page, options: ProductsMockOptions = {}): Promise<void> {
  await page.route('**/api/product**', async route => {
    const url = new URL(route.request().url());
    const pageNumber = Number(url.searchParams.get('page') ?? '0');
    const pageSize = Number(url.searchParams.get('size') ?? '20');
    const keyword = url.searchParams.get('keyword') ?? url.searchParams.get('search') ?? '';
    const type = url.searchParams.get('type') ?? 'all';
    const products = options.products ?? PRODUCTS_PAGE._embedded.products;
    const response = buildProductsPageWithSearch(pageNumber, pageSize, keyword, products, type);

    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({
        ...response
      })
    });
  });
}

/** Convenience: wire an empty product list so only the empty-state is rendered. */
export async function setupEmptyProductsMock(page: Page): Promise<void> {
  return setupApiMocks(page, { products: PRODUCTS_EMPTY_PAGE._embedded.products });
}
