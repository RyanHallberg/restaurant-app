import { Navigate, Outlet, useLocation } from 'react-router'
import { useAuthStore } from '../../features/auth/authStore'

/**
 * UX-only gating — the backend enforces the real authorization on every
 * request. Unauthenticated users bounce to /login and return here after.
 */
export default function RequireAuth({ role }: { role?: 'ADMIN' }) {
  const user = useAuthStore((state) => state.user)
  const location = useLocation()

  if (!user) return <Navigate to="/login" state={{ from: location.pathname }} replace />
  if (role && user.role !== role) return <Navigate to="/" replace />
  return <Outlet />
}
