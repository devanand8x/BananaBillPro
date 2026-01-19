import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Badge } from './badge';

describe('Badge Component', () => {
    it('renders badge with text', () => {
        render(<Badge>New</Badge>);
        expect(screen.getByText('New')).toBeInTheDocument();
    });

    it('renders with default variant', () => {
        render(<Badge>Default</Badge>);
        const badge = screen.getByText('Default');
        expect(badge).toBeInTheDocument();
    });

    it('renders with destructive variant', () => {
        render(<Badge variant="destructive">Error</Badge>);
        const badge = screen.getByText('Error');
        expect(badge).toHaveClass('bg-destructive');
    });

    it('renders with outline variant', () => {
        render(<Badge variant="outline">Outlined</Badge>);
        const badge = screen.getByText('Outlined');
        expect(badge).toHaveClass('border');
    });

    it('renders with secondary variant', () => {
        render(<Badge variant="secondary">Secondary</Badge>);
        const badge = screen.getByText('Secondary');
        expect(badge).toHaveClass('bg-secondary');
    });

    it('applies custom className', () => {
        render(<Badge className="custom">Badge</Badge>);
        const badge = screen.getByText('Badge');
        expect(badge).toHaveClass('custom');
    });
});
