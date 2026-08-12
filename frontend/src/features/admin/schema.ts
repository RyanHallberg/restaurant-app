import { z } from 'zod'

export const menuItemSchema = z.object({
  name: z.string().trim().min(1, 'Name is required').max(100, 'Name is too long'),
  categoryId: z.coerce.number().int().positive('Pick a category'),
  description: z.string().max(1000, 'Description is too long'),
  // Admins type dollars; the API (like all money in this app) speaks cents.
  price: z
    .string()
    .regex(/^\d+(\.\d{1,2})?$/, 'Enter a price like 12.50')
    .transform((value) => Math.round(parseFloat(value) * 100)),
  imageUrl: z.union([z.literal(''), z.url('Must be a full URL')]),
  available: z.boolean(),
})

export type MenuItemFormInput = z.input<typeof menuItemSchema>
export type MenuItemFormValues = z.output<typeof menuItemSchema>
