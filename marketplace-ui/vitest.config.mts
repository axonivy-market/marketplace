import { defineConfig } from 'vitest/config';
import angular from '@analogjs/vite-plugin-angular';

export default defineConfig({
  plugins: [angular({ tsconfig: './tsconfig.spec.json' })],
  optimizeDeps: {
    entries: ['src/index.html']
  },
  test: {
    environment: 'jsdom',
    globals: true,
    reporters: ['default'],
    setupFiles: ['src/test-setup.ts'],
    coverage: {
      provider: 'v8',
      reporter: ['text', 'text-summary', 'html', 'lcov'],
      exclude: ['**/*.html', '**/*.scss']
    },
    watch: false,
    testTimeout: 60000,
    restoreMocks: true
  }
});
