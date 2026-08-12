import { createBrowserRouter } from 'react-router'
import PublicLayout from './layouts/PublicLayout'
import RequireAuth from './layouts/RequireAuth'
import AdminLayout from './layouts/AdminLayout'
import HomePage from '../pages/HomePage'
import AboutPage from '../pages/AboutPage'
import NotFoundPage from '../pages/NotFoundPage'
import MenuPage from '../features/menu/pages/MenuPage'
import ReservationPage from '../features/reservations/pages/ReservationPage'
import LoginPage from '../features/auth/pages/LoginPage'
import RegisterPage from '../features/auth/pages/RegisterPage'
import DashboardPage from '../features/admin/pages/DashboardPage'
import AdminMenuPage from '../features/admin/pages/AdminMenuPage'
import AdminReservationsPage from '../features/admin/pages/AdminReservationsPage'
import AdminOrdersPage from '../features/admin/pages/AdminOrdersPage'
import CartPage from '../features/ordering/pages/CartPage'
import CheckoutPage from '../features/ordering/pages/CheckoutPage'
import OrderStatusPage from '../features/ordering/pages/OrderStatusPage'

export type RouteHandle = { title?: string }

export const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/menu', element: <MenuPage />, handle: { title: 'Menu' } satisfies RouteHandle },
      {
        path: '/reserve',
        element: <ReservationPage />,
        handle: { title: 'Reserve a table' } satisfies RouteHandle,
      },
      {
        path: '/login',
        element: <LoginPage />,
        handle: { title: 'Sign in' } satisfies RouteHandle,
      },
      {
        path: '/register',
        element: <RegisterPage />,
        handle: { title: 'Create account' } satisfies RouteHandle,
      },
      { path: '/about', element: <AboutPage />, handle: { title: 'About' } satisfies RouteHandle },
      { path: '/cart', element: <CartPage />, handle: { title: 'Cart' } satisfies RouteHandle },
      {
        element: <RequireAuth />,
        children: [
          {
            path: '/checkout',
            element: <CheckoutPage />,
            handle: { title: 'Checkout' } satisfies RouteHandle,
          },
          {
            path: '/orders/:orderId',
            element: <OrderStatusPage />,
            handle: { title: 'Your order' } satisfies RouteHandle,
          },
        ],
      },
      {
        element: <RequireAuth role="ADMIN" />,
        children: [
          {
            element: <AdminLayout />,
            children: [
              {
                path: '/admin',
                element: <DashboardPage />,
                handle: { title: 'Admin' } satisfies RouteHandle,
              },
              {
                path: '/admin/menu',
                element: <AdminMenuPage />,
                handle: { title: 'Admin · Menu' } satisfies RouteHandle,
              },
              {
                path: '/admin/reservations',
                element: <AdminReservationsPage />,
                handle: { title: 'Admin · Reservations' } satisfies RouteHandle,
              },
              {
                path: '/admin/orders',
                element: <AdminOrdersPage />,
                handle: { title: 'Admin · Orders' } satisfies RouteHandle,
              },
            ],
          },
        ],
      },
      {
        path: '*',
        element: <NotFoundPage />,
        handle: { title: 'Page not found' } satisfies RouteHandle,
      },
    ],
  },
])
