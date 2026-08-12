import { useEffect, useRef, useState } from 'react'
import type { MenuItemResponse } from '../../../api/generated'
import { formatCurrency } from '../../../lib/formatCurrency'
import { useCartStore } from '../../ordering/cartStore'

export default function MenuItemCard({ item }: { item: MenuItemResponse }) {
  const addItem = useCartStore((state) => state.addItem)
  const [justAdded, setJustAdded] = useState(false)
  const resetTimer = useRef<number>(undefined)
  const canAdd = item.available && item.id != null && item.name != null && item.priceCents != null

  useEffect(() => () => window.clearTimeout(resetTimer.current), [])

  const handleAdd = () => {
    addItem({ menuItemId: item.id!, name: item.name!, priceCents: item.priceCents! })
    setJustAdded(true)
    window.clearTimeout(resetTimer.current)
    resetTimer.current = window.setTimeout(() => setJustAdded(false), 1500)
  }

  return (
    <article className="overflow-hidden rounded-lg border border-charcoal/10 bg-white shadow-sm">
      {item.imageUrl && (
        <img
          src={item.imageUrl}
          alt={item.name ?? 'Menu item'}
          loading="lazy"
          className="aspect-[3/2] w-full object-cover"
        />
      )}
      <div className="p-4">
        <div className="flex items-baseline justify-between gap-3">
          <h2 className="font-medium">{item.name}</h2>
          <p className="shrink-0 text-sm text-sage-dark">{formatCurrency(item.priceCents ?? 0)}</p>
        </div>
        {item.description && <p className="mt-1 text-sm text-charcoal/70">{item.description}</p>}
        {canAdd && (
          <>
            <button
              type="button"
              onClick={handleAdd}
              aria-label={`Add ${item.name} to cart`}
              className="mt-3 rounded-md border border-ember px-3 py-1.5 text-sm text-ember transition-colors hover:bg-ember hover:text-cream"
            >
              {justAdded ? 'Added ✓' : 'Add to cart'}
            </button>
            {/* Persistent live region so the confirmation is announced. */}
            <p className="sr-only" role="status">
              {justAdded ? `${item.name} added to cart` : ''}
            </p>
          </>
        )}
      </div>
    </article>
  )
}
