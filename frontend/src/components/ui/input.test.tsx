import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Input } from './input';

describe('Input Component', () => {
    it('renders input field', () => {
        render(<Input placeholder="Enter text" />);
        const input = screen.getByPlaceholderText('Enter text');
        expect(input).toBeInTheDocument();
    });

    it('accepts typed input', async () => {
        const user = userEvent.setup();
        render(<Input />);
        const input = screen.getByRole('textbox');

        await user.type(input, 'Hello World');
        expect(input).toHaveValue('Hello World');
    });

    it('can be disabled', () => {
        render(<Input disabled />);
        const input = screen.getByRole('textbox');
        expect(input).toBeDisabled();
    });

    it('handles onChange event', async () => {
        const handleChange = vi.fn();
        const user = userEvent.setup();
        render(<Input onChange={handleChange} />);
        const input = screen.getByRole('textbox');

        await user.type(input, 'a');
        expect(handleChange).toHaveBeenCalled();
    });

    it('accepts different types', () => {
        const { rerender } = render(<Input type="email" data-testid="test-input" />);
        let input = screen.getByTestId('test-input');
        expect(input).toHaveAttribute('type', 'email');

        rerender(<Input type="password" data-testid="test-input" />);
        input = screen.getByTestId('test-input');
        expect(input).toHaveAttribute('type', 'password');
    });
});
