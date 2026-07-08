import { expect, test } from '@playwright/test';
import { ProductListingPage } from './page-objects/ProductListingPage';

test.describe('@real-endpoint Product listing page', () => {
  test.skip(
    !process.env.E2E_REAL_ENDPOINT,
    'Set E2E_REAL_ENDPOINT=true to run against a real endpoint.'
  );

  test('loads from real endpoint and renders a valid state', async ({ page }) => {
    const productResponsePromise = page.waitForResponse(
      response =>
        response.request().method() === 'GET' &&
        response.url().includes('/api/product')
    );

    const listing = new ProductListingPage(page);
    await listing.goto();

    const productResponse = await productResponsePromise;
    expect(productResponse.ok()).toBe(true);

    const productCount = await listing.getProductCount();
    if (productCount > 0) {
      await expect(listing.getProductCard(0)).toBeVisible();
      await expect(listing.emptyState).toHaveCount(0);
    } else {
      await expect(listing.emptyState).toBeVisible();
    }
  });
});
