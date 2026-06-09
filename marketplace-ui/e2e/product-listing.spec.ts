import { expect, test } from '@playwright/test';
import { setupApiMocks, setupEmptyProductsMock } from './mock/api.mock';
import { PRODUCTS_PAGE } from './mock/products.mock';

const PRODUCT_CARDS = '.row.product-container > .product-card';

test.describe('Product listing page', () => {
  test('renders products in a real browser', async ({ page }) => {
    await setupApiMocks(page);
    await page.goto('/');

    const productCards = page.locator(PRODUCT_CARDS);
    await expect(productCards).toHaveCount(PRODUCTS_PAGE._embedded.products.length);
    await expect(productCards.first()).toContainText(/amazon comprehend/i);
    await expect(productCards.nth(1)).toContainText(/a-trust/i);
    await expect(page.locator('.not-found-result')).toHaveCount(0);
  });

  test('shows empty-state when no products are returned', async ({ page }) => {
    await setupEmptyProductsMock(page);
    await page.goto('/');

    await expect(page.locator(PRODUCT_CARDS)).toHaveCount(0);
    await expect(page.locator('.not-found-result')).toBeVisible();
  });
});
