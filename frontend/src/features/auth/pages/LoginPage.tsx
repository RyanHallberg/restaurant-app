import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router'
import { loginMutation } from '../../../api/generated/@tanstack/react-query.gen'
import { useAuthStore } from '../authStore'
import { loginSchema, type LoginFormValues } from '../schema'
import Input from '../../../components/ui/Input'

export default function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [searchParams] = useSearchParams()
  const setAuth = useAuthStore((state) => state.setAuth)
  const user = useAuthStore((state) => state.user)
  const [submitError, setSubmitError] = useState<string | null>(null)

  // Redirect anyone who is already signed in (or just signed in) to their
  // intended destination — one effect owns the redirect so it can't race the
  // mutation's own navigate and drop `from`.
  const from = (location.state as { from?: string } | null)?.from
  useEffect(() => {
    if (user) void navigate(from ?? '/', { replace: true })
  }, [user, from, navigate])

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({ resolver: zodResolver(loginSchema) })

  const login = useMutation({
    ...loginMutation(),
    onSuccess: (auth) => {
      // Just set auth; the effect above performs the redirect once `user` is set.
      if (auth.token && auth.user) setAuth(auth.token, auth.user)
    },
    onError: (error) => {
      const detail =
        typeof error === 'object' && error !== null && 'detail' in error
          ? String((error as { detail: unknown }).detail)
          : 'Sign in failed. Please try again.'
      setSubmitError(detail)
    },
  })

  const onSubmit = handleSubmit((values) =>
    login.mutateAsync({ body: values }).then(
      () => undefined,
      () => undefined,
    ),
  )

  return (
    <section className="mx-auto max-w-sm px-4 py-16">
      <h1 className="font-display text-3xl">Sign in</h1>
      {searchParams.get('expired') && (
        <p role="alert" className="mt-4 rounded-md bg-ember/10 p-3 text-sm text-ember">
          Your session expired — please sign in again.
        </p>
      )}
      <form onSubmit={(event) => void onSubmit(event)} noValidate className="mt-8 space-y-5">
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
          autoComplete="current-password"
          error={errors.password?.message}
          {...register('password')}
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
          {isSubmitting ? 'Signing in…' : 'Sign in'}
        </button>
      </form>
      <p className="mt-6 text-sm text-charcoal/70">
        No account yet?{' '}
        <Link to="/register" className="text-ember hover:underline">
          Create one
        </Link>
      </p>
    </section>
  )
}
