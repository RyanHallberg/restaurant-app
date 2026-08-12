import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate } from 'react-router'
import { registerMutation } from '../../../api/generated/@tanstack/react-query.gen'
import { useAuthStore } from '../authStore'
import { registerSchema, type RegisterFormValues } from '../schema'
import Input from '../../../components/ui/Input'

export default function RegisterPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)
  const user = useAuthStore((state) => state.user)
  const [submitError, setSubmitError] = useState<string | null>(null)

  useEffect(() => {
    if (user) void navigate('/', { replace: true })
  }, [user, navigate])

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RegisterFormValues>({ resolver: zodResolver(registerSchema) })

  const signUp = useMutation({
    ...registerMutation(),
    onSuccess: (auth) => {
      if (auth.token && auth.user) setAuth(auth.token, auth.user)
      void navigate('/', { replace: true })
    },
    onError: (error) => {
      const detail =
        typeof error === 'object' && error !== null && 'detail' in error
          ? String((error as { detail: unknown }).detail)
          : 'Registration failed. Please try again.'
      setSubmitError(detail)
    },
  })

  const onSubmit = handleSubmit(({ fullName, email, password }) =>
    signUp.mutateAsync({ body: { fullName, email, password } }).then(
      () => undefined,
      () => undefined,
    ),
  )

  return (
    <section className="mx-auto max-w-sm px-4 py-16">
      <h1 className="font-display text-3xl">Create an account</h1>
      <p className="mt-2 text-sm text-charcoal/70">Track your reservations and order online.</p>
      <form onSubmit={(event) => void onSubmit(event)} noValidate className="mt-8 space-y-5">
        <Input
          label="Full name"
          autoComplete="name"
          error={errors.fullName?.message}
          {...register('fullName')}
        />
        <Input
          label="Email"
          type="email"
          autoComplete="email"
          error={errors.email?.message}
          {...register('email')}
        />
        <Input
          label="Password"
          type="password"
          autoComplete="new-password"
          error={errors.password?.message}
          {...register('password')}
        />
        <Input
          label="Confirm password"
          type="password"
          autoComplete="new-password"
          error={errors.confirmPassword?.message}
          {...register('confirmPassword')}
        />
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
          {isSubmitting ? 'Creating account…' : 'Create account'}
        </button>
      </form>
      <p className="mt-6 text-sm text-charcoal/70">
        Already have an account?{' '}
        <Link to="/login" className="text-ember hover:underline">
          Sign in
        </Link>
      </p>
    </section>
  )
}
