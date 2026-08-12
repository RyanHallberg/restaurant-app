import { z } from 'zod'

function notExpired(value: string): boolean {
  const [month, year] = value.split('/').map(Number)
  if (!month || year === undefined) return false
  const now = new Date()
  const currentYear = now.getFullYear() % 100
  const currentMonth = now.getMonth() + 1
  return year > currentYear || (year === currentYear && month >= currentMonth)
}

export const checkoutSchema = z.object({
  cardNumber: z
    .string()
    .transform((value) => value.replace(/\s/g, ''))
    .pipe(z.string().regex(/^\d{16}$/, 'Enter the 16-digit card number')),
  expiry: z
    .string()
    .regex(/^(0[1-9]|1[0-2])\/\d{2}$/, 'Use MM/YY')
    .refine(notExpired, 'This card has expired'),
  cvc: z.string().regex(/^\d{3,4}$/, 'CVC is 3 or 4 digits'),
})

export type CheckoutFormInput = z.input<typeof checkoutSchema>
export type CheckoutFormValues = z.output<typeof checkoutSchema>
