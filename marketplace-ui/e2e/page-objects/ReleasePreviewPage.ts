import { expect, type Locator, type Page } from '@playwright/test';

export class ReleasePreviewPage {
  readonly page: Page;
  readonly root: Locator;
  readonly fileInput: Locator;
  readonly descriptionTab: Locator;
  readonly demoTab: Locator;
  readonly setupTab: Locator;
  readonly descriptionPane: Locator;
  readonly demoPane: Locator;
  readonly setupPane: Locator;

  constructor(page: Page) {
    this.page = page;
    this.root = page.locator('app-release-preview');
    this.fileInput = page.locator('#file-input');
    this.descriptionTab = this.root.getByRole('tab', { name: /description/i });
    this.demoTab = this.root.getByRole('tab', { name: /demo/i });
    this.setupTab = this.root.getByRole('tab', { name: /installation guide/i });
    this.descriptionPane = this.root.locator('#description');
    this.demoPane = this.root.locator('#demo');
    this.setupPane = this.root.locator('#setup');
  }

  async goto() {
    await this.page.goto('/release-preview');
    await expect(this.root).toBeVisible();
  }

  async uploadFixture(zipPath: string) {
    await this.fileInput.setInputFiles(zipPath);
  }

  async assertTabsVisible() {
    await expect(this.descriptionTab).toBeVisible();
    await expect(this.demoTab).toBeVisible();
    await expect(this.setupTab).toBeVisible();
  }

  async assertDescriptionContent() {
    await expect(this.descriptionPane).toContainText(/asana connector/i);
    await expect(this.descriptionPane).toContainText(/create a task/i);
    await expect(this.descriptionPane).toContainText(/delete a task/i);
  }

  async assertDemoContent() {
    await expect(this.demoPane).toContainText(/this demo provides the following features/i);
    await expect(this.demoPane).toContainText(/task list/i);
    await expect(this.demoPane).toContainText(/update task/i);
  }

  async assertSetupContent() {
    await expect(this.setupPane).toContainText(/in order to use this product/i);
    await expect(this.setupPane).toContainText(/asana registration/i);
    await expect(this.setupPane).toContainText(/workspace gid/i);
  }
}
