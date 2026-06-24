import { expect, type Locator, type Page } from '@playwright/test';

export class ProductListingPage {
  readonly page: Page;
  readonly locator: Locator;
  readonly productCards: Locator;
  readonly emptyState: Locator;

  constructor(page: Page) {
    this.page = page;
    this.locator = page.locator('[data-testid="product-listing"], app-product');
    this.productCards = this.locator.locator('.row.product-container > .product-card');
    this.emptyState = this.locator.locator('.not-found-result');
  }

  async goto() {
    await this.page.goto('/');
    await expect(this.locator).toBeVisible();
  }

  async getProductCount(): Promise<number> {
    return this.productCards.count();
  }

  getProductCard(index: number): Locator {
    return this.productCards.nth(index);
  }

  async clickProductCard(index: number) {
    await this.getProductCard(index).click();
  }

  async assertProductCount(expected: number) {
    await expect(this.productCards).toHaveCount(expected);
  }

  async assertProductContainsText(index: number, text: string | RegExp) {
    await expect(this.getProductCard(index)).toContainText(text);
  }
}
