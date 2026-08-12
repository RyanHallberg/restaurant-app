import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  listOrdersOptions,
  updateOrderStatusMutation,
} from '../../../api/generated/@tanstack/react-query.gen'
import { formatCurrency } from '../../../lib/formatCurrency'
import Spinner from '../../../components/ui/Spinner'
import ErrorBanner from '../../../components/ui/ErrorBanner'
import EmptyState from '../../../components/ui/EmptyState'
import Select from '../../../components/ui/Select'

const STATUSES = ['PLACED', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED'] as const
type Status = (typeof STATUSES)[number]

const NEXT_ACTION: Partial<Record<Status, { label: string; target: Status }>> = {
  PLACED: { label: 'Start preparing', target: 'PREPARING' },
  PREPARING: { label: 'Mark ready', target: 'READY' },
  READY: { label: 'Complete', target: 'COMPLETED' },
}

const CANCELLABLE: ReadonlySet<Status> = new Set(['PLACED', 'PREPARING'])

export default function AdminOrdersPage() {
  const queryClient = useQueryClient()
  const [status, setStatus] = useState('')
  const [actionError, setActionError] = useState<string | null>(null)

  const orders = useQuery(
    listOrdersOptions({ query: { status: (status || undefined) as Status | undefined, size: 50 } }),
  )

  const updateStatus = useMutation({
    ...updateOrderStatusMutation(),
    onSuccess: () => {
      setActionError(null)
      void queryClient.invalidateQueries({ queryKey: [{ _id: 'listOrders' }] })
      void queryClient.invalidateQueries({ queryKey: [{ _id: 'getOrder' }] })
    },
    onError: (error) => {
      setActionError(
        typeof error === 'object' && error !== null && 'detail' in error
          ? String((error as { detail: unknown }).detail)
          : 'That change failed. Please try again.',
      )
    },
  })

  const rows = orders.data?.content ?? []
  // Disable only the row being mutated, not every action on the board.
  const pendingId = updateStatus.isPending ? updateStatus.variables?.path.id : undefined

  return (
    <section>
      <h1 className="font-display text-3xl">Orders</h1>

      <div className="mt-6 max-w-48">
        <Select
          label="Status"
          name="filter-order-status"
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
        {orders.data ? (
          rows.length === 0 ? (
            <EmptyState message="No orders match this filter." />
          ) : (
            <div className="overflow-x-auto rounded-lg border border-charcoal/10 bg-white">
              <table className="w-full text-left text-sm">
                <thead className="border-b border-charcoal/10 text-charcoal/70">
                  <tr>
                    <th scope="col" className="px-4 py-3 font-medium">
                      Order
                    </th>
                    <th scope="col" className="px-4 py-3 font-medium">
                      Placed
                    </th>
                    <th scope="col" className="px-4 py-3 font-medium">
                      Items
                    </th>
                    <th scope="col" className="px-4 py-3 font-medium">
                      Total
                    </th>
                    <th scope="col" className="px-4 py-3 font-medium">
                      Status
                    </th>
                    <th scope="col" className="px-4 py-3 font-medium">
                      <span className="sr-only">Actions</span>
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((order) => {
                    const orderStatus = (order.status ?? 'PLACED') as Status
                    const next = NEXT_ACTION[orderStatus]
                    return (
                      <tr key={order.id} className="border-b border-charcoal/5 last:border-0">
                        <td className="px-4 py-3">#{order.id}</td>
                        <td className="px-4 py-3 text-charcoal/70">
                          {order.createdAt ? new Date(order.createdAt).toLocaleTimeString() : ''}
                        </td>
                        <td className="px-4 py-3 text-charcoal/70">
                          {(order.items ?? [])
                            .map((line) => `${line.quantity}× ${line.itemName}`)
                            .join(', ')}
                        </td>
                        <td className="px-4 py-3">{formatCurrency(order.totalCents ?? 0)}</td>
                        <td className="px-4 py-3">{orderStatus}</td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-3 text-xs">
                            {next && order.id != null && (
                              <button
                                type="button"
                                disabled={pendingId === order.id}
                                onClick={() =>
                                  updateStatus.mutate({
                                    path: { id: order.id! },
                                    body: { status: next.target },
                                  })
                                }
                                className="text-sage-dark hover:underline disabled:opacity-50"
                                aria-label={`${next.label}: order ${order.id}`}
                              >
                                {next.label}
                              </button>
                            )}
                            {CANCELLABLE.has(orderStatus) && order.id != null && (
                              <button
                                type="button"
                                disabled={pendingId === order.id}
                                onClick={() => {
                                  if (
                                    window.confirm(
                                      `Cancel order #${order.id}? This can't be undone.`,
                                    )
                                  ) {
                                    updateStatus.mutate({
                                      path: { id: order.id! },
                                      body: { status: 'CANCELLED' },
                                    })
                                  }
                                }}
                                className="text-ember hover:underline disabled:opacity-50"
                                aria-label={`Cancel order ${order.id}`}
                              >
                                Cancel
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )
        ) : orders.isPending ? (
          <Spinner label="Loading orders…" />
        ) : (
          <ErrorBanner message="Couldn't load orders." onRetry={() => void orders.refetch()} />
        )}
      </div>
    </section>
  )
}
