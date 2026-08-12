import type { MenuItemResponse } from '../../../api/generated'
import { formatCurrency } from '../../../lib/formatCurrency'

export default function MenuItemCard({ item }: { item: MenuItemResponse }) {
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
      </div>
    </article>
  )
}
