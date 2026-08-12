import type { ComponentPropsWithRef } from 'react'

type SelectProps = ComponentPropsWithRef<'select'> & {
  label: string
  error?: string
}

export default function Select({ label, error, id, name, children, ...rest }: SelectProps) {
  const selectId = id ?? name
  const errorId = error ? `${selectId}-error` : undefined
  return (
    <div>
      <label htmlFor={selectId} className="block text-sm font-medium">
        {label}
      </label>
      <select
        id={selectId}
        name={name}
        aria-invalid={error ? true : undefined}
        aria-describedby={errorId}
        className="mt-1 w-full rounded-md border border-charcoal/20 bg-white px-3 py-2 text-sm focus:border-sage-dark focus:outline-none"
        {...rest}
      >
        {children}
      </select>
      {error && (
        <p id={errorId} className="mt-1 text-sm text-ember">
          {error}
        </p>
      )}
    </div>
  )
}
