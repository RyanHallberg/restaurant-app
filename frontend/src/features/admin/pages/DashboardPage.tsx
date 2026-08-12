import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import {
  adminListOptions,
  listItemsOptions,
} from '../../../api/generated/@tanstack/react-query.gen'
import { todayIso } from '../../reservations/schema'

export default function DashboardPage() {
  const todaysReservations = useQuery(
    adminListOptions({ query: { date: todayIso(), status: 'CONFIRMED', size: 1 } }),
  )
  const items = useQuery(listItemsOptions({ query: { size: 1 } }))

  return (
    <section>
      <h1 className="font-display text-3xl">Dashboard</h1>
      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <Link
          to="/admin/reservations"
          className="rounded-lg border border-charcoal/10 bg-white p-6 hover:border-charcoal/30"
        >
          <p className="text-3xl font-medium">{todaysReservations.data?.totalElements ?? '—'}</p>
          <p className="mt-1 text-sm text-charcoal/70">confirmed reservations today</p>
        </Link>
        <Link
          to="/admin/menu"
          className="rounded-lg border border-charcoal/10 bg-white p-6 hover:border-charcoal/30"
        >
          <p className="text-3xl font-medium">{items.data?.totalElements ?? '—'}</p>
          <p className="mt-1 text-sm text-charcoal/70">menu items (incl. hidden)</p>
        </Link>
      </div>
    </section>
  )
}
