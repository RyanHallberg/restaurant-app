type ErrorBannerProps = {
  message?: string
  onRetry?: () => void
}

export default function ErrorBanner({
  message = 'Something went wrong loading this page.',
  onRetry,
}: ErrorBannerProps) {
  return (
    <div
      role="alert"
      className="mx-auto my-12 max-w-md rounded-md border border-ember/30 bg-ember/5 p-6 text-center"
    >
      <p className="text-charcoal/80">{message}</p>
      {onRetry && (
        <button
          type="button"
          onClick={onRetry}
          className="mt-4 rounded-md bg-ember px-4 py-2 text-sm text-cream hover:bg-ember-dark"
        >
          Try again
        </button>
      )}
    </div>
  )
}
