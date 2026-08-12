import { describe, expect, it } from 'vitest'
import { checkoutSchema } from './schema'

const valid = { cardNumber: '4111111111111111', expiry: '12/30', cvc: '123' }

describe('checkoutSchema', () => {
  it('accepts a valid card', () => {
    expect(checkoutSchema.safeParse(valid).success).toBe(true)
  })

  it('strips spaces from the card number', () => {
    const result = checkoutSchema.parse({ ...valid, cardNumber: '4111 1111 1111 1111' })
    expect(result.cardNumber).toBe('4111111111111111')
  })

  it.each([
    ['short card number', { cardNumber: '4111' }],
    ['letters in card number', { cardNumber: '4111abcd11111111' }],
    ['bad expiry format', { expiry: '13/30' }],
    ['expired card', { expiry: '01/20' }],
    ['bad cvc', { cvc: '12' }],
  ])('rejects %s', (_label, override) => {
    expect(checkoutSchema.safeParse({ ...valid, ...override }).success).toBe(false)
  })
})
