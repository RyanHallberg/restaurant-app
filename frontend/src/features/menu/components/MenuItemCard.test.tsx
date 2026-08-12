import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import MenuItemCard from './MenuItemCard'
import type { MenuItemResponse } from '../../../api/generated'

const item: MenuItemResponse = {
  id: 17,
  categoryId: 5,
  name: 'Cold Brew',
  description: 'Single-origin cold brew over ice',
  priceCents: 600,
  imageUrl: 'https://example.test/coldbrew.jpg',
  available: true,
}

describe('MenuItemCard', () => {
  it('renders name, description, and formatted price', () => {
    render(<MenuItemCard item={item} />)
    expect(screen.getByRole('heading', { name: 'Cold Brew' })).toBeInTheDocument()
    expect(screen.getByText('Single-origin cold brew over ice')).toBeInTheDocument()
    expect(screen.getByText('$6.00')).toBeInTheDocument()
  })

  it('omits the image when imageUrl is absent', () => {
    render(<MenuItemCard item={{ ...item, imageUrl: undefined }} />)
    expect(screen.queryByRole('img')).not.toBeInTheDocument()
  })
})
