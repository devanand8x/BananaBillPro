import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { billService, BillWithFarmer } from '@/services/billService';

export interface BillFilters {
    paymentStatus: 'all' | 'paid' | 'unpaid';
    searchMobile: string;
    startDate: string;
    endDate: string;
}

/**
 * Custom hook for BillHistory logic
 * Separates business logic from UI
 */
export const useBillHistory = () => {
    const [filters, set Filters] = useState<BillFilters>({
        paymentStatus: 'all',
        searchMobile: '',
        startDate: '',
        endDate: '',
    });

    // Fetch bills with React Query
    const {
        data: bills = [],
        isLoading,
        error,
        refetch,
    } = useQuery({
        queryKey: ['bills', filters],
        queryFn: async () => {
            if (filters.searchMobile) {
                return billService.searchByMobile(filters.searchMobile);
            }
            return billService.getAll(filters);
        },
    });

    // Filter bills based on payment status
    const filteredBills = useMemo(() => {
        if (filters.paymentStatus === 'all') return bills;

        const isPaid = filters.paymentStatus === 'paid';
        return bills.filter((bill: BillWithFarmer) => bill.isPaid === isPaid);
    }, [bills, filters.paymentStatus]);

    // Handlers
    const handleFilterChange = (key: keyof BillFilters, value: string) => {
        setFilters(prev => ({ ...prev, [key]: value }));
    };

    const clearFilters = () => {
        setFilters({
            paymentStatus: 'all',
            searchMobile: '',
            startDate: '',
            endDate: '',
        });
    };

    return {
        bills: filteredBills,
        filters,
        isLoading,
        error,
        handleFilterChange,
        clearFilters,
        refetch,
    };
};
