import { useRef, useState } from 'react'
import { Link } from 'react-router'
import { selectCartTotalCents, useCartStore } from '../cartStore'
import { formatCurrency } from '../../../lib/formatCurrency'
import EmptyState from '../../../components/ui/EmptyState'

const MAX_QUANTITY = 20

export default function CartPage() {
  const items = useCartStore((state) => state.items)
  const setQuantity = useCartStore((state) => state.setQuantity)
  const removeItem = useCartStore((state) => state.removeItem)
  const totalCents = useCartStore(selectCartTotalCents)
  const [announcement, setAnnouncement] = useState('')
  const headingRef = useRef<HTMLHeadingElement>(null)

  // Removing a line unmounts the element holding focus; announce the change
  // from a stable live region and park focus on the page heading.
  const removeLine = (menuItemId: number, name: string) => {
    removeItem(menuItemId)
    setAnnouncement(`${name} removed from cart`)
    headingRef.current?.focus()
  }

  const decrement = (menuItemId: number, name: string, quantity: number) => {
    if (quantity <= 1) {
      removeLine(menuItemId, name)
    } else {
      setQuantity(menuItemId, quantity - 1)
    }
  }

  return (
    <section className="mx-auto max-w-2xl px-4 py-12">
      <h1 ref={headingRef} tabIndex={-1} className="font-display text-4xl focus:outline-none">
        Your cart
      </h1>
      <p className="sr-only" role="status">
        {announcement}
      </p>

      {items.length === 0 ? (
        <>
          <EmptyState message="Your cart is empty." />
          <div className="text-center">
            <Link to="/menu" className="text-ember hover:underline">
              Browse the menu
            </Link>
          </div>
        </>
      ) : (
        <>
          <ul className="mt-8 divide-y divide-charcoal/10 rounded-lg border border-charcoal/10 bg-white">
            {items.map((line) => (
              <li key={line.menuItemId} className="flex items-center gap-4 p-4">
                <div className="min-w-0 flex-1">
                  <p className="truncate font-medium">{line.name}</p>
                  <p className="text-sm text-charcoal/70">{formatCurrency(line.priceCents)} each</p>
                </div>
                <div
                  className="flex items-center gap-2"
                  role="group"
                  aria-label={`Quantity of ${line.name}`}
                >
                  <button
                    type="button"
                    onClick={() => decrement(line.menuItemId, line.name, line.quantity)}
                    aria-label={
                      line.quantity <= 1
                        ? `Remove ${line.name} from cart`
                        : `Decrease quantity of ${line.name}`
                    }
                    className="size-8 rounded-md border border-charcoal/20 hover:border-charcoal/40"
                  >
                    −
                  </button>
                  <span className="w-6 text-center text-sm" aria-live="polite">
                    {line.quantity}
                  </span>
                  <button
                    type="button"
                    disabled={line.quantity >= MAX_QUANTITY}
                    onClick={() => setQuantity(line.menuItemId, line.quantity + 1)}
                    aria-label={
                      line.quantity >= MAX_QUANTITY
                        ? `Maximum of ${MAX_QUANTITY} per item reached`
                        : `Increase quantity of ${line.name}`
                    }
                    className="size-8 rounded-md border border-charcoal/20 hover:border-charcoal/40 disabled:opacity-40"
                  >
                    +
                  </button>
                </div>
                <p className="w-20 text-right text-sm">
                  {formatCurrency(line.priceCents * line.quantity)}
                </p>
                <button
                  type="button"
                  onClick={() => removeLine(line.menuItemId, line.name)}
                  aria-label={`Remove ${line.name} from cart`}
                  className="text-sm text-charcoal/50 hover:text-ember"
                >
                  ✕
                </button>
              </li>
            ))}
          </ul>

          <div className="mt-6 flex items-center justify-between">
            <p className="text-lg">
              Total <span className="font-medium">{formatCurrency(totalCents)}</span>
            </p>
            <Link
              to="/checkout"
              className="rounded-md bg-ember px-6 py-3 text-cream transition-colors hover:bg-ember-dark"
            >
              Proceed to checkout
            </Link>
          </div>
        </>
      )}
    </section>
  )
}
