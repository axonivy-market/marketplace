import { expect, test } from '@playwright/test';
import path from 'node:path';
import { setupReleasePreviewMock, RELEASE_PREVIEW_FIXTURE_ZIP } from './mock/release-preview.mock';
import { ReleasePreviewPage } from './page-objects/ReleasePreviewPage';

test.describe('Release preview page', () => {
  test('uploads the Asana ZIP and renders description, demo, and installation guide tabs', async ({ page }) => {
    await setupReleasePreviewMock(page);

    const preview = new ReleasePreviewPage(page);
    await preview.goto();
    await preview.uploadFixture(path.resolve(RELEASE_PREVIEW_FIXTURE_ZIP));

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
