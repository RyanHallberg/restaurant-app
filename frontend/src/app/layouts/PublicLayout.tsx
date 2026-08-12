import { useEffect } from 'react'
import { NavLink, Outlet, ScrollRestoration, useMatches } from 'react-router'
import type { RouteHandle } from '../router'

function navClass({ isActive }: { isActive: boolean }) {
  return isActive
    ? 'text-ember font-medium'
    : 'text-charcoal/70 hover:text-charcoal transition-colors'
}

export default function PublicLayout() {
  const matches = useMatches()
  const title = matches
    .map((match) => (match.handle as RouteHandle | undefined)?.title)
    .filter(Boolean)
    .at(-1)

  useEffect(() => {
    document.title = title ? `${title} · Sage & Ember` : 'Sage & Ember'
  }, [title])

  return (
    <div className="flex min-h-screen flex-col">
      <header className="sticky top-0 z-10 border-b border-charcoal/10 bg-cream/90 backdrop-blur">
        <div className="mx-auto flex max-w-5xl items-center justify-between px-4 py-4">
          <NavLink to="/" className="font-display text-2xl tracking-tight">
            Sage &amp; Ember
          </NavLink>
          <nav className="flex items-center gap-6 text-sm">
            <NavLink to="/" className={navClass} end>
              Home
            </NavLink>
            <NavLink to="/menu" className={navClass}>
              Menu
            </NavLink>
            <NavLink to="/reserve" className={navClass}>
              Reserve
            </NavLink>
            <NavLink to="/about" className={navClass}>
              About
            </NavLink>
          </nav>
        </div>
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t border-charcoal/10 bg-charcoal text-cream/80">
        <div className="mx-auto grid max-w-5xl gap-6 px-4 py-10 sm:grid-cols-3">
          <div>
            <p className="font-display text-lg text-cream">Sage &amp; Ember</p>
            <p className="mt-2 text-sm">Wood-fired seasonal cooking.</p>
          </div>
          <div className="text-sm">
            <h2 className="font-medium text-cream">Hours</h2>
            <p className="mt-2">Tue–Sun · 11:00–22:00</p>
            <p>Closed Mondays</p>
          </div>
          <div className="text-sm">
            <h2 className="font-medium text-cream">Find us</h2>
            <p className="mt-2">418 Garland Ave</p>
            <p>(555) 014-2276</p>
          </div>
        </div>
      </footer>

      <ScrollRestoration />
    </div>
  )
}
