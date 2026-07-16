import { expect, type Locator, type Page } from '@playwright/test';

export class ProductDetailPage {
  readonly page: Page;
  readonly root: Locator;
  readonly descriptionPane: Locator;
  readonly descriptionTab: Locator;
  readonly downloadButton: Locator;
  readonly informationTab: Locator;

  constructor(page: Page) {
    this.page = page;
    this.root = page.locator('app-product-detail');
    this.descriptionPane = this.root.locator('#tab-pane-description');
    this.descriptionTab = this.root.locator('.nav-link', { hasText: /description/i });
    this.downloadButton = this.root.locator('#download-button');
    this.informationTab = this.root.locator('.info-tab app-product-detail-information-tab');
  }

  async goto(productId = 'smart-workflow') {
    await this.page.goto(`/${productId}`);
    await expect(this.root).toBeVisible();
  }

  async assertRedirectedToLatestVersion(version: string) {
    await expect(this.page).toHaveURL(
      new RegExp(`/smart-workflow\\?version=${version.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}#description$`)
    );
  }

  async assertDescriptionTabVisible() {
    await expect(this.descriptionTab).toBeVisible();
  }

  async assertDownloadButtonVisible() {
    await expect(this.downloadButton).toBeVisible();
    await expect(this.downloadButton).toContainText(/download/i);
  }

  async assertInformationTabVisible() {
    await expect(this.informationTab).toBeVisible();
    await expect(this.informationTab).toContainText(/implemented by/i);
  }
}
