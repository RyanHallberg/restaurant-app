import { client } from './generated/client.gen'
import { useAuthStore } from '../features/auth/authStore'

// Dev: baseUrl stays empty so requests are same-origin (/api/...) and the Vite
// proxy forwards them to Spring on :8080. Prod: VITE_API_URL is the Cloud Run
// origin, baked in at build time.
client.setConfig({
  baseUrl: import.meta.env.VITE_API_URL ?? '',
})

client.interceptors.request.use((request) => {
  const token = useAuthStore.getState().token
  if (token) request.headers.set('Authorization', `Bearer ${token}`)
  return request
})

client.interceptors.response.use((response) => {
  // A 401 on a normal call means the token expired — reset and re-login.
  // Login/register 401s are handled by their forms, not globally.
  const isAuthCall = response.url.includes('/api/v1/auth/')
  if (response.status === 401 && !isAuthCall && useAuthStore.getState().token) {
    useAuthStore.getState().logout()
    window.location.assign('/login?expired=1')
  }
  return response
})
