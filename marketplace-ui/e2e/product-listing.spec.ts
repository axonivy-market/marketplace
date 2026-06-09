import { expect, test } from '@playwright/test';
import { setupApiMocks, setupEmptyProductsMock } from './mock/api.mock';
import { PRODUCTS_PAGE } from './mock/products.mock';
import { ProductListingPage } from './page-objects/ProductListingPage';

test.describe('Product listing page', () => {
  test('renders products in a real browser', async ({ page }) => {
    await setupApiMocks(page);
    const listing = new ProductListingPage(page);
    await listing.goto();

    await listing.assertProductCount(PRODUCTS_PAGE._embedded.products.length);
    await listing.assertProductContainsText(0, /amazon comprehend/i);
    await listing.assertProductContainsText(1, /a-trust/i);
    expect(await listing.emptyState.isVisible()).toBe(false);
  });

  test('shows empty-state when no products are returned', async ({ page }) => {
    await setupEmptyProductsMock(page);
    const listing = new ProductListingPage(page);
    await listing.goto();

    await listing.assertProductCount(0);
    expect(await listing.emptyState.isVisible() ).toBe(true);
  });
});
