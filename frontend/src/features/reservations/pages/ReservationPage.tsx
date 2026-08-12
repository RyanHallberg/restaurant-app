import { useEffect, useRef, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import {
  availabilityOptions,
  createMutation,
} from '../../../api/generated/@tanstack/react-query.gen'
import type { ReservationResponse } from '../../../api/generated'
import {
  reservationSchema,
  todayIso,
  type ReservationFormInput,
  type ReservationFormValues,
} from '../schema'
import Input from '../../../components/ui/Input'
import Select from '../../../components/ui/Select'
import Spinner from '../../../components/ui/Spinner'

const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/

function slotClass(selected: boolean, available: boolean) {
  if (!available)
    return 'rounded-md border border-charcoal/10 px-2 py-1.5 text-sm text-charcoal/30 line-through'
  return selected
    ? 'rounded-md bg-sage-dark px-2 py-1.5 text-sm text-cream'
    : 'rounded-md border border-charcoal/20 px-2 py-1.5 text-sm hover:border-sage-dark'
}

function Confirmation({
  reservation,
  onReset,
}: {
  reservation: ReservationResponse
  onReset: () => void
}) {
  const headingRef = useRef<HTMLHeadingElement>(null)

  // The form subtree (and its focused submit button) unmounts on success;
  // move focus so keyboard and screen-reader users land on the result.
  useEffect(() => {
    headingRef.current?.focus()
  }, [])

  return (
    <div
      role="status"
      className="mx-auto max-w-md rounded-lg border border-sage/40 bg-white p-8 text-center shadow-sm"
    >
      <h1 ref={headingRef} tabIndex={-1} className="font-display text-2xl focus:outline-none">
        You&apos;re booked!
      </h1>
      <p className="mt-4 text-charcoal/80">
        {reservation.date} at {String(reservation.time).slice(0, 5)} · party of{' '}
        {reservation.partySize}
      </p>
      <p className="mt-6 text-sm text-charcoal/70">Your confirmation code</p>
      <p className="font-mono mt-1 text-3xl tracking-widest">{reservation.confirmationCode}</p>
      <p className="mt-6 text-sm text-charcoal/70">
        Keep this code — you&apos;ll use it if you need to change or cancel.
      </p>
      <button
        type="button"
        onClick={onReset}
        className="mt-8 rounded-md border border-charcoal/20 px-4 py-2 text-sm hover:border-charcoal/40"
      >
        Make another reservation
      </button>
    </div>
  )
}

export default function ReservationPage() {
  const queryClient = useQueryClient()
  const [confirmed, setConfirmed] = useState<ReservationResponse | null>(null)
  const [submitError, setSubmitError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<ReservationFormInput, unknown, ReservationFormValues>({
    resolver: zodResolver(reservationSchema),
    defaultValues: { date: todayIso(), partySize: 2, time: '' },
  })

  const date = watch('date')
  const selectedTime = watch('time')
  const dateIsValid = DATE_PATTERN.test(date)

  // A slot picked for one date means nothing on another; stale picks would
  // only fail server-side with a confusing 409.
  useEffect(() => {
    setValue('time', '')
    setSubmitError(null)
  }, [date, setValue])

  const availability = useQuery({
    ...availabilityOptions({ query: { date } }),
    enabled: dateIsValid,
  })

  const slots = availability.data?.slots ?? []
  const hasOpenSlot = slots.some((slot) => slot.available)

  const create = useMutation({
    ...createMutation(),
    onSuccess: (reservation) => {
      setConfirmed(reservation)
      setSubmitError(null)
      // Invalidate every cached date's availability, not just the current key.
      void queryClient.invalidateQueries({ queryKey: [{ _id: 'availability' }] })
    },
    onError: (error) => {
      const detail =
        typeof error === 'object' && error !== null && 'detail' in error
          ? String((error as { detail: unknown }).detail)
          : 'We couldn’t complete your booking. Please try again.'
      setSubmitError(detail)
      setValue('time', '')
      void availability.refetch()
    },
  })

  const onSubmit = handleSubmit((values) =>
    create.mutateAsync({ body: values }).then(
      () => undefined,
      () => undefined, // errors surface via onError; keep RHF from re-throwing
    ),
  )

  if (confirmed) {
    return (
      <section className="mx-auto max-w-5xl px-4 py-16">
        <Confirmation
          reservation={confirmed}
          onReset={() => {
            reset({ date: todayIso(), partySize: 2, time: '' })
            setConfirmed(null)
          }}
        />
      </section>
    )
  }

  return (
    <section className="mx-auto max-w-xl px-4 py-12">
      <h1 className="font-display text-4xl">Reserve a table</h1>
      <p className="mt-2 text-charcoal/70">
        Parties of up to 12, seated every 30 minutes from 11:00 to 21:30.
      </p>

      <form onSubmit={(event) => void onSubmit(event)} noValidate className="mt-8 space-y-5">
        <div className="grid gap-5 sm:grid-cols-2">
          <Input
            label="Date"
            type="date"
            min={todayIso()}
            error={errors.date?.message}
            {...register('date')}
          />
          <Select label="Party size" error={errors.partySize?.message} {...register('partySize')}>
            {Array.from({ length: 12 }, (_, i) => i + 1).map((n) => (
              <option key={n} value={n}>
                {n} {n === 1 ? 'guest' : 'guests'}
              </option>
            ))}
          </Select>
        </div>

        <fieldset aria-describedby={errors.time ? 'time-error' : undefined}>
          <legend className="text-sm font-medium">Time</legend>
          {availability.data ? (
            hasOpenSlot ? (
              <div className="mt-2 grid grid-cols-4 gap-2 sm:grid-cols-6">
                {slots.map((slot) => (
                  <button
                    key={slot.time}
                    type="button"
                    disabled={!slot.available}
                    aria-pressed={selectedTime === slot.time}
                    onClick={() => {
                      setValue('time', slot.time ?? '', { shouldValidate: true })
                      setSubmitError(null)
                    }}
                    className={slotClass(selectedTime === slot.time, slot.available ?? false)}
                  >
                    {slot.time}
                  </button>
                ))}
              </div>
            ) : (
              <p className="mt-2 text-sm text-charcoal/70">
                No times left for this date — try another day.
              </p>
            )
          ) : availability.isLoading ? (
            <Spinner label="Checking availability…" />
          ) : dateIsValid ? (
            <p className="mt-2 text-sm text-ember">
              We couldn&apos;t check availability.{' '}
              <button
                type="button"
                onClick={() => void availability.refetch()}
                className="underline"
              >
                Try again
              </button>
            </p>
          ) : (
            <p className="mt-2 text-sm text-charcoal/70">Pick a date to see available times.</p>
          )}
          {errors.time && (
            <p id="time-error" role="alert" className="mt-2 text-sm text-ember">
              {errors.time.message}
            </p>
          )}
        </fieldset>

        <Input label="Name" error={errors.customerName?.message} {...register('customerName')} />
        <div className="grid gap-5 sm:grid-cols-2">
          <Input
            label="Email"
            type="email"
            error={errors.customerEmail?.message}
            {...register('customerEmail')}
          />
          <Input
            label="Phone"
            type="tel"
            error={errors.customerPhone?.message}
            {...register('customerPhone')}
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
          {isSubmitting ? 'Booking…' : 'Book table'}
        </button>
      </form>
    </section>
  )
}
