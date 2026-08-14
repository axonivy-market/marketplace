import { expect, type Locator, type Page } from '@playwright/test';

export class MonitoringPage {
  readonly page: Page;
  readonly locator: Locator;
  readonly searchInput: Locator;
  readonly defaultModeButton: Locator;
  readonly reportModeButton: Locator;
  readonly table: Locator;
  readonly tableRows: Locator;
  readonly pageSizeSelect: Locator;
  readonly pagination: Locator;

  constructor(page: Page) {
    this.page = page;
    this.locator = page.locator('app-monitor-dashboard');
    this.searchInput = this.locator.locator('input[type="text"].search-input');
    this.defaultModeButton = this.locator.locator('label[for^="default-mode-"]', { hasText: 'Default Mode' });
    this.reportModeButton = this.locator.locator('label[for^="report-mode-"]', { hasText: 'Report Mode' });
    this.table = this.locator.locator('app-monitor-repo .monitor-repo-wrapper > div.position-relative > table.table');
    this.tableRows = this.table.locator('tbody > tr');
    this.pageSizeSelect = this.locator.locator('select.form-select.pagination');
    this.pagination = this.locator.locator('ngb-pagination');
  }

  async goto() {
    await this.page.goto('/monitoring');
    await expect(this.locator).toBeVisible();
  }

  async search(text: string) {
    await this.searchInput.fill(text);
  }

  async selectDefaultMode() {
    await this.defaultModeButton.click();
  }

  async selectReportMode() {
    await this.reportModeButton.click();
  }

  async assertSearchBarVisible() {
    await expect(this.searchInput).toBeVisible();
  }

  async assertModeButtonsVisible() {
    await expect(this.defaultModeButton).toBeVisible();
    await expect(this.reportModeButton).toBeVisible();
  }

  async getTableRowCount(): Promise<number> {
    return this.tableRows.count();
  }

  getTableRow(index: number): Locator {
    return this.tableRows.nth(index);
  }

  async assertTableRowContainsText(index: number, text: string | RegExp) {
    await expect(this.getTableRow(index)).toContainText(text);
  }

  async assertTableRowHasLink(index: number, href: string | RegExp) {
    await expect(this.getTableRow(index).locator('p.name > a')).toHaveAttribute('href', href);
  }

  async assertAllTableRowsContainText(text: string | RegExp) {
    const rowCount = await this.getTableRowCount();
    for (let index = 0; index < rowCount; index += 1) {
      await expect(this.getTableRow(index)).toContainText(text);
    }
  }

  async assertTableHasRows(expected: number) {
    await expect(this.tableRows).toHaveCount(expected);
  }

  async selectPageSize(pageSize: 10 | 20 | 'all') {
    const optionIndex = pageSize === 10 ? 0 : pageSize === 20 ? 1 : 2;
    await this.pageSizeSelect.selectOption({ index: optionIndex });
  }

  async getPaginationPageNumberCount(): Promise<number> {
    return this.pagination
      .locator('li .page-link')
      .evaluateAll(
        links => links.map(link => link.textContent?.trim() ?? '').filter(text => /^\d+$/.test(text)).length
      );
  }

  async assertPaginationState(expectedRows: number, expectedPages: number) {
    await expect(this.tableRows).toHaveCount(expectedRows);
    await expect.poll(() => this.getPaginationPageNumberCount()).toBe(expectedPages);
  }
}
