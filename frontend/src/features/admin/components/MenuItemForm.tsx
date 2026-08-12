import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import type { MenuCategoryResponse, MenuItemResponse } from '../../../api/generated'
import { menuItemSchema, type MenuItemFormInput, type MenuItemFormValues } from '../schema'
import Input from '../../../components/ui/Input'
import Select from '../../../components/ui/Select'

type MenuItemFormProps = {
  categories: MenuCategoryResponse[]
  initial?: MenuItemResponse
  pending: boolean
  onSubmit: (values: MenuItemFormValues) => void
  onCancel: () => void
}

export default function MenuItemForm({
  categories,
  initial,
  pending,
  onSubmit,
  onCancel,
}: MenuItemFormProps) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<MenuItemFormInput, unknown, MenuItemFormValues>({
    resolver: zodResolver(menuItemSchema),
    defaultValues: initial
      ? {
          name: initial.name ?? '',
          categoryId: initial.categoryId ?? undefined,
          description: initial.description ?? '',
          price: initial.priceCents != null ? (initial.priceCents / 100).toFixed(2) : '',
          imageUrl: initial.imageUrl ?? '',
          available: initial.available ?? true,
        }
      : { name: '', description: '', price: '', imageUrl: '', available: true },
  })

  return (
    <form
      onSubmit={(event) => void handleSubmit(onSubmit)(event)}
      noValidate
      className="rounded-lg border border-charcoal/10 bg-white p-6"
    >
      <h2 className="font-medium">{initial ? `Edit “${initial.name}”` : 'New menu item'}</h2>
      <div className="mt-4 grid gap-4 sm:grid-cols-2">
        <Input label="Name" error={errors.name?.message} {...register('name')} />
        <Select label="Category" error={errors.categoryId?.message} {...register('categoryId')}>
          <option value="">Pick a category…</option>
          {categories.map((category) => (
            <option key={category.id} value={category.id}>
              {category.name}
            </option>
          ))}
        </Select>
        <Input
          label="Price (USD)"
          inputMode="decimal"
          error={errors.price?.message}
          {...register('price')}
        />
        <Input
          label="Image URL (optional)"
          error={errors.imageUrl?.message}
          {...register('imageUrl')}
        />
      </div>
      <div className="mt-4">
        <label htmlFor="description" className="block text-sm font-medium">
          Description
        </label>
        <textarea
          id="description"
          rows={2}
          aria-invalid={errors.description ? true : undefined}
          aria-describedby={errors.description ? 'description-error' : undefined}
          className="mt-1 w-full rounded-md border border-charcoal/20 bg-white px-3 py-2 text-sm focus:border-sage-dark focus:outline-none"
          {...register('description')}
        />
        {errors.description && (
          <p id="description-error" className="mt-1 text-sm text-ember">
            {errors.description.message}
          </p>
        )}
      </div>
      <label className="mt-4 flex items-center gap-2 text-sm">
        <input type="checkbox" {...register('available')} />
        Available on the public menu
      </label>
      <div className="mt-6 flex gap-3">
        <button
          type="submit"
          disabled={pending}
          className="rounded-md bg-ember px-4 py-2 text-sm text-cream hover:bg-ember-dark disabled:opacity-60"
        >
          {pending ? 'Saving…' : 'Save'}
        </button>
        <button
          type="button"
          onClick={onCancel}
          className="rounded-md border border-charcoal/20 px-4 py-2 text-sm hover:border-charcoal/40"
        >
          Cancel
        </button>
      </div>
    </form>
  )
}
