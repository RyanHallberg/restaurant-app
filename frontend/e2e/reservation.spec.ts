import { expect, test } from '@playwright/test'

function tomorrowIso(): string {
  const date = new Date()
  date.setDate(date.getDate() + 1)
  return date.toLocaleDateString('en-CA')
}

test('a guest books a table and receives a confirmation code', async ({ page }) => {
  await page.goto('/reserve')

  // Tomorrow avoids today's already-passed slots.
  await page.getByLabel('Date').fill(tomorrowIso())
  await page.getByLabel('Party size').selectOption('4')

  const firstOpenSlot = page.locator('fieldset button:enabled').first()
  await expect(firstOpenSlot).toBeVisible()
  await firstOpenSlot.click()

  await page.getByLabel('Name', { exact: true }).fill('Playwright Guest')
  await page.getByLabel('Email').fill(`e2e-reserve-${Date.now()}@example.com`)
  await page.getByLabel('Phone').fill('555-0142')
  await page.getByRole('button', { name: 'Book table' }).click()

  await expect(page.getByRole('heading', { name: "You're booked!" })).toBeVisible()
  await expect(page.getByText(/^[A-Z2-9]{8}$/)).toBeVisible()
})
