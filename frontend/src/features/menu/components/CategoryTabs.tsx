import type { MenuCategoryResponse } from '../../../api/generated'

type CategoryTabsProps = {
  categories: MenuCategoryResponse[]
  selectedId: number | null
  onSelect: (id: number | null) => void
}

// These are filter toggles, not ARIA tabs: tab roles promise a keyboard
// contract (roving focus, arrow keys, tabpanel) this filter doesn't need.
// role=group + aria-pressed matches what the buttons actually do.
export default function CategoryTabs({ categories, selectedId, onSelect }: CategoryTabsProps) {
  const tabClass = (active: boolean) =>
    active
      ? 'rounded-full bg-sage-dark px-4 py-1.5 text-sm text-cream'
      : 'rounded-full border border-charcoal/15 px-4 py-1.5 text-sm text-charcoal/70 hover:border-charcoal/35'

  return (
    <div className="flex flex-wrap gap-2" role="group" aria-label="Filter menu by category">
      <button
        type="button"
        aria-pressed={selectedId === null}
        className={tabClass(selectedId === null)}
        onClick={() => onSelect(null)}
      >
        All
      </button>
      {categories.map((category) => (
        <button
          key={category.id}
          type="button"
          aria-pressed={selectedId === category.id}
          className={tabClass(selectedId === category.id)}
          onClick={() => onSelect(category.id ?? null)}
        >
          {category.name}
        </button>
      ))}
    </div>
  )
}
