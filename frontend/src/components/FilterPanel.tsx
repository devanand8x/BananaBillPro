import React, { memo } from 'react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Calendar, Filter, X } from 'lucide-react';

export type PaymentStatusFilter = 'ALL' | 'PAID' | 'UNPAID' | 'PARTIAL';

interface DateFilterProps {
    startDate: string;
    endDate: string;
    onStartDateChange: (date: string) => void;
    onEndDateChange: (date: string) => void;
    onApply: () => void;
    onClear: () => void;
    isFiltered: boolean;
}

interface PaymentFilterProps {
    value: PaymentStatusFilter;
    onChange: (status: PaymentStatusFilter) => void;
}

interface FilterPanelProps {
    dateFilter?: DateFilterProps;
    paymentFilter?: PaymentFilterProps;
    className?: string;
}

/**
 * Date Range Filter Component
 */
export const DateFilter: React.FC<DateFilterProps> = memo(({
    startDate,
    endDate,
    onStartDateChange,
    onEndDateChange,
    onApply,
    onClear,
    isFiltered
}) => {
    return (
        <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
            <div className="flex items-center gap-2 mb-3">
                <Calendar className="w-4 h-4 text-gray-500" />
                <span className="font-medium text-sm text-gray-700">Date Range</span>
            </div>

            <div className="grid grid-cols-2 gap-3 mb-3">
                <div>
                    <label className="text-xs text-gray-500 mb-1 block">From</label>
                    <Input
                        type="date"
                        value={startDate}
                        onChange={(e) => onStartDateChange(e.target.value)}
                        className="h-10"
                    />
                </div>
                <div>
                    <label className="text-xs text-gray-500 mb-1 block">To</label>
                    <Input
                        type="date"
                        value={endDate}
                        onChange={(e) => onEndDateChange(e.target.value)}
                        className="h-10"
                    />
                </div>
            </div>

            <div className="flex gap-2">
                <Button onClick={onApply} size="sm" className="flex-1">
                    Apply
                </Button>
                {isFiltered && (
                    <Button onClick={onClear} variant="outline" size="sm" className="gap-1">
                        <X className="w-3 h-3" />
                        Clear
                    </Button>
                )}
            </div>
        </div>
    );
});

DateFilter.displayName = 'DateFilter';

/**
 * Payment Status Filter Component
 */
export const PaymentFilter: React.FC<PaymentFilterProps> = memo(({
    value,
    onChange
}) => {
    const statusOptions: { value: PaymentStatusFilter; label: string; color: string }[] = [
        { value: 'ALL', label: 'All', color: 'bg-gray-100 text-gray-700' },
        { value: 'PAID', label: 'Paid', color: 'bg-green-100 text-green-700' },
        { value: 'PARTIAL', label: 'Partial', color: 'bg-yellow-100 text-yellow-700' },
        { value: 'UNPAID', label: 'Unpaid', color: 'bg-red-100 text-red-700' },
    ];

    return (
        <div className="flex gap-2 flex-wrap">
            {statusOptions.map((option) => (
                <button
                    key={option.value}
                    onClick={() => onChange(option.value)}
                    className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all ${value === option.value
                        ? `${option.color} ring-2 ring-offset-1 ring-gray-300`
                        : 'bg-gray-50 text-gray-500 hover:bg-gray-100'
                        }`}
                >
                    {option.label}
                </button>
            ))}
        </div>
    );
});

PaymentFilter.displayName = 'PaymentFilter';

/**
 * Combined Filter Panel
 */
const FilterPanel: React.FC<FilterPanelProps> = memo(({
    dateFilter,
    paymentFilter,
    className = ''
}) => {
    return (
        <div className={`space-y-4 ${className}`}>
            {dateFilter && <DateFilter {...dateFilter} />}
            {paymentFilter && (
                <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100">
                    <div className="flex items-center gap-2 mb-3">
                        <Filter className="w-4 h-4 text-gray-500" />
                        <span className="font-medium text-sm text-gray-700">Payment Status</span>
                    </div>
                    <PaymentFilter {...paymentFilter} />
                </div>
            )}
        </div>
    );
});

FilterPanel.displayName = 'FilterPanel';

export default FilterPanel;
