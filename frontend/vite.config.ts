/// <reference types="vitest/config" />
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Same-origin API calls in dev: the SPA hits /api/... and Vite forwards to
    // Spring on :8080, so no CORS config is needed locally.
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
  test: {
    environment: 'jsdom',
    globals: true,
    setupFiles: ['./src/test/setup.ts'],
    // e2e specs belong to Playwright, not Vitest.
    exclude: ['node_modules/**', 'dist/**', 'e2e/**'],
  },
})
