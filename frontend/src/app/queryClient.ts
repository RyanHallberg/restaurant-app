import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // Menu data changes rarely; avoid refetch storms on tab focus.
      staleTime: 30_000,
      retry: 1,
    },
  },
})
