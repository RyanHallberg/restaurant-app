import { client } from './generated/client.gen'

// Dev: baseUrl stays empty so requests are same-origin (/api/...) and the Vite
// proxy forwards them to Spring on :8080. Prod: VITE_API_URL is the Cloud Run
// origin, baked in at build time. Auth-header injection lands in M6.
client.setConfig({
  baseUrl: import.meta.env.VITE_API_URL ?? '',
})
