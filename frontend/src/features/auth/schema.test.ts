import { describe, expect, it } from 'vitest'
import { loginSchema, registerSchema } from './schema'

describe('loginSchema', () => {
  it('accepts valid credentials', () => {
    expect(loginSchema.safeParse({ email: 'a@b.co', password: 'x' }).success).toBe(true)
  })

  it.each([
    ['bad email', { email: 'nope', password: 'x' }],
    ['empty password', { email: 'a@b.co', password: '' }],
  ])('rejects %s', (_label, value) => {
    expect(loginSchema.safeParse(value).success).toBe(false)
  })
})

describe('registerSchema', () => {
  const valid = {
    fullName: 'Ada Lovelace',
    email: 'ada@example.com',
    password: 'longenough',
    confirmPassword: 'longenough',
  }

  it('accepts a valid registration', () => {
    expect(registerSchema.safeParse(valid).success).toBe(true)
  })

  it('rejects mismatched passwords on the confirm field', () => {
    const result = registerSchema.safeParse({ ...valid, confirmPassword: 'different11' })
    expect(result.success).toBe(false)
    if (!result.success) {
      expect(result.error.issues[0]?.path).toContain('confirmPassword')
    }
  })

  it('rejects short passwords', () => {
    expect(
      registerSchema.safeParse({ ...valid, password: 'short', confirmPassword: 'short' }).success,
    ).toBe(false)
  })
})
