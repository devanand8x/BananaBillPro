import { format } from 'date-fns';

/**
 * Shared Formatters
 * Centralizes formatting logic to avoid duplication
 */

/**
 * Format number as Indian currency
 */
export const formatCurrency = (amount: number, decimals: number = 0): string => {
    return new Intl.NumberFormat('en-IN', {
        style: 'currency',
        currency: 'INR',
        minimumFractionDigits: decimals,
        maximumFractionDigits: decimals,
    }).format(amount);
};

/**
 * Format weight with optional decimals
 */
export const formatWeight = (weight: number): string => {
    const decimals = weight % 1 !== 0 ? 2 : 0;
    return `${weight.toLocaleString('en-IN', {
        minimumFractionDigits: decimals,
        maximumFractionDigits: 2
    })} kg`;
};

/**
 * Format date to Indian format (DD/MM/YYYY)
 */
export const formatDate = (date: Date | string): string => {
    return format(new Date(date), 'dd/MM/yyyy');
};

/**
 * Format date with time
 */
export const formatDateTime = (date: Date | string): string => {
    return format(new Date(date), 'dd MMM yyyy, hh:mm a');
};

/**
 * Format phone number (Indian format)
 */
export const formatPhoneNumber = (phone: string): string => {
    // Remove non-digits
    const cleaned = phone.replace(/\D/g, '');

    // Format as +91 XXXXX XXXXX
    if (cleaned.length === 10) {
        return `+91 ${cleaned.slice(0, 5)} ${cleaned.slice(5)}`;
    }

    return phone;
};

/**
 * Format file size
 */
export const formatFileSize = (bytes: number): string => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
};
