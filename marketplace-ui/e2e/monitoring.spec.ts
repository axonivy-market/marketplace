import { expect, test } from '@playwright/test';

const MARKET_MONITOR_URL = 'https://axonivy-market.github.io/market-monitor/';

test.describe('Monitoring page', () => {
  test('redirects /monitoring to the market monitor', async ({ page }) => {
    await page.route(`${MARKET_MONITOR_URL}**`, route =>
      route.fulfill({ status: 200, contentType: 'text/html', body: '<title>Action Monitor</title>' })
    );

    await page.goto('/monitoring');

    await expect(page).toHaveURL(MARKET_MONITOR_URL);
  });
});
