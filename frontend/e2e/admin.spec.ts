import { expect, test } from '@playwright/test'

test('the admin creates a menu item and it appears on the public menu', async ({ page }) => {
  const dishName = `E2E Special ${Date.now()}`

  await page.goto('/login')
  await page.getByLabel('Email').fill('admin@porkfiction.example')
  await page.getByLabel('Password').fill('admin123')
  await page.getByRole('button', { name: 'Sign in' }).click()
  await expect(page.getByRole('link', { name: 'Admin' })).toBeVisible()

  await page.goto('/admin/menu')
  await page.getByRole('button', { name: 'New item' }).click()
  await page.getByLabel('Name').fill(dishName)
  await page.getByLabel('Category').selectOption({ label: 'Mains' })
  await page.getByLabel('Price (USD)').fill('21.50')
  await page.getByRole('button', { name: 'Save' }).click()
  await expect(page.getByRole('cell', { name: dishName, exact: true })).toBeVisible()

  await page.goto('/menu')
  await expect(page.getByRole('heading', { name: dishName })).toBeVisible()
})
