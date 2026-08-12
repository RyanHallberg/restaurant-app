import { z } from 'zod'

/** Local-timezone YYYY-MM-DD (toISOString would shift dates near midnight). */
export function todayIso(): string {
  return new Date().toLocaleDateString('en-CA')
}

export const reservationSchema = z.object({
  customerName: z.string().trim().min(1, 'Name is required').max(100, 'Name is too long'),
  customerEmail: z.email('Enter a valid email'),
  customerPhone: z.string().trim().min(7, 'Enter a phone number').max(30, 'Phone is too long'),
  partySize: z.coerce.number().int().min(1, 'At least 1 guest').max(12, 'Parties over 12, call us'),
  date: z
    .string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, 'Pick a date')
    .refine((value) => value >= todayIso(), 'Pick today or a future date'),
  time: z.string().min(1, 'Pick a time'),
})

/** Raw field values (partySize arrives as a select string before coercion). */
export type ReservationFormInput = z.input<typeof reservationSchema>
/** Parsed values after zod validation — what submit handlers receive. */
export type ReservationFormValues = z.output<typeof reservationSchema>
