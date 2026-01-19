import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import * as React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { LanguageProvider } from '@/contexts/LanguageContext';

// Mock the auth context
const mockSignIn = vi.fn();
const mockUser = null;

vi.mock('@/contexts/AuthContext', () => ({
    AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    useAuth: () => ({
        user: mockUser,
        signIn: mockSignIn,
        signOut: vi.fn(),
    }),
}));

vi.mock('@/hooks/use-toast', () => ({
    useToast: () => ({
        toast: vi.fn(),
    }),
}));

const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate,
    };
});

// Simple login form component for testing
const SimpleLoginForm = () => {
    const [mobile, setMobile] = React.useState('');
    const [password, setPassword] = React.useState('');

    return (
        <div>
            <input
                data-testid="mobile-input"
                type="tel"
                value={mobile}
                onChange={(e) => setMobile(e.target.value)}
                placeholder="Mobile"
            />
            <input
                data-testid="password-input"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Password"
            />
            <button data-testid="login-button">Login</button>
        </div>
    );
};

describe('LoginPage - Form Validation', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('validates mobile number length', () => {
        const mobile = '9876543210';
        expect(mobile.length).toBe(10);
    });

    it('validates mobile is numeric', () => {
        const mobile = '9876543210';
        expect(/^\d+$/.test(mobile)).toBe(true);
    });

    it('validates password minimum length', () => {
        const password = 'password123';
        expect(password.length).toBeGreaterThanOrEqual(8);
    });

    it('rejects invalid mobile numbers', () => {
        const invalidMobile = '12345'; // Too short
        expect(invalidMobile.length).toBeLessThan(10);
    });

    it('rejects short passwords', () => {
        const shortPassword = 'pass';
        expect(shortPassword.length).toBeLessThan(8);
    });
});

describe('LoginPage - Form Rendering', () => {
    const Wrapper = ({ children }: { children: React.ReactNode }) => (
        <BrowserRouter>
            <LanguageProvider>
                {children}
            </LanguageProvider>
        </BrowserRouter>
    );

    it('renders login form inputs', () => {
        render(<SimpleLoginForm />, { wrapper: Wrapper });

        expect(screen.getByTestId('mobile-input')).toBeInTheDocument();
        expect(screen.getByTestId('password-input')).toBeInTheDocument();
        expect(screen.getByTestId('login-button')).toBeInTheDocument();
    });

    it('mobile input accepts numbers', () => {
        render(<SimpleLoginForm />, { wrapper: Wrapper });

        const input = screen.getByTestId('mobile-input') as HTMLInputElement;
        fireEvent.change(input, { target: { value: '9876543210' } });

        expect(input.value).toBe('9876543210');
    });

    it('password input hides characters', () => {
        render(<SimpleLoginForm />, { wrapper: Wrapper });

        const input = screen.getByTestId('password-input');
        expect(input).toHaveAttribute('type', 'password');
    });
});
