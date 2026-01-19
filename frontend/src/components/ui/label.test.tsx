import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { Label } from './label';

describe('Label Component', () => {
    it('renders label with text', () => {
        render(<Label>Username</Label>);
        expect(screen.getByText('Username')).toBeInTheDocument();
    });

    it('associates with input using htmlFor', () => {
        render(
            <div>
                <Label htmlFor="test-input">Email</Label>
                <input id="test-input" />
            </div>
        );

        const label = screen.getByText('Email');
        expect(label).toHaveAttribute('for', 'test-input');
    });

    it('applies custom className', () => {
        render(<Label className="custom-class">Label</Label>);
        const label = screen.getByText('Label');
        expect(label).toHaveClass('custom-class');
    });

    it('renders as label element', () => {
        const { container } = render(<Label>Test</Label>);
        const label = container.querySelector('label');
        expect(label).toBeInTheDocument();
    });
});
