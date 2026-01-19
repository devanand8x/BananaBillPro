import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useToast } from '@/hooks/use-toast';
import { reportService, BillWithFarmer } from '@/services/billService';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, ArrowLeft, FileText, IndianRupee, Scale, Users, Calendar, ChevronDown, Filter, Download, User } from 'lucide-react';
import { format } from 'date-fns';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

interface FarmerStat {
    name: string;
    mobile: string;
    billCount: number;
    totalAmount: number;
    totalWeight: number;
}

interface MonthlyReportData {
    year: number;
    month: number;
    monthName: string;
    totalBills: number;
    totalAmount: number;
    averageAmount: number;
    totalWeight: number;
    farmers: FarmerStat[];
    bills: BillWithFarmer[];
}

const Reports: React.FC = () => {
    const { t } = useLanguage();
    const { toast } = useToast();

    const [loading, setLoading] = useState(false);
    const [selectedYear, setSelectedYear] = useState(new Date().getFullYear());
    const [selectedMonth, setSelectedMonth] = useState(new Date().getMonth() + 1);
    const [reportData, setReportData] = useState<MonthlyReportData | null>(null);

    // Date filter states
    const [showDateFilter, setShowDateFilter] = useState(false);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [appliedStartDate, setAppliedStartDate] = useState('');  // Tracks applied date filter
    const [appliedEndDate, setAppliedEndDate] = useState('');
    const [filteredBills, setFilteredBills] = useState<BillWithFarmer[] | null>(null);

    const months = [
        { value: 1, label: 'January' },
        { value: 2, label: 'February' },
        { value: 3, label: 'March' },
        { value: 4, label: 'April' },
        { value: 5, label: 'May' },
        { value: 6, label: 'June' },
        { value: 7, label: 'July' },
        { value: 8, label: 'August' },
        { value: 9, label: 'September' },
        { value: 10, label: 'October' },
        { value: 11, label: 'November' },
        { value: 12, label: 'December' },
    ];

    // Years from 2021 to 2040
    const years = Array.from({ length: 20 }, (_, i) => 2021 + i);

    const fetchReport = async (useAppliedDates = false) => {
        setLoading(true);
        try {
            let data;
            const sDate = useAppliedDates ? appliedStartDate : '';
            const eDate = useAppliedDates ? appliedEndDate : '';
            // If date range is applied, use date range API
            if (sDate && eDate) {
                data = await reportService.getDateRangeReport(sDate, eDate);
                setFilteredBills(null); // Reset client-side filter
            } else {
                // Otherwise use monthly report
                data = await reportService.getMonthlyReport(selectedYear, selectedMonth);
            }
            setReportData(data);
        } catch (error) {
            console.error('Error fetching report:', error);
            toast({ title: 'Error loading report', variant: 'destructive' });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchReport();
    }, []);

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0,
        }).format(amount);
    };

    const applyDateFilter = () => {
        if (!startDate || !endDate) {
            toast({ title: 'Please select both start and end dates', variant: 'destructive' });
            return;
        }
        // Set applied dates and fetch report
        setAppliedStartDate(startDate);
        setAppliedEndDate(endDate);
    };

    // Re-fetch when applied dates change
    useEffect(() => {
        if (appliedStartDate && appliedEndDate) {
            fetchReport(true);
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [appliedStartDate, appliedEndDate]);

    const clearDateFilter = () => {
        setStartDate('');
        setEndDate('');
        setAppliedStartDate('');
        setAppliedEndDate('');
        setFilteredBills(null);
        // Re-fetch monthly report
        fetchReport(false);
    };

    // Export to CSV function
    const exportToCSV = () => {
        const billsToExport = filteredBills || (reportData?.bills ?? []);

        if (!reportData || billsToExport.length === 0) {
            toast({ title: 'No data to export', variant: 'destructive' });
            return;
        }

        // CSV Headers
        const headers = ['Bill No', 'Date', 'Farmer Name', 'Mobile', 'Net Weight (Kg)', 'Rate/Kg', 'Total Amount', 'Majuri', 'Net Amount', 'Payment Status'];

        // CSV Rows - using correct Bill interface fields
        const rows = billsToExport.map(bill => [
            bill.billNumber,
            format(new Date(bill.createdAt), 'dd/MM/yyyy'),
            bill.farmer.name,
            bill.farmer.mobileNumber,
            bill.finalNetWeight?.toFixed(2) || '0',
            bill.ratePerKg?.toFixed(2) || '0',
            bill.totalAmount?.toFixed(2) || '0',
            bill.majuri?.toFixed(2) || '0',
            bill.netAmount?.toFixed(2) || '0',
            bill.paymentStatus || 'UNPAID'
        ]);

        // Create CSV content
        const csvContent = [
            headers.join(','),
            ...rows.map(row => row.join(','))
        ].join('\n');

        // Create download link
        const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
        const link = document.createElement('a');
        const url = URL.createObjectURL(blob);
        link.setAttribute('href', url);
        link.setAttribute('download', `report_${reportData.monthName}_${reportData.year}.csv`);
        link.style.visibility = 'hidden';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        toast({ title: 'CSV exported successfully!' });
    };

    // Export to PDF function
    const exportToPDF = () => {
        const billsToExport = filteredBills || (reportData?.bills ?? []);

        if (!reportData || billsToExport.length === 0) {
            toast({ title: 'No data to export', variant: 'destructive' });
            return;
        }

        // Create PDF document
        const doc = new jsPDF();

        // Header
        doc.setFontSize(20);
        doc.setTextColor(40, 40, 40);
        doc.text('Banana Bill Pro', 105, 15, { align: 'center' });

        doc.setFontSize(14);
        doc.setTextColor(80, 80, 80);
        doc.text(`Monthly Report - ${reportData.monthName} ${reportData.year}`, 105, 25, { align: 'center' });

        // Summary Stats
        doc.setFontSize(11);
        doc.setTextColor(60, 60, 60);
        doc.text(`Total Bills: ${reportData.totalBills}`, 14, 40);
        doc.text(`Total Amount: Rs. ${reportData.totalAmount.toLocaleString('en-IN')}`, 14, 48);
        doc.text(`Total Weight: ${(reportData.totalWeight ?? 0).toFixed(2)} Kg`, 14, 56);
        doc.text(`Average Bill: Rs. ${(reportData.averageAmount ?? 0).toFixed(2)}`, 120, 40);
        doc.text(`Generated: ${format(new Date(), 'dd/MM/yyyy HH:mm')}`, 120, 48);

        // Bills Table
        const tableData = billsToExport.map(bill => [
            bill.billNumber,
            format(new Date(bill.createdAt), 'dd/MM/yy'),
            bill.farmer.name,
            bill.finalNetWeight?.toFixed(1) || '0',
            `Rs. ${bill.netAmount.toLocaleString('en-IN')}`,
            bill.paymentStatus || 'UNPAID'
        ]);

        autoTable(doc, {
            head: [['Bill No', 'Date', 'Farmer', 'Weight (Kg)', 'Amount', 'Status']],
            body: tableData,
            startY: 65,
            styles: { fontSize: 9, cellPadding: 2 },
            headStyles: { fillColor: [76, 175, 80], textColor: 255 },
            alternateRowStyles: { fillColor: [245, 245, 245] },
        });

        // Footer
        const pageCount = doc.getNumberOfPages();
        for (let i = 1; i <= pageCount; i++) {
            doc.setPage(i);
            doc.setFontSize(8);
            doc.setTextColor(150, 150, 150);
            doc.text(`Page ${i} of ${pageCount} | Banana Bill Pro`, 105, doc.internal.pageSize.height - 10, { align: 'center' });
        }

        // Save PDF
        doc.save(`report_${reportData.monthName}_${reportData.year}.pdf`);
        toast({ title: 'PDF exported successfully!' });
    };

    // Get bills to display (filtered or all)
    const displayBills = filteredBills || (reportData?.bills ?? []);

    return (
        <div className="min-h-screen bg-gradient-to-br from-background via-muted/20 to-background">
            <header className="header-bar">
                <div className="max-w-4xl mx-auto px-3 sm:px-4 py-3 sm:py-4 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <Link to="/dashboard">
                            <Button
                                variant="ghost"
                                size="icon"
                                className="hover:bg-muted"
                            >
                                <ArrowLeft className="w-10 h-10 text-gray-800" />
                            </Button>
                        </Link>
                        <div className="flex items-center gap-3">
                            <div className="logo-container w-10 h-10">
                                <Banana className="w-5 h-5 text-primary-foreground" />
                            </div>
                            <span className="font-bold text-foreground text-lg">{t('monthlyReports')}</span>
                        </div>
                    </div>
                    <LanguageSwitch />
                </div>
            </header>

            <main className="max-w-4xl mx-auto px-3 sm:px-4 py-4 sm:py-6 pb-6 sm:pb-8">
                {/* Farmer Report Button */}
                <Link to="/farmer-report" className="block mb-4">
                    <Button variant="outline" className="w-full h-14 gap-3 border-2 rounded-xl border-green-500/50 bg-green-500/5 hover:bg-green-500/10 text-green-700 hover:text-green-800 transition-all">
                        <User className="w-5 h-5" />
                        {t('farmerWiseReport')}
                    </Button>
                </Link>

                {/* Month/Year Selector */}
                <div className="form-section mb-4 sm:mb-6 animate-fade-in">
                    <div className="grid grid-cols-2 gap-2 sm:gap-3 mb-3">
                        <div>
                            <label className="text-xs text-muted-foreground mb-1 block">{t('month')}</label>
                            <div className="relative">
                                <select
                                    value={selectedMonth}
                                    onChange={(e) => setSelectedMonth(Number(e.target.value))}
                                    className="w-full h-11 sm:h-12 px-3 sm:px-4 pr-10 rounded-xl border-2 border-input bg-background text-foreground text-sm sm:text-base appearance-none cursor-pointer focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                >
                                    {months.map((m) => (
                                        <option key={m.value} value={m.value}>{m.label}</option>
                                    ))}
                                </select>
                                <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 sm:w-5 sm:h-5 text-muted-foreground pointer-events-none" />
                            </div>
                        </div>
                        <div>
                            <label className="text-xs text-muted-foreground mb-1 block">{t('year')}</label>
                            <div className="relative">
                                <select
                                    value={selectedYear}
                                    onChange={(e) => setSelectedYear(Number(e.target.value))}
                                    className="w-full h-11 sm:h-12 px-3 sm:px-4 pr-10 rounded-xl border-2 border-input bg-background text-foreground text-sm sm:text-base appearance-none cursor-pointer focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
                                >
                                    {years.map((y) => (
                                        <option key={y} value={y}>{y}</option>
                                    ))}
                                </select>
                                <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 sm:w-5 sm:h-5 text-muted-foreground pointer-events-none" />
                            </div>
                        </div>
                    </div>
                    <div className="flex flex-col sm:flex-row gap-2 sm:gap-3">
                        <Button onClick={() => fetchReport(false)} className="btn-primary h-11 sm:h-12 px-4 sm:px-6 flex-1 text-sm sm:text-base" disabled={loading}>
                            {loading ? (
                                <span className="w-5 h-5 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                            ) : t('generateReport')}
                        </Button>
                        {reportData && (
                            <Button onClick={exportToPDF} variant="outline" className="h-11 sm:h-12 px-4 sm:px-6 gap-2 border-red-500 text-red-600 hover:bg-red-600 hover:text-white hover:border-red-600 text-sm sm:text-base">
                                <FileText className="w-4 h-4" />
                                {t('exportPdf')}
                            </Button>
                        )}
                    </div>

                    {/* Date Filter Toggle */}
                    {reportData && (
                        <div className="mt-4 flex flex-wrap gap-3 items-end">
                            <Button
                                variant="outline"
                                onClick={() => setShowDateFilter(!showDateFilter)}
                                className={`gap-2 h-10 rounded-xl ${showDateFilter ? 'border-yellow-400 bg-yellow-50 text-yellow-600' : 'hover:border-yellow-400'}`}
                            >
                                <Filter className="w-4 h-4" />
                                {showDateFilter ? t('hideDateFilter') : t('filterByDate')}
                            </Button>

                            {showDateFilter && (
                                <>
                                    <div>
                                        <label className="text-sm text-muted-foreground block mb-1">{t('startDate')}</label>
                                        <Input
                                            type="date"
                                            value={startDate}
                                            onChange={(e) => setStartDate(e.target.value)}
                                            className="w-40 cursor-pointer"
                                            onClick={(e) => (e.target as HTMLInputElement).showPicker?.()}
                                        />
                                    </div>
                                    <div>
                                        <label className="text-sm text-muted-foreground block mb-1">{t('endDate')}</label>
                                        <Input
                                            type="date"
                                            value={endDate}
                                            min={startDate}
                                            onChange={(e) => setEndDate(e.target.value)}
                                            className="w-40 cursor-pointer"
                                            onClick={(e) => (e.target as HTMLInputElement).showPicker?.()}
                                        />
                                    </div>
                                    <Button onClick={applyDateFilter} className="btn-primary">{t('apply')}</Button>
                                    <Button variant="outline" onClick={clearDateFilter}>{t('clearAll')}</Button>
                                </>
                            )}
                        </div>
                    )}
                </div>

                {/* Summary Cards */}
                {reportData && (
                    <div className="grid grid-cols-2 gap-2 sm:gap-3 mb-4 sm:mb-6 animate-fade-in">
                        <div className="stat-card p-3 sm:p-4">
                            <div className="flex items-center gap-1.5 sm:gap-2 mb-1 sm:mb-2">
                                <FileText className="w-4 h-4 sm:w-5 sm:h-5 text-primary" />
                                <span className="text-[10px] sm:text-xs text-muted-foreground">{t('totalBills')}</span>
                            </div>
                            <p className="text-lg sm:text-2xl font-bold text-foreground">{reportData.totalBills}</p>
                        </div>

                        <div className="stat-card p-3 sm:p-4">
                            <div className="flex items-center gap-1.5 sm:gap-2 mb-1 sm:mb-2">
                                <IndianRupee className="w-4 h-4 sm:w-5 sm:h-5 text-accent" />
                                <span className="text-[10px] sm:text-xs text-muted-foreground">{t('totalAmount')}</span>
                            </div>
                            <p className="text-base sm:text-2xl font-bold text-accent truncate">{formatCurrency(reportData.totalAmount)}</p>
                        </div>

                        <div className="stat-card p-3 sm:p-4">
                            <div className="flex items-center gap-1.5 sm:gap-2 mb-1 sm:mb-2">
                                <Scale className="w-4 h-4 sm:w-5 sm:h-5 text-warning" />
                                <span className="text-[10px] sm:text-xs text-muted-foreground">{t('totalWeight')}</span>
                            </div>
                            <p className="text-lg sm:text-2xl font-bold text-foreground">{(reportData.totalWeight ?? 0).toFixed(0)} kg</p>
                        </div>

                        <div className="stat-card p-3 sm:p-4">
                            <div className="flex items-center gap-1.5 sm:gap-2 mb-1 sm:mb-2">
                                <Users className="w-4 h-4 sm:w-5 sm:h-5 text-primary" />
                                <span className="text-[10px] sm:text-xs text-muted-foreground">{t('farmers')}</span>
                            </div>
                            <p className="text-lg sm:text-2xl font-bold text-foreground">{reportData.farmers.length}</p>
                        </div>
                    </div>
                )}

                {/* Top Farmers */}
                {reportData && reportData.farmers.length > 0 && (
                    <div className="form-section mb-6 animate-fade-in">
                        <h3 className="font-semibold text-foreground mb-4 flex items-center gap-2">
                            <Users className="w-5 h-5 text-primary" />
                            {t('topFarmers')}
                        </h3>
                        <div className="space-y-3">
                            {reportData.farmers.slice(0, 5).map((farmer, index) => (
                                <div key={index} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg">
                                    <div>
                                        <p className="font-medium text-foreground">{farmer.name}</p>
                                        <p className="text-sm text-muted-foreground">{farmer.mobile} • {farmer.billCount} bills</p>
                                    </div>
                                    <div className="text-right">
                                        <p className="font-bold text-accent">{formatCurrency(farmer.totalAmount)}</p>
                                        <p className="text-xs text-muted-foreground">{(farmer.totalWeight ?? 0).toFixed(0)} kg</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                {/* Bills Table */}
                {reportData && displayBills.length > 0 && (
                    <div className="form-section animate-fade-in">
                        <h3 className="font-semibold text-foreground mb-4 flex items-center gap-2">
                            <FileText className="w-5 h-5 text-primary" />
                            {filteredBills ? `Filtered Bills (${displayBills.length})` : `All Bills (${reportData.bills.length})`}
                        </h3>
                        <div className="space-y-3">
                            {displayBills.map((bill) => (
                                <Link key={bill.id} to={`/bill/${bill.id}`}>
                                    <div className="flex items-center justify-between p-3 bg-muted/30 rounded-lg hover:bg-muted/50 transition-colors cursor-pointer">
                                        <div>
                                            <p className="font-medium text-foreground">{bill.billNumber}</p>
                                            <p className="text-sm text-muted-foreground">
                                                {bill.farmer.name} • {format(new Date(bill.createdAt), 'dd MMM yyyy')}
                                            </p>
                                        </div>
                                        <p className="font-bold text-accent">{formatCurrency(bill.netAmount)}</p>
                                    </div>
                                </Link>
                            ))}
                        </div>
                    </div>
                )}

                {/* No Data */}
                {reportData && reportData.bills.length === 0 && !loading && (
                    <div className="form-section text-center py-12 animate-fade-in">
                        <Calendar className="w-12 h-12 text-muted-foreground mx-auto mb-4" />
                        <p className="text-muted-foreground">No bills found for {months.find(m => m.value === selectedMonth)?.label} {selectedYear}</p>
                    </div>
                )}
            </main>
        </div>
    );
};

export default Reports;
