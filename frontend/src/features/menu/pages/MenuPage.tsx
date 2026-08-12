import { useState } from 'react'
import { keepPreviousData, useQuery } from '@tanstack/react-query'
import {
  listCategoriesOptions,
  listItemsOptions,
} from '../../../api/generated/@tanstack/react-query.gen'
import CategoryTabs from '../components/CategoryTabs'
import MenuItemCard from '../components/MenuItemCard'
import Spinner from '../../../components/ui/Spinner'
import ErrorBanner from '../../../components/ui/ErrorBanner'
import EmptyState from '../../../components/ui/EmptyState'

function RefreshFailedNotice({ onRetry }: { onRetry: () => void }) {
  return (
    <p role="status" className="mt-3 text-sm text-ember">
      We couldn&apos;t refresh this just now —{' '}
      <button type="button" onClick={onRetry} className="underline">
        try again
      </button>
    </p>
  )
}

export default function MenuPage() {
  const [categoryId, setCategoryId] = useState<number | null>(null)

  const categories = useQuery(listCategoriesOptions())
  // keepPreviousData: switching category filters targets a new cache key; keep
  // the previous grid mounted (dimmed) instead of collapsing to a spinner.
  const items = useQuery({
    ...listItemsOptions({ query: { categoryId: categoryId ?? undefined, size: 50 } }),
    placeholderData: keepPreviousData,
  })

  const itemList = items.data?.content ?? []

  return (
    <section className="mx-auto max-w-5xl px-4 py-12">
      <h1 className="font-display text-4xl">The Menu</h1>
      <p className="mt-2 text-charcoal/70">Seasonal, wood-fired, and made to share.</p>

      {/* Data-first branching: cached data keeps rendering even if a background
          refetch fails; the full error state is reserved for "no data at all". */}
      <div className="mt-8">
        {categories.data ? (
          <>
            <CategoryTabs
              categories={categories.data}
              selectedId={categoryId}
              onSelect={setCategoryId}
            />
            {categories.isError && (
              <RefreshFailedNotice onRetry={() => void categories.refetch()} />
            )}
          </>
        ) : categories.isPending ? (
          <Spinner label="Loading categories…" />
        ) : (
          <ErrorBanner
            message="We couldn't load the menu categories."
            onRetry={() => void categories.refetch()}
          />
        )}
      </div>

      <div className="mt-8">
        {items.data ? (
          itemList.length === 0 ? (
            <EmptyState message="No dishes in this category yet — check back soon." />
          ) : (
            <>
              {items.isError && <RefreshFailedNotice onRetry={() => void items.refetch()} />}
              <p className="sr-only" role="status">
                {itemList.length} dishes shown
              </p>
              <div
                aria-busy={items.isFetching}
                className={`grid gap-6 transition-opacity sm:grid-cols-2 lg:grid-cols-3 ${
                  items.isFetching ? 'opacity-60' : ''
                }`}
              >
                {itemList.map((item) => (
                  <MenuItemCard key={item.id} item={item} />
                ))}
              </div>
            </>
          )
        ) : items.isPending ? (
          <Spinner label="Loading dishes…" />
        ) : (
          <ErrorBanner
            message="We couldn't load the menu right now."
            onRetry={() => void items.refetch()}
          />
        )}
      </div>
    </section>
  )
}
