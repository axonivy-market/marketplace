import { expect, test } from '@playwright/test';
import {
  setupProductDetailMocks,
  SMART_WORKFLOW_BEST_MATCH_VERSION
} from './mock/product-detail.mock';
import { ProductDetailPage } from './page-objects/ProductDetailPage';

test.describe('Product detail page', () => {
  test('redirects to the latest version and renders the key detail areas', async ({ page }) => {
    await setupProductDetailMocks(page);

    const detail = new ProductDetailPage(page);
    await detail.goto('smart-workflow');
    await detail.assertRedirectedToVersion(SMART_WORKFLOW_BEST_MATCH_VERSION);

    await detail.assertDescriptionTabVisible();
    await expect(detail.descriptionPane).toHaveClass(/active/);
    await expect(detail.descriptionPane).toContainText(/brings ai directly into axon ivy/i);
    await detail.assertDownloadButtonVisible();
    await detail.assertInformationTabVisible();
  });
});
