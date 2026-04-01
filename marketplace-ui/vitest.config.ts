import { defineConfig } from 'vitest/config';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
  plugins: [
    angular({ tsconfig: './tsconfig.spec.json' })
  ],
  test: {
    environment: 'jsdom',
    globals: true,
    reporters: ['default'],
    setupFiles: ['src/test-setup.ts'],
    coverage: {
      provider: 'v8',
      reportsDirectory: './coverage',
      reporter: ['html', 'text-summary', 'lcov'],
      exclude: ['**/mocks/**', 'node_modules/**', 'dist/**', 'coverage/**']
    },
    watch: false,
    testTimeout: 60000,
    include: ['src/**/*.spec.{ts,js}']
  }
});
