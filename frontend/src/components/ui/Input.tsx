import type { ComponentPropsWithRef } from 'react'

type InputProps = ComponentPropsWithRef<'input'> & {
  label: string
  error?: string
}

export default function Input({ label, error, id, name, ...rest }: InputProps) {
  const inputId = id ?? name
  const errorId = error ? `${inputId}-error` : undefined
  return (
    <div>
      <label htmlFor={inputId} className="block text-sm font-medium">
        {label}
      </label>
      <input
        id={inputId}
        name={name}
        aria-invalid={error ? true : undefined}
        aria-describedby={errorId}
        className="mt-1 w-full rounded-md border border-charcoal/20 bg-white px-3 py-2 text-sm focus:border-sage-dark focus:outline-none"
        {...rest}
      />
      {error && (
        <p id={errorId} className="mt-1 text-sm text-ember">
          {error}
        </p>
      )}
    </div>
  )
}
