export default function Spinner({ label = 'Loading…' }: { label?: string }) {
  return (
    <div className="flex flex-col items-center gap-3 py-16" role="status">
      <div className="size-8 animate-spin rounded-full border-2 border-sage border-t-transparent" />
      <p className="text-sm text-charcoal/70">{label}</p>
    </div>
  )
}
