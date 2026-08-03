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
  readonly downloadDialog: Locator;
  readonly artifactDropdown: Locator;
  readonly versionDropdown: Locator;
  readonly artifactDropdownButton: Locator;
  readonly versionDropdownButton: Locator;
  readonly artifactDropdownItems: Locator;
  readonly versionDropdownItems: Locator;
  readonly confirmDownloadButton: Locator;
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
    this.downloadDialog = this.root.locator('#download-dropdown-menu');
    this.artifactDropdown = this.downloadDialog.locator('#artifacts-selector app-common-dropdown');
    this.versionDropdown = this.downloadDialog.locator('#version-selector app-common-dropdown');
    this.artifactDropdownButton = this.artifactDropdown.locator('button');
    this.versionDropdownButton = this.versionDropdown.locator('button');
    this.artifactDropdownItems = this.artifactDropdown.locator('.dropdown-menu .dropdown-item');
    this.versionDropdownItems = this.versionDropdown.locator('.dropdown-menu .dropdown-item');
    this.confirmDownloadButton = this.downloadDialog.locator('#downloadButton');
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
    await expect(activePane).toBeVisible();
  }

  async assertDownloadButtonVisible() {
    await expect(this.downloadButton).toBeVisible();
    await expect(this.downloadButton).toContainText(/download/i);
  }

  async openDownloadDialog() {
    await this.downloadButton.click();
    await expect(this.downloadDialog).toBeVisible();
  }

  async assertDownloadDialogVisible() {
    await expect(this.downloadDialog).toBeVisible();
    await expect(this.artifactDropdownButton).toBeVisible();
    await expect(this.versionDropdownButton).toBeVisible();
    await expect(this.confirmDownloadButton).toBeVisible();
  }

  async getArtifactOptions(): Promise<string[]> {
    return (await this.artifactDropdownItems.allTextContents()).map(text => text.trim());
  }

  async getVersionOptions(): Promise<string[]> {
    return (await this.versionDropdownItems.allTextContents()).map(text => text.trim());
  }

  async assertArtifactOptions(expected: string[]) {
    await expect(this.artifactDropdownItems).toHaveText(expected);
  }

  async assertVersionOptions(expected: string[]) {
    await expect(this.versionDropdownItems).toHaveText(expected);
  }

  async selectVersion(version: string) {
    await this.versionDropdownButton.click();
    const versionItem = this.versionDropdownItems.filter({ hasText: version }).first();
    await expect(versionItem).toBeVisible();
    await versionItem.click();
  }

  async selectArtifact(artifactName: string) {
    await this.artifactDropdownButton.click();
    const artifactItem = this.artifactDropdownItems.filter({ hasText: artifactName }).first();
    await expect(artifactItem).toBeVisible();
    await artifactItem.click();
  }

  async clickDownloadArtifact() {
    await this.confirmDownloadButton.click();
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
