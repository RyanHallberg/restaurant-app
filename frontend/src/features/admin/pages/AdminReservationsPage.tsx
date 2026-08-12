import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  adminListOptions,
  updateStatusMutation,
} from '../../../api/generated/@tanstack/react-query.gen'
import { todayIso } from '../../reservations/schema'
import Spinner from '../../../components/ui/Spinner'
import ErrorBanner from '../../../components/ui/ErrorBanner'
import EmptyState from '../../../components/ui/EmptyState'
import Input from '../../../components/ui/Input'
import Select from '../../../components/ui/Select'

const STATUSES = ['CONFIRMED', 'CANCELLED', 'COMPLETED'] as const

export default function AdminReservationsPage() {
  const queryClient = useQueryClient()
  const [date, setDate] = useState(todayIso())
  const [status, setStatus] = useState('')
  const [actionError, setActionError] = useState<string | null>(null)

  const reservations = useQuery(
    adminListOptions({
      query: {
        date: date || undefined,
        status: (status || undefined) as (typeof STATUSES)[number] | undefined,
        size: 50,
      },
    }),
  )

  const updateStatus = useMutation({
    ...updateStatusMutation(),
    onSuccess: () => {
      setActionError(null)
      void queryClient.invalidateQueries({ queryKey: [{ _id: 'adminList' }] })
      void queryClient.invalidateQueries({ queryKey: [{ _id: 'availability' }] })
    },
    onError: (error) => {
      setActionError(
        typeof error === 'object' && error !== null && 'detail' in error
          ? String((error as { detail: unknown }).detail)
          : 'That change failed. Please try again.',
      )
    },
  })

  const rows = reservations.data?.content ?? []

  return (
    <section>
      <h1 className="font-display text-3xl">Reservations</h1>

      <div className="mt-6 grid max-w-md grid-cols-2 gap-4">
        <Input
          label="Date"
          type="date"
          name="filter-date"
          value={date}
          onChange={(event) => setDate(event.target.value)}
        />
        <Select
          label="Status"
          name="filter-status"
          value={status}
          onChange={(event) => setStatus(event.target.value)}
        >
          <option value="">All statuses</option>
          {STATUSES.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </Select>
      </div>

      {actionError && (
        <p role="alert" className="mt-4 rounded-md bg-ember/10 p-3 text-sm text-ember">
          {actionError}
        </p>
      )}

      <div className="mt-6">
        {reservations.data ? (
          rows.length === 0 ? (
            <EmptyState message="No reservations match these filters." />
          ) : (
            <div className="overflow-x-auto rounded-lg border border-charcoal/10 bg-white">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-charcoal/10 text-charcoal/70">
                  <tr>
                    <th className="px-4 py-3 font-medium">When</th>
                    <th className="px-4 py-3 font-medium">Guest</th>
                    <th className="px-4 py-3 font-medium">Party</th>
                    <th className="px-4 py-3 font-medium">Code</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                    <th className="px-4 py-3 font-medium">
                      <span className="sr-only">Actions</span>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((reservation) => (
                    <tr key={reservation.id} className="border-b border-charcoal/5 last:border-0">
                      <td className="px-4 py-3">
                        {reservation.date} · {String(reservation.time).slice(0, 5)}
                      </td>
                      <td className="px-4 py-3">{reservation.customerName}</td>
                      <td className="px-4 py-3">{reservation.partySize}</td>
                      <td className="font-mono px-4 py-3 text-xs">
                        {reservation.confirmationCode}
                      </td>
                      <td className="px-4 py-3">{reservation.status}</td>
                      <td className="px-4 py-3">
                        {reservation.status === 'CONFIRMED' && reservation.id != null && (
                          <div className="flex justify-end gap-3 text-xs">
                            <button
                              type="button"
                              disabled={updateStatus.isPending}
                              onClick={() =>
                                updateStatus.mutate({
                                  path: { id: reservation.id! },
                                  body: { status: 'COMPLETED' },
                                })
                              }
                              className="text-sage-dark hover:underline disabled:opacity-50"
                              aria-label={`Mark ${reservation.customerName}'s reservation completed`}
                            >
                              Complete
                            </button>
                            <button
                              type="button"
                              disabled={updateStatus.isPending}
                              onClick={() => {
                                if (
                                  window.confirm(
                                    `Cancel ${reservation.customerName}'s reservation? This can't be undone.`,
                                  )
                                ) {
                                  updateStatus.mutate({
                                    path: { id: reservation.id! },
                                    body: { status: 'CANCELLED' },
                                  })
                                }
                              }}
                              className="text-ember hover:underline disabled:opacity-50"
                              aria-label={`Cancel ${reservation.customerName}'s reservation`}
                            >
                              Cancel
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
        ) : reservations.isPending ? (
          <Spinner label="Loading reservations…" />
        ) : (
          <ErrorBanner
            message="Couldn't load reservations."
            onRetry={() => void reservations.refetch()}
          />
        )}
      </div>
    </section>
  )
}
