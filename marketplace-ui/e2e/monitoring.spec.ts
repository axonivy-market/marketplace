import { expect, test } from '@playwright/test';
import { MonitoringPage } from './page-objects/MonitoringPage';
import { MONITORING_REPOSITORIES, setupMonitoringApiMocks } from './mock/monitoring.mock';

test.describe('Monitoring page', () => {
  test('renders mocked repositories and receives a 200 response', async ({ page }) => {
    await setupMonitoringApiMocks(page);

    const responsePromise = page.waitForResponse(
      response => response.url().includes('/api/monitor-dashboard') && response.status() === 200
    );

    const monitoring = new MonitoringPage(page);
    await monitoring.goto();

    const response = await responsePromise;
    expect(response.ok()).toBe(true);

    await monitoring.assertSearchBarVisible();
    await monitoring.assertModeButtonsVisible();
    await monitoring.assertPaginationState(10, 3);
    await monitoring.assertTableRowContainsText(0, /smart-workflow/i);
    await monitoring.assertTableRowHasLink(0, 'https://market.axonivy.com/smart-workflow');
    await monitoring.assertTableRowContainsText(1, /persistence-utils/i);
    await monitoring.assertTableRowHasLink(1, 'https://market.axonivy.com/persistence-utils');

    await monitoring.selectPageSize(20);
    await monitoring.assertPaginationState(20, 2);
    await monitoring.assertTableRowContainsText(0, /smart-workflow/i);
    await monitoring.assertTableRowContainsText(19, /repo-20/i);

    await monitoring.selectPageSize('all');
    await monitoring.assertPaginationState(MONITORING_REPOSITORIES.length, 1);
    await monitoring.assertTableRowContainsText(24, /repo-25/i);
  });

  test('filters repositories when searching for smart', async ({ page }) => {
    await setupMonitoringApiMocks(page);

    const monitoring = new MonitoringPage(page);
    await monitoring.goto();
    await monitoring.assertSearchBarVisible();

    const responsePromise = page.waitForResponse(response => {
      return response.url().includes('/api/monitor-dashboard/repos') && response.url().includes('search=smart');
    });

    await monitoring.search('smart');
    await responsePromise;

    await monitoring.assertTableHasRows(1);
    await monitoring.assertAllTableRowsContainText(/smart/i);
  });
});
