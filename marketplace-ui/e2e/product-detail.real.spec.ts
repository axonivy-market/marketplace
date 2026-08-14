import { expect, test } from '@playwright/test';
import { ProductDetailPage } from './page-objects/ProductDetailPage';

test.describe('@real-endpoint Product detail page', () => {
  test.skip(
    !process.env['E2E_REAL_ENDPOINT'],
    'Set E2E_REAL_ENDPOINT=true to run against a real endpoint.'
  );

  test('loads product detail from a real endpoint and downloads the selected artifact', async ({ page }) => {
    const detail = new ProductDetailPage(page);
    await detail.goto('smart-workflow');

    await detail.waitForDetailLoaded();
    await detail.assertDownloadButtonVisible();

    await detail.openDownloadDialog();
    await detail.assertDownloadDialogVisible();

    await expect.poll(async () => await detail.versionDropdownItems.count()).toBeGreaterThan(0);
    const versionOptions = await detail.getVersionOptions();
    expect(versionOptions.length).toBeGreaterThan(0);
    test.skip(
      versionOptions.length < 2,
      'Real endpoint must expose at least 2 versions to verify artifact switching.'
    );

    await expect.poll(async () => await detail.artifactDropdownItems.count()).toBeGreaterThan(0);
    const initialArtifacts = await detail.getArtifactOptions();
    expect(initialArtifacts.length).toBeGreaterThan(0);

    const currentVersion = (await detail.versionDropdownButton.textContent())?.trim() ?? '';
    const selectedVersion = versionOptions.find(version => version !== currentVersion) ?? versionOptions[1];
    await detail.selectVersion(selectedVersion);

    await expect.poll(async () => await detail.versionDropdownButton.textContent()).toContain(selectedVersion);

    const selectedArtifact = initialArtifacts[0];
    await detail.selectArtifact(selectedArtifact);

    const downloadRequestPromise = page.waitForRequest(request => {
      const url = request.url();
      return (
        request.method() === 'GET' &&
        url.includes('/api/product-marketplace-data/smart-workflow/') &&
        url.includes(selectedVersion.replace(/^Version\s+/, ''))
      );
    });

    await detail.clickDownloadArtifact();

    const downloadRequest = await downloadRequestPromise;
    expect(downloadRequest.method()).toBe('GET');
    expect(downloadRequest.url()).toContain('/api/product-marketplace-data/smart-workflow/');
    expect(downloadRequest.url()).toContain(selectedVersion.replace(/^Version\s+/, ''));
  });
});
