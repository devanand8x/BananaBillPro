/**
 * Constants
 * Centralized app-wide constants
 */

// Cache durations
export const CACHE_TIME = {
    SHORT: 1 * 60 * 1000,      // 1 minute
    MEDIUM: 5 * 60 * 1000,     // 5 minutes
    LONG: 10 * 60 * 1000,      // 10 minutes
    VERY_LONG: 30 * 60 * 1000, // 30 minutes
} as const;

// API timeouts
export const TIMEOUT = {
    DEFAULT: 30000,    // 30 seconds
    UPLOAD: 120000,    // 2 minutes
    DOWNLOAD: 60000,   // 1 minute
} as const;

// Pagination
export const PAGINATION = {
    DEFAULT_PAGE_SIZE: 20,
    MAX_PAGE_SIZE: 100,
} as const;

// Validation
export const VALIDATION = {
    MIN_PASSWORD_LENGTH: 8,
    MAX_PASSWORD_LENGTH: 100,
    PHONE_LENGTH: 10,
    OTP_LENGTH: 6,
} as const;

// Date formats
export const DATE_FORMAT = {
    DISPLAY: 'dd/MM/yyyy',
    API: 'yyyy-MM-dd',
    FULL: 'dd MMM yyyy, hh:mm a',
} as const;

// Query keys
export const QUERY_KEYS = {
    BILLS: 'bills',
    FARMERS: 'farmers',
    REPORTS: 'reports',
    USER: 'user',
    PAYMENTS: 'payments',
} as const;

// Routes
export const ROUTES = {
    HOME: '/',
    LOGIN: '/login',
    REGISTER: '/register',
    DASHBOARD: '/dashboard',
    CREATE_BILL: '/create-bill',
    BILL_HISTORY: '/history',
    REPORTS: '/reports',
    FARMER_REPORT: '/farmer-report',
} as const;

// Export everything
export default {
    CACHE_TIME,
    TIMEOUT,
    PAGINATION,
    VALIDATION,
    DATE_FORMAT,
    QUERY_KEYS,
    ROUTES,
};
