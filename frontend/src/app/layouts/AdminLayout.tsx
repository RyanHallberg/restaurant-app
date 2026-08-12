import { NavLink, Outlet } from 'react-router'

function adminNavClass({ isActive }: { isActive: boolean }) {
  return isActive
    ? 'block rounded-md bg-sage-dark px-3 py-2 text-sm text-cream'
    : 'block rounded-md px-3 py-2 text-sm text-charcoal/70 hover:bg-charcoal/5'
}

export default function AdminLayout() {
  return (
    <div className="mx-auto grid max-w-6xl gap-8 px-4 py-10 md:grid-cols-[12rem_1fr]">
      <aside>
        <h2 className="px-3 text-xs font-medium uppercase tracking-wider text-charcoal/50">
          Admin
        </h2>
        <nav className="mt-2 space-y-1">
          <NavLink to="/admin" end className={adminNavClass}>
            Dashboard
          </NavLink>
          <NavLink to="/admin/menu" className={adminNavClass}>
            Menu
          </NavLink>
          <NavLink to="/admin/reservations" className={adminNavClass}>
            Reservations
          </NavLink>
          <NavLink to="/admin/orders" className={adminNavClass}>
            Orders
          </NavLink>
        </nav>
      </aside>
      <div className="min-w-0">
        <Outlet />
      </div>
    </div>
  )
}
