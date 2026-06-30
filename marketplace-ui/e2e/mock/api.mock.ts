import type { Page } from '@playwright/test';
import { PRODUCTS_EMPTY_PAGE, PRODUCTS_PAGE } from './products.mock';

export type ProductsMockOptions = {
  products?: object;
};

/**
 * Wire all marketplace API routes for a single test page.
 * Pass `options.products` to override the default product list response.
 */
export async function setupApiMocks(
  page: Page,
  options: ProductsMockOptions = {}
): Promise<void> {
  const productsBody = JSON.stringify(options.products ?? PRODUCTS_PAGE);
  await page.route('**/api/product**', route =>
    route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: productsBody
    })
  );
}

/** Convenience: wire an empty product list so only the empty-state is rendered. */
export async function setupEmptyProductsMock(page: Page): Promise<void> {
  return setupApiMocks(page, { products: PRODUCTS_EMPTY_PAGE });
}
