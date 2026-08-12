import { describe, expect, it } from 'vitest'
import { reservationSchema, todayIso } from './schema'

const valid = {
  customerName: 'Ada Lovelace',
  customerEmail: 'ada@example.com',
  customerPhone: '555-0100',
  partySize: '4',
  date: todayIso(),
  time: '19:00',
}

describe('reservationSchema', () => {
  it('accepts a valid booking and coerces partySize to a number', () => {
    const result = reservationSchema.parse(valid)
    expect(result.partySize).toBe(4)
  })

  it.each([
    ['empty name', { customerName: '' }, 'customerName'],
    ['bad email', { customerEmail: 'not-an-email' }, 'customerEmail'],
    ['short phone', { customerPhone: '12' }, 'customerPhone'],
    ['party of zero', { partySize: '0' }, 'partySize'],
    ['party of thirteen', { partySize: '13' }, 'partySize'],
    ['past date', { date: '2020-01-01' }, 'date'],
    ['missing time', { time: '' }, 'time'],
  ])('rejects %s', (_label, override, field) => {
    const result = reservationSchema.safeParse({ ...valid, ...override })
    expect(result.success).toBe(false)
    if (!result.success) {
      expect(result.error.issues.map((issue) => issue.path[0])).toContain(field)
    }
  })
})
