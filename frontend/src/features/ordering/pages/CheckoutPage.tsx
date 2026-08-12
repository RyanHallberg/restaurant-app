import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router'
import { createOrderMutation } from '../../../api/generated/@tanstack/react-query.gen'
import { selectCartTotalCents, useCartStore } from '../cartStore'
import { checkoutSchema, type CheckoutFormInput, type CheckoutFormValues } from '../schema'
import { formatCurrency } from '../../../lib/formatCurrency'
import Input from '../../../components/ui/Input'

export default function CheckoutPage() {
  const navigate = useNavigate()
  const items = useCartStore((state) => state.items)
  const clearCart = useCartStore((state) => state.clear)
  const totalCents = useCartStore(selectCartTotalCents)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<CheckoutFormInput, unknown, CheckoutFormValues>({
    resolver: zodResolver(checkoutSchema),
  })

  const placeOrder = useMutation({
    ...createOrderMutation(),
    onSuccess: (order) => {
      clearCart()
      void navigate(`/orders/${order.id}`, { replace: true })
    },
    onError: (error) => {
      setSubmitError(
        typeof error === 'object' && error !== null && 'detail' in error
          ? String((error as { detail: unknown }).detail)
          : 'We couldn’t place your order. Please try again.',
      )
    },
  })

  const onSubmit = handleSubmit((payment) => {
    // Clear first so an identical repeat failure re-mounts the alert and is
    // re-announced by assistive tech.
    setSubmitError(null)
    return placeOrder
      .mutateAsync({
        body: {
          items: items.map((line) => ({ menuItemId: line.menuItemId, quantity: line.quantity })),
          payment,
        },
      })
      .then(
        () => undefined,
        () => undefined,
      )
  })

  if (items.length === 0) {
    return (
      <section className="mx-auto max-w-md px-4 py-16 text-center">
        <h1 className="font-display text-3xl">Checkout</h1>
        <p className="mt-4 text-charcoal/70">Your cart is empty.</p>
        <Link to="/menu" className="mt-4 inline-block text-ember hover:underline">
          Browse the menu
        </Link>
      </section>
    )
  }

  return (
    <section className="mx-auto max-w-md px-4 py-12">
      <h1 className="font-display text-4xl">Checkout</h1>

      <div className="mt-6 rounded-lg border border-charcoal/10 bg-white p-4">
        <h2 className="text-sm font-medium text-charcoal/70">Order summary</h2>
        <ul className="mt-2 space-y-1 text-sm">
          {items.map((line) => (
            <li key={line.menuItemId} className="flex justify-between">
              <span>
                {line.quantity} × {line.name}
              </span>
              <span>{formatCurrency(line.priceCents * line.quantity)}</span>
            </li>
          ))}
        </ul>
        <p className="mt-3 flex justify-between border-t border-charcoal/10 pt-3 font-medium">
          <span>Total</span>
          <span>{formatCurrency(totalCents)}</span>
        </p>
        <p className="mt-2 text-xs text-charcoal/70">
          Your total is confirmed at order time from current menu prices.
        </p>
      </div>

      <p className="mt-4 rounded-md bg-sage/10 p-3 text-xs text-charcoal/70">
        This is a demo — no real payment happens. Use any 16-digit card number, e.g.{' '}
        <span className="font-mono">4111 1111 1111 1111</span>. The card{' '}
        <span className="font-mono">4000 0000 0000 0002</span> simulates a decline.
      </p>

      <form onSubmit={(event) => void onSubmit(event)} noValidate className="mt-6 space-y-5">
        <Input
          label="Card number"
          inputMode="numeric"
          autoComplete="cc-number"
          error={errors.cardNumber?.message}
          {...register('cardNumber')}
        />
        <div className="grid grid-cols-2 gap-5">
          <Input
            label="Expiry (MM/YY)"
            inputMode="numeric"
            autoComplete="cc-exp"
            placeholder="12/30"
            error={errors.expiry?.message}
            {...register('expiry')}
          />
          <Input
            label="CVC"
            inputMode="numeric"
            autoComplete="cc-csc"
            error={errors.cvc?.message}
            {...register('cvc')}
          />
        </div>

        {submitError && (
          <p role="alert" className="text-sm text-ember">
            {submitError}
          </p>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className="w-full rounded-md bg-ember px-6 py-3 text-cream transition-colors hover:bg-ember-dark disabled:opacity-60"
        >
          {isSubmitting ? 'Placing order…' : `Pay ${formatCurrency(totalCents)}`}
        </button>
      </form>
    </section>
  )
}
