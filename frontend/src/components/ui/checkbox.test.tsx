import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Checkbox } from './checkbox';

describe('Checkbox Component', () => {
    it('renders checkbox', () => {
        render(<Checkbox data-testid="checkbox" />);
        const checkbox = screen.getByTestId('checkbox');
        expect(checkbox).toBeInTheDocument();
    });

    it('can be checked', async () => {
        const user = userEvent.setup();
        render(<Checkbox data-testid="checkbox" />);
        const checkbox = screen.getByTestId('checkbox');

        await user.click(checkbox);
        expect(checkbox).toBeChecked();
    });

    it('can be disabled', () => {
        render(<Checkbox disabled data-testid="checkbox" />);
        const checkbox = screen.getByTestId('checkbox');
        expect(checkbox).toBeDisabled();
    });

    it('handles checked prop', () => {
        render(<Checkbox checked data-testid="checkbox" />);
        const checkbox = screen.getByTestId('checkbox');
        expect(checkbox).toBeChecked();
    });

    it('handles onCheckedChange callback', async () => {
        const handleChange = vi.fn();
        const user = userEvent.setup();

        render(<Checkbox onCheckedChange={handleChange} data-testid="checkbox" />);
        const checkbox = screen.getByTestId('checkbox');

        await user.click(checkbox);
        expect(handleChange).toHaveBeenCalled();
    });
});
