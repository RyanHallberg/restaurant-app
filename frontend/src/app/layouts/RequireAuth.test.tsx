import { afterEach, describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { RouterProvider, createMemoryRouter } from 'react-router'
import RequireAuth from './RequireAuth'
import { useAuthStore } from '../../features/auth/authStore'

function renderGuarded(role?: 'ADMIN') {
  const router = createMemoryRouter(
    [
      { path: '/login', element: <p>login page</p> },
      { path: '/', element: <p>home page</p> },
      {
        element: <RequireAuth role={role} />,
        children: [{ path: '/secret', element: <p>secret content</p> }],
      },
    ],
    { initialEntries: ['/secret'] },
  )
  render(<RouterProvider router={router} />)
}

describe('RequireAuth', () => {
  afterEach(() => {
    useAuthStore.setState({ token: null, user: null })
  })

  it('redirects anonymous visitors to /login', () => {
    renderGuarded()
    expect(screen.getByText('login page')).toBeInTheDocument()
  })

  it('bounces non-admins away from admin routes', () => {
    useAuthStore.setState({
      token: 't',
      user: { id: 1, email: 'c@example.com', fullName: 'Customer', role: 'CUSTOMER' },
    })
    renderGuarded('ADMIN')
    expect(screen.getByText('home page')).toBeInTheDocument()
  })

  it('renders protected content for a matching role', () => {
    useAuthStore.setState({
      token: 't',
      user: { id: 2, email: 'a@example.com', fullName: 'Admin', role: 'ADMIN' },
    })
    renderGuarded('ADMIN')
    expect(screen.getByText('secret content')).toBeInTheDocument()
  })
})
