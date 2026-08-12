import { Link } from 'react-router'

export default function HomePage() {
  return (
    <section className="mx-auto max-w-5xl px-4 py-24 text-center">
      <p className="text-sm uppercase tracking-[0.3em] text-sage-dark">Est. 2026</p>
      <h1 className="font-display mt-4 text-5xl tracking-tight sm:text-6xl">Sage &amp; Ember</h1>
      <p className="mx-auto mt-6 max-w-xl text-lg text-charcoal/70">
        Wood-fired, seasonal cooking built around local farms — served in a warm room, or at your
        table at home.
      </p>
      <div className="mt-10 flex justify-center gap-4">
        <Link
          to="/reserve"
          className="rounded-md bg-ember px-6 py-3 text-cream transition-colors hover:bg-ember-dark"
        >
          Reserve a table
        </Link>
        <Link
          to="/menu"
          className="rounded-md border border-charcoal/20 px-6 py-3 transition-colors hover:border-charcoal/40"
        >
          View the menu
        </Link>
      </div>
    </section>
  )
}
