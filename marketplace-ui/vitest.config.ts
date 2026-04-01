import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    environment: 'jsdom', // Similar to browser env in Karma ChromeHeadless.
    globals: true, // Like Jasmine global APIs.
    reporters: ['default', 'html', 'lcov'],
    coverage: {
      provider: 'v8',
      reportsDirectory: './coverage',
      reporter: ['html', 'text-summary', 'lcov'],
      // --- Karma only covered "src/**/mocks/**" which is unusual,
      // but in Vitest you typically exclude, not include:
      // Exclude mocks from coverage instead of including only them
      exclude: ['**/mocks/**', 'node_modules/**', 'dist/**', 'coverage/**']
    },
    watch: true, // restartOnFileChange: true in Karma
    // Equivalent to Jasmine's timeoutInterval:
    testTimeout: 60000,
    // failFast: true is handled differently; see below.
    // random order: Vitest does not randomize by default (like your random: false)
    // stopSpecOnExpectationFailure: true - Not natively supported, but failFast can be approximated:
    // use --bail CLI flag or bail: 1 here for fail-fast
    bail: 1,
    // To clear output between runs: Vitest does so by default in terminal. For browser see docs.
    // Viewport: There's no direct viewport plugin; use jsdom or Playwright if you need deeper viewport simulation.
    // Headless: 'jsdom' environment is always headless.
    // frameworks: ['jasmine'] - Jasmine-style APIs are globally supported with globals: true
    include: ['src/**/*.spec.{ts,js}'] // Typical Angular spec file pattern
  }
});
