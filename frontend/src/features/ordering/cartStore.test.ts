import { afterEach, describe, expect, it } from 'vitest'
import { selectCartCount, selectCartTotalCents, useCartStore } from './cartStore'

const margherita = { menuItemId: 10, name: 'Margherita', priceCents: 1600 }
const coldBrew = { menuItemId: 17, name: 'Cold Brew', priceCents: 600 }

describe('cartStore', () => {
  afterEach(() => {
    useCartStore.getState().clear()
  })

  it('adds new lines and increments existing ones', () => {
    const { addItem } = useCartStore.getState()
    addItem(margherita)
    addItem(margherita)
    addItem(coldBrew)

    const { items } = useCartStore.getState()
    expect(items).toHaveLength(2)
    expect(items.find((line) => line.menuItemId === 10)?.quantity).toBe(2)
    expect(selectCartCount(useCartStore.getState())).toBe(3)
  })

  it('computes the total in integer cents', () => {
    const { addItem, setQuantity } = useCartStore.getState()
    addItem(margherita)
    setQuantity(10, 3)
    addItem(coldBrew)

    expect(selectCartTotalCents(useCartStore.getState())).toBe(1600 * 3 + 600)
  })

  it('removes a line when quantity drops below one', () => {
    const { addItem, setQuantity } = useCartStore.getState()
    addItem(margherita)
    setQuantity(10, 0)

    expect(useCartStore.getState().items).toHaveLength(0)
  })

  it('caps quantity at 20', () => {
    const { addItem, setQuantity } = useCartStore.getState()
    addItem(margherita)
    setQuantity(10, 99)

    expect(useCartStore.getState().items[0]?.quantity).toBe(20)
  })

  it('clears everything', () => {
    const { addItem, clear } = useCartStore.getState()
    addItem(margherita)
    addItem(coldBrew)
    clear()

    expect(useCartStore.getState().items).toHaveLength(0)
  })
})
