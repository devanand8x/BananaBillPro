import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import apiClient from '@/services/apiClient';

// ============================================================
// QUERY KEYS - Centralized key management for cache invalidation
// ============================================================
export const queryKeys = {
    // Bills
    bills: ['bills'] as const,
    billsList: () => [...queryKeys.bills, 'list'] as const,
    billsRecent: (limit: number) => [...queryKeys.bills, 'recent', limit] as const,
    billsByFarmer: (mobile: string) => [...queryKeys.bills, 'farmer', mobile] as const,
    billById: (id: string) => [...queryKeys.bills, 'detail', id] as const,
    billsStats: () => [...queryKeys.bills, 'stats'] as const,
    billsUnpaid: () => [...queryKeys.bills, 'unpaid'] as const,

    // Farmers
    farmers: ['farmers'] as const,
    farmersList: () => [...queryKeys.farmers, 'list'] as const,
    farmerByMobile: (mobile: string) => [...queryKeys.farmers, 'mobile', mobile] as const,
    farmerById: (id: string) => [...queryKeys.farmers, 'detail', id] as const,

    // Reports
    reports: ['reports'] as const,
    monthlyReport: (year: number, month: number) => [...queryKeys.reports, 'monthly', year, month] as const,
    dateRangeReport: (start: string, end: string) => [...queryKeys.reports, 'range', start, end] as const,

    // Dashboard
    dashboard: ['dashboard'] as const,
    dashboardStats: () => [...queryKeys.dashboard, 'stats'] as const,
};

// ============================================================
// BILL INTERFACES
// ============================================================
interface Bill {
    id: string;
    billNumber: string;
    farmerId: string;
    farmer: {
        name: string;
        mobileNumber: string;
    };
    grossWeight: number;
    netWeight: number;
    netAmount: number;
    paymentStatus: 'PAID' | 'UNPAID' | 'PARTIAL';
    createdAt: string;
}

interface CreateBillRequest {
    farmerId: string;
    vehicleNumber?: string | null;
    grossWeight: number;
    pattiWeight: number;
    boxCount: number;
    tutWastage: number;
    ratePerKg: number;
    majuri: number;
}

interface DashboardStats {
    todayCount: number;
    totalCount: number;
    unpaidCount: number;
    unpaidAmount: number;
}

// ============================================================
// BILL QUERIES
// ============================================================

/**
 * Fetch recent bills with caching
 */
export function useRecentBills(limit: number = 20) {
    return useQuery({
        queryKey: queryKeys.billsRecent(limit),
        queryFn: async () => {
            const response = await apiClient.get(`/bills/recent?limit=${limit}`);
            return response.data.data || response.data;
        },
        staleTime: 2 * 60 * 1000, // 2 minutes
    });
}

/**
 * Fetch bill by ID
 */
export function useBill(id: string) {
    return useQuery({
        queryKey: queryKeys.billById(id),
        queryFn: async () => {
            const response = await apiClient.get(`/bills/${id}`);
            return response.data.data || response.data;
        },
        enabled: !!id,
    });
}

/**
 * Fetch bills by farmer mobile
 */
export function useBillsByFarmer(mobile: string) {
    return useQuery({
        queryKey: queryKeys.billsByFarmer(mobile),
        queryFn: async () => {
            const response = await apiClient.get(`/bills/farmer/${mobile}`);
            return response.data.data || response.data;
        },
        enabled: mobile.length >= 10,
    });
}

/**
 * Fetch unpaid bills
 */
export function useUnpaidBills() {
    return useQuery({
        queryKey: queryKeys.billsUnpaid(),
        queryFn: async () => {
            const response = await apiClient.get('/bills/unpaid');
            return response.data.data || response.data;
        },
        staleTime: 1 * 60 * 1000, // 1 minute
    });
}

// ============================================================
// BILL MUTATIONS
// ============================================================

/**
 * Create new bill with cache invalidation
 */
export function useCreateBill() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (data: CreateBillRequest) => {
            const response = await apiClient.post('/bills', data);
            return response.data.data || response.data;
        },
        onSuccess: () => {
            // Invalidate related queries
            queryClient.invalidateQueries({ queryKey: queryKeys.bills });
            queryClient.invalidateQueries({ queryKey: queryKeys.dashboardStats() });
        },
    });
}

/**
 * Update bill
 */
export function useUpdateBill() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async ({ id, data }: { id: string; data: CreateBillRequest }) => {
            const response = await apiClient.put(`/bills/${id}`, data);
            return response.data.data || response.data;
        },
        onSuccess: (_, variables) => {
            queryClient.invalidateQueries({ queryKey: queryKeys.billById(variables.id) });
            queryClient.invalidateQueries({ queryKey: queryKeys.bills });
        },
    });
}

/**
 * Delete bill with optimistic update
 */
