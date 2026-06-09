import { defineConfig, devices } from '@playwright/test';

const isCI = !!process.env['CI'];
const localBaseURL = isCI ? 'http://127.0.0.1:4000' : 'http://127.0.0.1:4200';
const baseURL = process.env['E2E_BASE_URL'] ?? localBaseURL;
const useExternalBaseURL = !!process.env['E2E_BASE_URL'];

export default defineConfig({
  testDir: './e2e',
  fullyParallel: true,
  forbidOnly: isCI,
  retries: isCI ? 2 : 0,
  workers: isCI ? 1 : undefined,
  reporter: [['html', { open: 'never' }]],
  use: {
    baseURL,
    trace: isCI ? 'retain-on-failure' : 'on-first-retry',
    screenshot: isCI ? 'only-on-failure' : 'off',
    video: isCI ? 'retain-on-failure' : 'off'
  },
  webServer: useExternalBaseURL
    ? undefined
    : {
      command: isCI ? 'npm run serve:ssr:marketplace-ui' : 'npm run start -- --host 127.0.0.1 --port 4200',
      url: localBaseURL,
      reuseExistingServer: !isCI,
      timeout: 120000
    },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ]
});
