import { defineConfig } from '@playwright/test'

// Smoke journeys against the docker-compose full-stack profile:
//   docker compose --profile full up -d --build && npm run e2e
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  retries: process.env.CI ? 1 : 0,
  reporter: 'list',
  use: {
    baseURL: process.env.E2E_BASE_URL ?? 'http://localhost:8081',
    trace: 'retain-on-failure',
  },
})
