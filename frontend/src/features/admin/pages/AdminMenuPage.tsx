import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  createItemMutation,
  deleteItemMutation,
  listCategoriesOptions,
  listItemsOptions,
  updateItemMutation,
} from '../../../api/generated/@tanstack/react-query.gen'
import type { MenuItemResponse } from '../../../api/generated'
import type { MenuItemFormValues } from '../schema'
import MenuItemForm from '../components/MenuItemForm'
import Spinner from '../../../components/ui/Spinner'
import ErrorBanner from '../../../components/ui/ErrorBanner'
import { formatCurrency } from '../../../lib/formatCurrency'

type Editing = { mode: 'new' } | { mode: 'edit'; item: MenuItemResponse } | null

export default function AdminMenuPage() {
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState<Editing>(null)
  const [actionError, setActionError] = useState<string | null>(null)

  const categories = useQuery(listCategoriesOptions())
  // The bearer token makes the backend include unavailable items for admins.
  const items = useQuery(listItemsOptions({ query: { size: 100 } }))

  const invalidateMenu = () => {
    void queryClient.invalidateQueries({ queryKey: [{ _id: 'listItems' }] })
    void queryClient.invalidateQueries({ queryKey: [{ _id: 'getItem' }] })
  }

  const onError = (error: unknown) => {
    setActionError(
      typeof error === 'object' && error !== null && 'detail' in error
        ? String((error as { detail: unknown }).detail)
        : 'That change failed. Please try again.',
    )
  }
  // Row-level mutations (toggle, delete) only refresh data — they must NOT
  // close an open edit form and discard the admin's unsaved input.
  const rowSuccess = () => {
    setActionError(null)
    invalidateMenu()
  }
  // Form submission closes the editor on success.
  const formSuccess = () => {
    setActionError(null)
    setEditing(null)
    invalidateMenu()
  }

  const create = useMutation({ ...createItemMutation(), onError })
  const update = useMutation({ ...updateItemMutation(), onError })
  const remove = useMutation({ ...deleteItemMutation(), onError })

  const submitForm = (values: MenuItemFormValues) => {
    const body = {
      categoryId: values.categoryId,
      name: values.name,
      description: values.description || undefined,
      priceCents: values.price,
      imageUrl: values.imageUrl || undefined,
      available: values.available,
    }
    if (editing?.mode === 'edit' && editing.item.id != null) {
      update.mutate({ path: { id: editing.item.id }, body }, { onSuccess: formSuccess })
    } else {
      create.mutate({ body }, { onSuccess: formSuccess })
    }
  }

  const toggleAvailability = (item: MenuItemResponse) => {
    if (item.id == null || item.categoryId == null || item.name == null || item.priceCents == null)
      return
    update.mutate(
      {
        path: { id: item.id },
        body: {
          categoryId: item.categoryId,
          name: item.name,
          description: item.description,
          priceCents: item.priceCents,
          imageUrl: item.imageUrl,
          available: !item.available,
        },
      },
      { onSuccess: rowSuccess },
    )
  }

  const deleteItem = (item: MenuItemResponse) => {
    if (item.id == null) return
    if (!window.confirm(`Delete “${item.name}”? Hiding it is usually better.`)) return
    remove.mutate(
      { path: { id: item.id } },
      {
        onSuccess: () => {
          // If we were editing this row, close the now-dangling form.
          if (editing?.mode === 'edit' && editing.item.id === item.id) setEditing(null)
          rowSuccess()
        },
      },
    )
  }

  const categoryName = (id?: number) =>
    categories.data?.find((category) => category.id === id)?.name ?? '—'

  if (items.isPending || categories.isPending) return <Spinner label="Loading menu…" />
  if (!items.data || !categories.data)
    return <ErrorBanner message="Couldn't load the menu." onRetry={() => void items.refetch()} />

  return (
    <section>
      <div className="flex items-center justify-between">
        <h1 className="font-display text-3xl">Menu management</h1>
        {!editing && (
          <button
            type="button"
            onClick={() => setEditing({ mode: 'new' })}
            className="rounded-md bg-ember px-4 py-2 text-sm text-cream hover:bg-ember-dark"
          >
            New item
          </button>
        )}
      </div>

      {actionError && (
        <p role="alert" className="mt-4 rounded-md bg-ember/10 p-3 text-sm text-ember">
          {actionError}
        </p>
      )}

      {editing && (
        <div className="mt-6">
          {/* key forces a fresh form (and fresh RHF defaults) per edit target,
              so switching rows never carries the previous item's values over. */}
          <MenuItemForm
            key={editing.mode === 'edit' ? editing.item.id : 'new'}
            categories={categories.data}
            initial={editing.mode === 'edit' ? editing.item : undefined}
            pending={create.isPending || update.isPending}
            onSubmit={submitForm}
            onCancel={() => setEditing(null)}
          />
        </div>
      )}

      <div className="mt-6 overflow-x-auto rounded-lg border border-charcoal/10 bg-white">
        <table className="w-full text-left text-sm">
          <thead className="border-b border-charcoal/10 text-charcoal/70">
            <tr>
              <th className="px-4 py-3 font-medium">Item</th>
              <th className="px-4 py-3 font-medium">Category</th>
              <th className="px-4 py-3 font-medium">Price</th>
              <th className="px-4 py-3 font-medium">Status</th>
              <th className="px-4 py-3 font-medium">
                <span className="sr-only">Actions</span>
              </th>
            </tr>
          </thead>
          <tbody>
            {(items.data.content ?? []).map((item) => (
              <tr key={item.id} className="border-b border-charcoal/5 last:border-0">
                <td className="px-4 py-3">{item.name}</td>
                <td className="px-4 py-3 text-charcoal/70">{categoryName(item.categoryId)}</td>
                <td className="px-4 py-3">{formatCurrency(item.priceCents ?? 0)}</td>
                <td className="px-4 py-3">
                  <span
                    className={
                      item.available
                        ? 'rounded-full bg-sage/15 px-2 py-0.5 text-xs text-sage-dark'
                        : 'rounded-full bg-charcoal/10 px-2 py-0.5 text-xs text-charcoal/70'
                    }
                  >
                    {item.available ? 'Available' : 'Hidden'}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex justify-end gap-3 text-xs">
                    <button
                      type="button"
                      onClick={() => setEditing({ mode: 'edit', item })}
                      className="text-ember hover:underline"
                      aria-label={`Edit ${item.name}`}
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => toggleAvailability(item)}
                      className="text-charcoal/70 hover:underline"
                      aria-label={`${item.available ? 'Hide' : 'Show'} ${item.name}`}
                    >
                      {item.available ? 'Hide' : 'Show'}
                    </button>
                    <button
                      type="button"
                      onClick={() => deleteItem(item)}
                      className="text-charcoal/50 hover:underline"
                      aria-label={`Delete ${item.name}`}
                    >
                      Delete
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  )
}
