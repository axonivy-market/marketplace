import { expect, test } from '@playwright/test';
import { MonitoringPage } from './page-objects/MonitoringPage';

test.describe('@real-endpoint Monitoring page', () => {
  test.skip(
    !process.env.E2E_REAL_ENDPOINT,
    'Set E2E_REAL_ENDPOINT=true to run against a real endpoint.'
  );

  test('loads from real endpoint and renders a valid state', async ({ page }) => {
    const responsePromise = page.waitForResponse(response => {
      return (
        response.request().method() === 'GET' &&
        response.url().includes('/api/monitor-dashboard/repos') &&
        response.status() === 200
      );
    });

    const monitoring = new MonitoringPage(page);
    await monitoring.goto();

    const response = await responsePromise;
    expect(response.ok()).toBe(true);

    await expect(page).toHaveTitle(/Monitor Dashboard/i);
    await monitoring.assertSearchBarVisible();
    await monitoring.assertModeButtonsVisible();
    await expect(monitoring.locator.locator('table.table')).toBeVisible();

    const repositoryLinkCount = await monitoring.locator.locator('p.name > a').count();
    if (repositoryLinkCount > 0) {
      await expect(monitoring.getTableRow(0)).toBeVisible();
      await monitoring.assertTableRowHasLink(0, /^https:\/\/market\.axonivy\.com\//);
    } else {
      await expect(monitoring.locator.locator('.no-repositories')).toBeVisible();
    }
  });
});
