import { Link } from 'react-router'

export default function NotFoundPage() {
  return (
    <section className="mx-auto max-w-3xl px-4 py-24 text-center">
      <h1 className="font-display text-5xl">404</h1>
      <p className="mt-4 text-charcoal/70">That page isn&apos;t on the menu.</p>
      <Link to="/" className="mt-8 inline-block text-ember hover:underline">
        Back to the homepage
      </Link>
    </section>
  )
}
