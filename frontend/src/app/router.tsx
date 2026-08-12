import { createBrowserRouter } from 'react-router'
import PublicLayout from './layouts/PublicLayout'
import HomePage from '../pages/HomePage'
import AboutPage from '../pages/AboutPage'
import NotFoundPage from '../pages/NotFoundPage'
import MenuPage from '../features/menu/pages/MenuPage'

export type RouteHandle = { title?: string }

export const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [
      { path: '/', element: <HomePage /> },
      { path: '/menu', element: <MenuPage />, handle: { title: 'Menu' } satisfies RouteHandle },
      { path: '/about', element: <AboutPage />, handle: { title: 'About' } satisfies RouteHandle },
      {
        path: '*',
        element: <NotFoundPage />,
        handle: { title: 'Page not found' } satisfies RouteHandle,
      },
    ],
  },
])
