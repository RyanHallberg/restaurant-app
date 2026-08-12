export default function EmptyState({ message }: { message: string }) {
  return (
    <div className="py-16 text-center">
      <p className="text-charcoal/70">{message}</p>
    </div>
  )
}
