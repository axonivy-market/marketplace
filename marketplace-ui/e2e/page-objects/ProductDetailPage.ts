import { expect, type Locator, type Page } from '@playwright/test';

export class ProductDetailPage {
  readonly page: Page;
  readonly root: Locator;
  readonly descriptionPane: Locator;
  readonly descriptionTab: Locator;
  readonly demoPane: Locator;
  readonly demoTab: Locator;
  readonly setupPane: Locator;
  readonly setupTab: Locator;
  readonly downloadButton: Locator;
  readonly informationTab: Locator;

  constructor(page: Page) {
    this.page = page;
    this.root = page.locator('app-product-detail');
    this.descriptionPane = this.root.locator('#tab-pane-description');
    this.descriptionTab = this.root.getByRole('tab', { name: /^description$/i });
    this.demoPane = this.root.locator('#tab-pane-demo');
    this.demoTab = this.root.getByRole('tab', { name: /^demo$/i });
    this.setupPane = this.root.locator('#tab-pane-setup');
    this.setupTab = this.root.getByRole('tab', { name: /installation guide/i });
    this.downloadButton = this.root.locator('#download-button');
    this.informationTab = this.root.locator('.info-tab app-product-detail-information-tab');
  }

  async goto(productId = 'smart-workflow') {
    await this.page.goto(`/${productId}`);
    await expect(this.root).toBeVisible();
  }

  async assertRedirectedToVersion(version: string) {
    await expect(this.page).toHaveURL(
      new RegExp(`/smart-workflow\\?version=${version.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')}#description$`)
    );
  }

  async assertRedirectedToLatestVersion(version: string) {
    await this.assertRedirectedToVersion(version);
  }

  async assertDescriptionTabVisible() {
    await expect(this.descriptionTab).toBeVisible();
  }

  async assertDemoTabVisible() {
    await expect(this.demoTab).toBeVisible();
  }

  async assertSetupTabVisible() {
    await expect(this.setupTab).toBeVisible();
  }

  async clickTab(tab: Locator, activePane: Locator) {
    await tab.click({ force: true });
    await expect(tab).toHaveAttribute('aria-selected', 'true');
    await expect(activePane).toBeVisible();
  }

  async assertDownloadButtonVisible() {
    await expect(this.downloadButton).toBeVisible();
    await expect(this.downloadButton).toContainText(/download/i);
  }

  async assertInformationTabVisible() {
    await expect(this.informationTab).toBeVisible();
    await expect(this.informationTab).toContainText(/implemented by/i);
  }

  async waitForDetailLoaded() {
    await expect(this.root.locator('.tab-group')).toBeVisible();
    await expect(this.descriptionTab).toBeVisible();
  }
}
