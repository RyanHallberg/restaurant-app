import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export type CartLine = {
  menuItemId: number
  name: string
  priceCents: number
  quantity: number
}

type CartState = {
  items: CartLine[]
  addItem: (item: { menuItemId: number; name: string; priceCents: number }) => void
  removeItem: (menuItemId: number) => void
  setQuantity: (menuItemId: number, quantity: number) => void
  clear: () => void
}

const MAX_QUANTITY = 20

/**
 * Prices here are display-only convenience (mirrored as integer cents). The
 * backend re-prices every order from the database at checkout.
 */
export const useCartStore = create<CartState>()(
  persist(
    (set) => ({
      items: [],
      addItem: (item) =>
        set((state) => {
          const existing = state.items.find((line) => line.menuItemId === item.menuItemId)
          if (existing) {
            // Refresh the price/name snapshot too — the caller has fresh menu
            // data in hand, and a persisted stale price shouldn't survive it.
            return {
              items: state.items.map((line) =>
                line.menuItemId === item.menuItemId
                  ? {
                      ...line,
                      name: item.name,
                      priceCents: item.priceCents,
                      quantity: Math.min(line.quantity + 1, MAX_QUANTITY),
                    }
                  : line,
              ),
            }
          }
          return { items: [...state.items, { ...item, quantity: 1 }] }
        }),
      removeItem: (menuItemId) =>
        set((state) => ({ items: state.items.filter((line) => line.menuItemId !== menuItemId) })),
      setQuantity: (menuItemId, quantity) =>
        set((state) => ({
          items:
            quantity < 1
              ? state.items.filter((line) => line.menuItemId !== menuItemId)
              : state.items.map((line) =>
                  line.menuItemId === menuItemId
                    ? { ...line, quantity: Math.min(quantity, MAX_QUANTITY) }
                    : line,
                ),
        })),
      clear: () => set({ items: [] }),
    }),
    { name: 'restaurant-cart', version: 1 },
  ),
)

export const selectCartCount = (state: CartState) =>
  state.items.reduce((sum, line) => sum + line.quantity, 0)

export const selectCartTotalCents = (state: CartState) =>
  state.items.reduce((sum, line) => sum + line.priceCents * line.quantity, 0)