export function useDeleteBill() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (id: string) => {
            await apiClient.delete(`/bills/${id}`);
            return id;
        },
        onMutate: async (id) => {
            // Cancel outgoing refetches
            await queryClient.cancelQueries({ queryKey: queryKeys.bills });

            // Snapshot previous value
            const previousBills = queryClient.getQueryData(queryKeys.billsRecent(20));

            // Optimistically remove from cache
            queryClient.setQueryData(queryKeys.billsRecent(20), (old: Bill[] | undefined) =>
                old?.filter(bill => bill.id !== id)
            );

            return { previousBills };
        },
        onError: (_, __, context) => {
            // Rollback on error
            if (context?.previousBills) {
                queryClient.setQueryData(queryKeys.billsRecent(20), context.previousBills);
            }
        },
        onSettled: () => {
            queryClient.invalidateQueries({ queryKey: queryKeys.bills });
            queryClient.invalidateQueries({ queryKey: queryKeys.dashboardStats() });
        },
    });
}

/**
 * Mark bill as paid
 */
export function useMarkAsPaid() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (id: string) => {
            const response = await apiClient.post(`/bills/${id}/mark-paid`);
            return response.data.data || response.data;
        },
        onSuccess: (_, id) => {
            queryClient.invalidateQueries({ queryKey: queryKeys.billById(id) });
            queryClient.invalidateQueries({ queryKey: queryKeys.billsUnpaid() });
            queryClient.invalidateQueries({ queryKey: queryKeys.dashboardStats() });
        },
    });
}

// ============================================================
// DASHBOARD QUERIES
// ============================================================

/**
 * Fetch all dashboard stats at once
 */
export function useDashboardStats() {
    return useQuery({
        queryKey: queryKeys.dashboardStats(),
        queryFn: async (): Promise<DashboardStats> => {
            // Parallel fetches
            const [todayRes, totalRes, unpaidRes] = await Promise.all([
                apiClient.get('/bills/stats/today'),
                apiClient.get('/bills/stats/total'),
                apiClient.get('/bills/stats/unpaid'),
            ]);

            return {
                todayCount: todayRes.data.count || todayRes.data.data?.count || 0,
                totalCount: totalRes.data.count || totalRes.data.data?.count || 0,
                unpaidCount: unpaidRes.data.count || unpaidRes.data.data?.count || 0,
                unpaidAmount: unpaidRes.data.totalAmount || unpaidRes.data.data?.totalAmount || 0,
            };
        },
        staleTime: 1 * 60 * 1000, // 1 minute
        refetchOnWindowFocus: true,
    });
}

// ============================================================
// FARMER QUERIES
// ============================================================

/**
 * Fetch farmer by mobile
 */
export function useFarmer(mobile: string) {
    return useQuery({
        queryKey: queryKeys.farmerByMobile(mobile),
        queryFn: async () => {
            const response = await apiClient.get(`/farmers/mobile/${mobile}`);
            return response.data.data || response.data;
        },
        enabled: mobile.length >= 10,
        retry: false, // Don't retry if farmer not found
    });
}

/**
 * Fetch all farmers
 */
export function useFarmers() {
    return useQuery({
        queryKey: queryKeys.farmersList(),
        queryFn: async () => {
            const response = await apiClient.get('/farmers');
            return response.data.data || response.data;
        },
        staleTime: 5 * 60 * 1000, // 5 minutes
    });
}

/**
 * Create/Update farmer
 */
export function useUpsertFarmer() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: async (data: { mobileNumber: string; name: string; address?: string }) => {
            const response = await apiClient.post('/farmers', data);
            return response.data.data || response.data;
        },
        onSuccess: (farmer) => {
            queryClient.setQueryData(queryKeys.farmerByMobile(farmer.mobileNumber), farmer);
            queryClient.invalidateQueries({ queryKey: queryKeys.farmersList() });
        },
    });
}

// ============================================================
// REPORT QUERIES
// ============================================================

/**
 * Fetch monthly report
 */
export function useMonthlyReport(year: number, month: number) {
    return useQuery({
        queryKey: queryKeys.monthlyReport(year, month),
        queryFn: async () => {
            const response = await apiClient.get(`/reports/monthly?year=${year}&month=${month}`);
            return response.data.data || response.data;
        },
        staleTime: 10 * 60 * 1000, // 10 minutes - reports don't change often
    });
}

/**
 * Fetch date range report
 */
export function useDateRangeReport(startDate: string, endDate: string) {
    return useQuery({
        queryKey: queryKeys.dateRangeReport(startDate, endDate),
        queryFn: async () => {
            const response = await apiClient.get(`/reports/date-range?startDate=${startDate}&endDate=${endDate}`);
            return response.data.data || response.data;
        },
        enabled: !!startDate && !!endDate,
        staleTime: 10 * 60 * 1000,
    });
}
