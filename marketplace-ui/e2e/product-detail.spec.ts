import { expect, test } from '@playwright/test';
import {
  SMART_WORKFLOW_DESCRIPTION_EXPECTED,
  SMART_WORKFLOW_DEMO_EXPECTED,
  SMART_WORKFLOW_BEST_MATCH_VERSION,
  SMART_WORKFLOW_SETUP_EXPECTED,
  setupProductDetailMocks
} from './mock/product-detail.mock';
import { ProductDetailPage } from './page-objects/ProductDetailPage';

test.describe('Product detail page', () => {
  test('redirects to the latest version and renders the key detail areas', async ({ page }) => {
    await setupProductDetailMocks(page);

    const detail = new ProductDetailPage(page);
    await detail.goto('smart-workflow');
    await detail.assertRedirectedToVersion(SMART_WORKFLOW_BEST_MATCH_VERSION);
    await detail.assertDownloadButtonVisible();
    await detail.assertInformationTabVisible();
    await detail.waitForDetailLoaded();

    await detail.assertDescriptionTabVisible();
    await expect(detail.descriptionPane).toContainText(SMART_WORKFLOW_DESCRIPTION_EXPECTED[1]);
    await detail.clickTab(detail.descriptionTab, detail.descriptionPane);
    for (const expectedText of SMART_WORKFLOW_DESCRIPTION_EXPECTED) {
      await expect(detail.descriptionPane).toContainText(expectedText);
    }

    await detail.assertDemoTabVisible();
    await detail.clickTab(detail.demoTab, detail.demoPane);
    for (const expectedText of SMART_WORKFLOW_DEMO_EXPECTED) {
      await expect(detail.demoPane).toContainText(expectedText);
    }

    await detail.assertSetupTabVisible();
    await detail.clickTab(detail.setupTab, detail.setupPane);
    for (const expectedText of SMART_WORKFLOW_SETUP_EXPECTED) {
      await expect(detail.setupPane).toContainText(expectedText);
    }
  });
});
