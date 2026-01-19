import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Textarea } from './textarea';

describe('Textarea Component', () => {
    it('renders textarea', () => {
        render(<Textarea placeholder="Enter text" />);
        const textarea = screen.getByPlaceholderText('Enter text');
        expect(textarea).toBeInTheDocument();
    });

    it('accepts typed input', async () => {
        const user = userEvent.setup();
        render(<Textarea data-testid="textarea" />);
        const textarea = screen.getByTestId('textarea');

        await user.type(textarea, 'Multi-line\ntext input');
        expect(textarea).toHaveValue('Multi-line\ntext input');
    });

    it('can be disabled', () => {
        render(<Textarea disabled data-testid="textarea" />);
        const textarea = screen.getByTestId('textarea');
        expect(textarea).toBeDisabled();
    });

    it('handles onChange event', async () => {
        const handleChange = vi.fn();
        const user = userEvent.setup();
        render(<Textarea onChange={handleChange} data-testid="textarea" />);
        const textarea = screen.getByTestId('textarea');

        await user.type(textarea, 'test');
        expect(handleChange).toHaveBeenCalled();
    });

    it('applies custom className', () => {
        render(<Textarea className="custom-class" data-testid="textarea" />);
        const textarea = screen.getByTestId('textarea');
        expect(textarea).toHaveClass('custom-class');
    });
});
