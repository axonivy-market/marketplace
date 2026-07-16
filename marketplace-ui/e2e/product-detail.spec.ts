import { expect, test } from '@playwright/test';
import { setupProductDetailMocks, SMART_WORKFLOW_LATEST_VERSION } from './mock/product-detail.mock';
import { ProductDetailPage } from './page-objects/ProductDetailPage';

test.describe('Product detail page', () => {
  test('redirects to the latest version and renders the key detail areas', async ({ page }) => {
    await setupProductDetailMocks(page);

    const detail = new ProductDetailPage(page);
    await detail.goto('smart-workflow');
    await detail.assertRedirectedToLatestVersion(SMART_WORKFLOW_LATEST_VERSION);

    await detail.assertDescriptionTabVisible();
    await expect(detail.descriptionPane).toContainText(/smart workflow description/i);
    await detail.assertDownloadButtonVisible();
    await detail.assertInformationTabVisible();
  });
});
