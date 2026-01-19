// Farmer Types
export interface Farmer {
    id: string;
    name: string;
    mobileNumber: string;
    address?: string;
    createdBy?: string;
    createdAt?: string;
    updatedAt?: string;
}

// Bill Types
export interface Bill {
    id: string;
    billNumber: string;
    farmerId: string;
    farmer?: Farmer;
    vehicleNumber?: string;

    // Weight calculations
    grossWeight: number;
    pattiWeight: number;
    boxCount: number;
    netWeight: number;
    dandaWeight: number;
    tutWastage: number;
    finalNetWeight: number;

    // Payment calculations
    ratePerKg: number;
    totalAmount: number;
    majuri: number;
    netAmount: number;

    // Payment status
    paymentStatus: PaymentStatus;
    paidAmount: number;
    advanceAmount: number;
    paymentDate?: string;
    dueDate?: string;

    // Audit
    createdBy?: string;
    createdAt: string;
    updatedAt?: string;
}

export type PaymentStatus = 'UNPAID' | 'PARTIAL' | 'PAID';

// User Types
export interface User {
    id: string;
    name: string;
    mobileNumber: string;
    role: 'ADMIN' | 'TRADER';
}

// Auth Types
export interface LoginRequest {
    mobileNumber: string;
    password: string;
}

export interface LoginResponse {
    accessToken: string;
    refreshToken: string;
    user: User;
}

// Bill Creation Request
export interface CreateBillRequest {
    farmerMobile: string;
    farmerName: string;
    farmerAddress?: string;
    vehicleNumber?: string;
    grossWeight: number;
    pattiWeight: number;
    boxCount: number;
    tutWastage: number;
    ratePerKg: number;
    majuri: number;
}

// API Response
export interface ApiResponse<T> {
    success: boolean;
    message?: string;
    data?: T;
}

// Dashboard Stats
export interface DashboardStats {
    todayBillsCount: number;
    totalBillsCount: number;
    unpaidCount: number;
    totalUnpaidAmount: number;
}
