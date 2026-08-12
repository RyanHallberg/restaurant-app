const usd = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' })

/** Prices travel as integer cents end-to-end; format only at the render edge. */
export function formatCurrency(cents: number): string {
  return usd.format(cents / 100)
}
