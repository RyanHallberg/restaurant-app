export default function AboutPage() {
  return (
    <section className="mx-auto max-w-3xl px-4 py-16">
      <h1 className="font-display text-4xl">About Pork Fiction</h1>
      <p className="mt-6 leading-relaxed text-charcoal/80">
        Pork Fiction is a neighborhood restaurant built around a single wood-fired hearth. Our menu
        changes with the seasons and with what our farm partners bring through the back door — but
        the char, the smoke, and the hospitality stay constant.
      </p>
      <p className="mt-4 leading-relaxed text-charcoal/80">
        This is a demo application: every dish, review, and reservation here is fictional, built as
        a full-stack engineering exercise.
      </p>
      <div className="mt-10 grid gap-8 sm:grid-cols-2">
        <div>
          <h2 className="font-medium">Hours</h2>
          <p className="mt-2 text-charcoal/70">Tuesday–Sunday, 11:00–22:00</p>
          <p className="text-charcoal/70">Closed Mondays</p>
        </div>
        <div>
          <h2 className="font-medium">Contact</h2>
          <p className="mt-2 text-charcoal/70">418 Garland Ave</p>
          <p className="text-charcoal/70">(555) 014-2276 · hello@porkfiction.example</p>
        </div>
      </div>
    </section>
  )
}
