import { useQuery } from '@tanstack/react-query'
import { useParams } from 'react-router'
import { getOrderOptions } from '../../../api/generated/@tanstack/react-query.gen'
import { formatCurrency } from '../../../lib/formatCurrency'
import Spinner from '../../../components/ui/Spinner'
import ErrorBanner from '../../../components/ui/ErrorBanner'

const PIPELINE = ['PLACED', 'PREPARING', 'READY', 'COMPLETED'] as const
const LABELS: Record<string, string> = {
  PLACED: 'Order received',
  PREPARING: 'In the kitchen',
  READY: 'Ready for pickup',
  COMPLETED: 'Completed',
}

export default function OrderStatusPage() {
  const { orderId } = useParams()
  const id = Number(orderId)
  const idIsValid = Number.isInteger(id) && id > 0

  const order = useQuery({
    ...getOrderOptions({ path: { id } }),
    enabled: idIsValid,
    // Poll while the kitchen works; stop on a final state — and on errors,
    // or a 404'd order would be re-requested every 5s forever.
    refetchInterval: (query) => {
      if (query.state.status === 'error') return false
      const status = query.state.data?.status
      return status === 'COMPLETED' || status === 'CANCELLED' ? false : 5000
    },
  })

  // A disabled query stays isPending forever; without this check a malformed
  // URL would show a spinner that never resolves.
  if (!idIsValid)
    return <ErrorBanner message="That order link doesn't look right — check the address." />
  if (order.isPending) return <Spinner label="Loading your order…" />
  if (!order.data)
    return (
      <ErrorBanner message="We couldn't load this order." onRetry={() => void order.refetch()} />
    )

  const status = order.data.status ?? 'PLACED'
  const currentIndex = PIPELINE.indexOf(status as (typeof PIPELINE)[number])

  return (
    <section className="mx-auto max-w-md px-4 py-12">
      <h1 className="font-display text-4xl">Order #{order.data.id}</h1>
      <p className="mt-2 text-sm text-charcoal/70">
        Placed {order.data.createdAt ? new Date(order.data.createdAt).toLocaleString() : ''} · ref{' '}
        <span className="font-mono">{order.data.paymentReference}</span>
      </p>

      {/* Persistent live region: polled status changes must be announced, and
          a region mounted together with its text is not reliably read. */}
      <p className="sr-only" role="status">
        {status === 'CANCELLED' ? 'Order cancelled' : `Order status: ${LABELS[status] ?? status}`}
      </p>

      {status === 'CANCELLED' ? (
        <p role="status" className="mt-8 rounded-md bg-ember/10 p-4 text-ember">
          This order was cancelled. If that's a surprise, call us at (555) 014-2276.
        </p>
      ) : (
        <ol className="mt-8 space-y-4" aria-label="Order progress">
          {PIPELINE.map((step, index) => {
            const done = index < currentIndex
            const current = index === currentIndex
            return (
              <li key={step} className="flex items-center gap-3">
                <span
                  aria-hidden
                  className={
                    done
                      ? 'size-4 rounded-full bg-sage-dark'
                      : current
                        ? 'size-4 animate-pulse rounded-full bg-ember'
                        : 'size-4 rounded-full border border-charcoal/30'
                  }
                />
                <span className={current ? 'font-medium' : done ? '' : 'text-charcoal/50'}>
                  {LABELS[step]}
                  {current && <span className="sr-only"> (current status)</span>}
                </span>
              </li>
            )
          })}
        </ol>
      )}

      <div className="mt-8 rounded-lg border border-charcoal/10 bg-white p-4">
        <h2 className="text-sm font-medium text-charcoal/70">Items</h2>
        <ul className="mt-2 space-y-1 text-sm">
          {(order.data.items ?? []).map((line, index) => (
            <li key={index} className="flex justify-between">
              <span>
                {line.quantity} × {line.itemName}
              </span>
              <span>{formatCurrency((line.priceCents ?? 0) * (line.quantity ?? 0))}</span>
            </li>
          ))}
        </ul>
        <p className="mt-3 flex justify-between border-t border-charcoal/10 pt-3 font-medium">
          <span>Total</span>
          <span>{formatCurrency(order.data.totalCents ?? 0)}</span>
        </p>
      </div>
    </section>
  )
}
