import { defineConfig } from '@hey-api/openapi-ts'

// The backend's springdoc spec is the single source of truth for API types.
// Regenerate with `npm run generate:api` (backend must be running on :8080).
export default defineConfig({
  input: 'http://localhost:8080/v3/api-docs',
  output: 'src/api/generated',
  plugins: ['@hey-api/client-fetch', '@tanstack/react-query'],
})
