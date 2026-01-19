import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { logger } from '@/utils/logger';

/**
 * API Client with automatic token refresh
 * 
 * Token Strategy:
 * - Access Token: 15 min (stored in memory, used for API calls)
 * - Refresh Token: 7 days (stored in localStorage, used to get new access tokens)
 */

// Determine API URL based on environment
const getApiUrl = () => {
    // Check for environment variable first
    if (import.meta.env.VITE_API_URL) {
        return import.meta.env.VITE_API_URL;
    }

    const host = window.location.hostname;
    const protocol = window.location.protocol;

    // Development
    if (host === 'localhost' || host === '127.0.0.1') {
        return 'http://localhost:8080/api';
    }

    // Production - use same protocol as page
    return `${protocol}//${host}/api`;
};

// Create axios instance
const apiClient = axios.create({
    baseURL: getApiUrl(),
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Token storage keys
const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';

// Flag to prevent multiple refresh attempts
let isRefreshing = false;
let failedQueue: Array<{
    resolve: (token: string) => void;
    reject: (error: Error) => void;
}> = [];

// Process queued requests after token refresh
const processQueue = (error: Error | null, token: string | null = null) => {
    failedQueue.forEach(promise => {
        if (error) {
            promise.reject(error);
        } else {
            promise.resolve(token!);
        }
    });
    failedQueue = [];
};

// Request interceptor - add access token to headers
apiClient.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
        if (accessToken && config.headers) {
            config.headers['Authorization'] = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response interceptor - handle 401 and refresh token
apiClient.interceptors.response.use(
    (response) => response,
    async (error: AxiosError) => {
        const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

        // If error is 401 and we haven't retried yet
        if (error.response?.status === 401 && !originalRequest._retry) {

            // Skip refresh for auth endpoints
            if (originalRequest.url?.includes('/auth/login') ||
                originalRequest.url?.includes('/auth/register') ||
                originalRequest.url?.includes('/auth/refresh')) {
                return Promise.reject(error);
            }

            // If already refreshing, queue this request
            if (isRefreshing) {
                return new Promise((resolve, reject) => {
                    failedQueue.push({ resolve, reject });
                }).then(token => {
                    if (originalRequest.headers) {
                        originalRequest.headers['Authorization'] = `Bearer ${token}`;
                    }
                    return apiClient(originalRequest);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);

            if (!refreshToken) {
                // No refresh token - redirect to login
                handleLogout();
                return Promise.reject(error);
            }

            try {
                // Attempt to refresh tokens
                const response = await axios.post(`${getApiUrl()}/auth/refresh`, {
                    refreshToken: refreshToken
                });

                const { accessToken, refreshToken: newRefreshToken } = response.data.data;

                // Store new tokens
                localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
                localStorage.setItem(REFRESH_TOKEN_KEY, newRefreshToken);

                // Update authorization header
                if (originalRequest.headers) {
                    originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
                }

                // Process queued requests
                processQueue(null, accessToken);

                // Retry original request
                return apiClient(originalRequest);

            } catch (refreshError) {
                // Refresh failed - logout
                processQueue(refreshError as Error, null);
                handleLogout();
                return Promise.reject(refreshError);
            } finally {
                isRefreshing = false;
            }
        }

        return Promise.reject(error);
    }
);

// Handle logout - clear tokens and redirect
const handleLogout = () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem('user');

    // Only redirect if not already on login page
    if (!window.location.pathname.includes('/login')) {
        window.location.href = '/login';
    }
};

// Helper functions for token management
export const setTokens = (accessToken: string, refreshToken: string) => {
    localStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
};

export const clearTokens = () => {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
};

export const getAccessToken = () => localStorage.getItem(ACCESS_TOKEN_KEY);
export const getRefreshToken = () => localStorage.getItem(REFRESH_TOKEN_KEY);

export default apiClient;
