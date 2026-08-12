import { describe, expect, it } from 'vitest'
import { formatCurrency } from './formatCurrency'

describe('formatCurrency', () => {
  it('formats whole-dollar cents as USD', () => {
    expect(formatCurrency(1400)).toBe('$14.00')
  })

  it('formats sub-dollar amounts', () => {
    expect(formatCurrency(50)).toBe('$0.50')
  })

  it('formats zero', () => {
    expect(formatCurrency(0)).toBe('$0.00')
  })

  it('formats amounts over a thousand dollars with grouping', () => {
    expect(formatCurrency(123456)).toBe('$1,234.56')
  })
})
