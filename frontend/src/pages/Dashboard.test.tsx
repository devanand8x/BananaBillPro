import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import Dashboard from './Dashboard';
import { AuthProvider } from '@/contexts/AuthContext';
import { LanguageProvider } from '@/contexts/LanguageContext';
import { billService } from '@/services/billService';

// Mock services
vi.mock('@/services/billService', () => ({
    billService: {
        getTodayCount: vi.fn(),
        getTotalCount: vi.fn(),
    },
}));

vi.mock('@/contexts/AuthContext', () => ({
    AuthProvider: ({ children }: { children: React.ReactNode }) => <div>{children}</div>,
    useAuth: () => ({
        user: { id: '1', name: 'Test User' },
        signOut: vi.fn(),
    }),
}));

const Wrapper = ({ children }: { children: React.ReactNode }) => (
    <BrowserRouter>
        <LanguageProvider>
            <AuthProvider>
                {children}
            </AuthProvider>
        </LanguageProvider>
    </BrowserRouter>
);

describe('Dashboard', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('renders dashboard with navigation buttons', () => {
        vi.mocked(billService.getTodayCount).mockResolvedValue(0);
        vi.mocked(billService.getTotalCount).mockResolvedValue(0);

        render(<Dashboard />, { wrapper: Wrapper });

        // Check for unique elements instead of duplicate text
        expect(screen.getByText(/create new bill/i)).toBeInTheDocument();
        expect(screen.getByText(/bill history/i)).toBeInTheDocument();
        expect(screen.getByText(/monthly reports/i)).toBeInTheDocument();
    });

    it('displays today bills count', async () => {
        vi.mocked(billService.getTodayCount).mockResolvedValue(15);
        vi.mocked(billService.getTotalCount).mockResolvedValue(100);

        render(<Dashboard />, { wrapper: Wrapper });

        await waitFor(() => {
            expect(screen.getByText('15')).toBeInTheDocument();
        });
    });

    it('displays total bills count', async () => {
        vi.mocked(billService.getTodayCount).mockResolvedValue(15);
        vi.mocked(billService.getTotalCount).mockResolvedValue(248);

        render(<Dashboard />, { wrapper: Wrapper });

        await waitFor(() => {
            expect(screen.getByText('248')).toBeInTheDocument();
        });
    });

    it('shows loading state initially', () => {
        vi.mocked(billService.getTodayCount).mockImplementation(
            () => new Promise(() => { }) // Never resolves
        );
        vi.mocked(billService.getTotalCount).mockImplementation(
            () => new Promise(() => { })
        );

        render(<Dashboard />, { wrapper: Wrapper });

        // Should show loading state (—)
        const loadingElements = screen.getAllByText('—');
        expect(loadingElements).toHaveLength(2);
    });

    it('handles API errors gracefully', async () => {
        const consoleError = vi.spyOn(console, 'error').mockImplementation(() => { });

        vi.mocked(billService.getTodayCount).mockRejectedValue(new Error('API Error'));
        vi.mocked(billService.getTotalCount).mockRejectedValue(new Error('API Error'));

        render(<Dashboard />, { wrapper: Wrapper });

        await waitFor(() => {
            expect(consoleError).toHaveBeenCalled();
        });

        consoleError.mockRestore();
    });
});
