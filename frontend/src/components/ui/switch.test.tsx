import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Switch } from './switch';

describe('Switch Component', () => {
    it('renders switch', () => {
        render(<Switch data-testid="switch" />);
        const switchElement = screen.getByTestId('switch');
        expect(switchElement).toBeInTheDocument();
    });

    it('can be toggled on', async () => {
        const user = userEvent.setup();
        render(<Switch data-testid="switch" />);
        const switchElement = screen.getByRole('switch');

        await user.click(switchElement);
        expect(switchElement).toBeChecked();
    });

    it('can be disabled', () => {
        render(<Switch disabled data-testid="switch" />);
        const switchElement = screen.getByRole('switch');
        expect(switchElement).toBeDisabled();
    });

    it('handles checked prop', () => {
        render(<Switch checked data-testid="switch" />);
        const switchElement = screen.getByRole('switch');
        expect(switchElement).toBeChecked();
    });

    it('handles onCheckedChange callback', async () => {
        const handleChange = vi.fn();
        const user = userEvent.setup();

        render(<Switch onCheckedChange={handleChange} data-testid="switch" />);
        const switchElement = screen.getByRole('switch');

        await user.click(switchElement);
        expect(handleChange).toHaveBeenCalled();
    });
});
