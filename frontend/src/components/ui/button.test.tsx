import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Button } from './button';

describe('Button Component', () => {
    it('renders button with text', () => {
        render(<Button>Click me</Button>);
        expect(screen.getByText('Click me')).toBeInTheDocument();
    });

    it('renders with default variant', () => {
        render(<Button>Default</Button>);
        const button = screen.getByRole('button');
        expect(button).toHaveClass('bg-primary');
    });

    it('renders with outline variant', () => {
        render(<Button variant="outline">Outline</Button>);
        const button = screen.getByRole('button');
        expect(button).toHaveClass('border');
    });

    it('can be disabled', () => {
        render(<Button disabled>Disabled</Button>);
        const button = screen.getByRole('button');
        expect(button).toBeDisabled();
    });

    it('handles onClick', () => {
        const handleClick = vi.fn();
        render(<Button onClick={handleClick}>Click</Button>);
        const button = screen.getByRole('button');
        button.click();
        expect(handleClick).toHaveBeenCalledOnce();
    });
});
