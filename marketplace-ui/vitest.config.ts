import { defineConfig } from 'vitest/config';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
  plugins: [angular({ tsconfig: './tsconfig.spec.json' })],
  test: {
    environment: 'jsdom',
    globals: true,
    reporters: ['default'],
    setupFiles: ['src/test-setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'text-summary', 'html', 'lcov']
    },
    watch: false,
    testTimeout: 60000,
    restoreMocks: true,
    include: ['src/**/*.spec.{ts,js}']
  }
});
