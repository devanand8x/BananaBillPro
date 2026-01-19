import { describe, it, expect, vi } from 'vitest';
import apiClient from './apiClient';

// Define auth service
const authService = {
    async login(mobile: string, password: string) {
        const response = await apiClient.post('/auth/login', { mobile, password });
        return response.data;
    },

    async register(data: { name: string; mobile: string; password: string }) {
        const response = await apiClient.post('/auth/register', data);
        return response.data;
    },

    async logout() {
        const response = await apiClient.post('/auth/logout');
        return response.data;
    },

    async refreshToken(refreshToken: string) {
        const response = await apiClient.post('/auth/refresh', { refreshToken });
        return response.data;
    },

    async changePassword(oldPassword: string, newPassword: string) {
        const response = await apiClient.post('/auth/change-password', { oldPassword, newPassword });
        return response.data;
    },
};

// Mock apiClient
vi.mock('./apiClient');

describe('authService', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it('logs in successfully', async () => {
        const mockResponse = {
            data: {
                success: true,
                accessToken: 'token123',
                user: { id: '1', name: 'Test User' },
            },
        };

        vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

        const result = await authService.login('9876543210', 'password123');

        expect(apiClient.post).toHaveBeenCalledWith('/auth/login', {
            mobile: '9876543210',
            password: 'password123',
        });
        expect(result.success).toBe(true);
        expect(result.accessToken).toBeDefined();
    });

    it('registers new user', async () => {
        const mockResponse = {
            data: {
                success: true,
                user: { id: '1', name: 'New User', mobile: '9876543210' },
            },
        };

        vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

        const result = await authService.register({
            name: 'New User',
            mobile: '9876543210',
            password: 'password123',
        });

        expect(apiClient.post).toHaveBeenCalledWith('/auth/register', {
            name: 'New User',
            mobile: '9876543210',
            password: 'password123',
        });
        expect(result.success).toBe(true);
    });

    it('logs out successfully', async () => {
        const mockResponse = { data: { success: true } };
        vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

        const result = await authService.logout();

        expect(apiClient.post).toHaveBeenCalledWith('/auth/logout');
        expect(result.success).toBe(true);
    });

    it('refreshes access token', async () => {
        const mockResponse = {
            data: {
                accessToken: 'newToken123',
            },
        };

        vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

        await authService.refreshToken('oldToken');

        expect(apiClient.post).toHaveBeenCalledWith('/auth/refresh', {
            refreshToken: 'oldToken',
        });
    });

    it('changes password', async () => {
        const mockResponse = { data: { success: true, message: 'Password changed' } };
        vi.mocked(apiClient.post).mockResolvedValue(mockResponse);

        const result = await authService.changePassword('oldPass123', 'newPass456');

        expect(apiClient.post).toHaveBeenCalledWith('/auth/change-password', {
            oldPassword: 'oldPass123',
            newPassword: 'newPass456',
        });
        expect(result.success).toBe(true);
    });

    it('handles login errors', async () => {
        vi.mocked(apiClient.post).mockRejectedValue(new Error('Invalid credentials'));

        await expect(authService.login('wrong', 'wrong')).rejects.toThrow('Invalid credentials');
    });
});
