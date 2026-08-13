import { expect, test } from '@playwright/test';
import path from 'node:path';
import { RELEASE_PREVIEW_FIXTURE_ZIP } from './mock/release-preview.mock';
import { ReleasePreviewPage } from './page-objects/ReleasePreviewPage';

test.describe('@real-endpoint Release preview page', () => {
  test.skip(
    !process.env.E2E_REAL_ENDPOINT,
    'Set E2E_REAL_ENDPOINT=true to run against a real endpoint.'
  );

  test('uploads the Asana ZIP and renders tabs from the real endpoint response', async ({ page }) => {
    const previewResponsePromise = page.waitForResponse(
      response =>
        response.request().method() === 'POST' &&
        response.url().includes('/api/release-preview')
    );

    const preview = new ReleasePreviewPage(page);
    await preview.goto();
    await preview.uploadFixture(path.resolve(RELEASE_PREVIEW_FIXTURE_ZIP));

    const previewResponse = await previewResponsePromise;
    expect(previewResponse.ok()).toBe(true);

    await preview.assertTabsVisible();
    await preview.assertDescriptionContent();

    await preview.demoTab.click();
    await expect(preview.demoPane).toHaveClass(/active/);
    await preview.assertDemoContent();

    await preview.setupTab.click();
    await expect(preview.setupPane).toHaveClass(/active/);
    await preview.assertSetupContent();
  });
});
