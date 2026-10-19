import { describe, expect, it } from 'vitest'
import { render, screen } from '@testing-library/react'
import { DataSourceBadge } from './Badge'

describe('DataSourceBadge', () => {
  it('labels demo data clearly, never as a live API result', () => {
    render(<DataSourceBadge isDemoData={true} />)
    expect(screen.getByText('Demo data')).toBeInTheDocument()
  })

  it('labels real provider data as Live API', () => {
    render(<DataSourceBadge isDemoData={false} />)
    expect(screen.getByText('Live API')).toBeInTheDocument()
  })
})
