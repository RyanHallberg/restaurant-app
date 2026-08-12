import { expect, test } from '@playwright/test'

test('a new customer registers, orders, and lands on the status timeline', async ({ page }) => {
  const email = `e2e-order-${Date.now()}@example.com`

  await page.goto('/register')
  await page.getByLabel('Full name').fill('Playwright Customer')
  await page.getByLabel('Email').fill(email)
  await page.getByLabel('Password', { exact: true }).fill('e2e-password-1')
  await page.getByLabel('Confirm password').fill('e2e-password-1')
  await page.getByRole('button', { name: 'Create account' }).click()
  await expect(page.getByRole('button', { name: 'Sign out' })).toBeVisible()

  await page.goto('/menu')
  await page
    .getByRole('button', { name: /^Add .+ to cart$/ })
    .first()
    .click()
  await page.getByRole('link', { name: /^Cart, 1 item$/ }).click()

  await page.getByRole('link', { name: 'Proceed to checkout' }).click()
  await page.getByLabel('Card number').fill('4111111111111111')
  await page.getByLabel('Expiry (MM/YY)').fill('12/30')
  await page.getByLabel('CVC').fill('123')
  await page.getByRole('button', { name: /^Pay / }).click()

  await expect(page).toHaveURL(/\/orders\/\d+$/)
  await expect(page.getByRole('heading', { name: /^Order #\d+$/ })).toBeVisible()
  await expect(
    page.getByRole('list', { name: 'Order progress' }).getByText('Order received'),
  ).toBeVisible()
})
