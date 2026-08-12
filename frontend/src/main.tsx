import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { RouterProvider } from 'react-router'
import './index.css'
import './api/client'
import { queryClient } from './app/queryClient'
import { router } from './app/router'
import { useAuthStore } from './features/auth/authStore'

// Any identity change (login or logout) must drop cached server data: the same
// query keys serve different results per role (e.g. the menu list includes
// hidden items for admins), so a stale cache would leak across sessions.
useAuthStore.subscribe((state, prev) => {
  if (state.token !== prev.token) queryClient.clear()
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <RouterProvider router={router} />
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  </StrictMode>,
)
