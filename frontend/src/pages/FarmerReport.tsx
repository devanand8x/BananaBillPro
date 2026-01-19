import React, { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { useToast } from '@/hooks/use-toast';
import { farmerService, farmerReportService, FarmerReportData, Farmer, Bill } from '@/services/billService';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, ArrowLeft, Search, User, FileText, IndianRupee, Scale, AlertCircle, Download, Filter, CalendarDays } from 'lucide-react';
import { format } from 'date-fns';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';

const FarmerReport: React.FC = () => {
    const { t } = useLanguage();
    const { toast } = useToast();

    const [mobileNumber, setMobileNumber] = useState('');
    const [loading, setLoading] = useState(false);
    const [farmer, setFarmer] = useState<Farmer | null>(null);
    const [reportData, setReportData] = useState<FarmerReportData | null>(null);

    // Filter states
    const [showDateFilter, setShowDateFilter] = useState(false);
    const [showStatusFilter, setShowStatusFilter] = useState(false);
    const [startDate, setStartDate] = useState('');
    const [endDate, setEndDate] = useState('');
    const [paymentStatusFilter, setPaymentStatusFilter] = useState<'ALL' | 'PAID' | 'UNPAID'>('ALL');
    const [appliedStartDate, setAppliedStartDate] = useState('');  // Tracks date filter applied via Apply button
    const [appliedEndDate, setAppliedEndDate] = useState('');

    // Fetch report from backend with filters
    const fetchReport = useCallback(async (farmerId: string) => {
        setLoading(true);
        try {
            const report = await farmerReportService.getFarmerReport(farmerId, {
                startDate: startDate || undefined,
                endDate: endDate || undefined,
                paymentStatus: paymentStatusFilter,
            });
            setReportData(report);
        } catch (error) {
            console.error('Error fetching farmer report:', error);
            toast({ title: 'Error fetching report', variant: 'destructive' });
        } finally {
            setLoading(false);
        }
    }, [startDate, endDate, paymentStatusFilter, toast]);

    // Re-fetch when payment status filter or applied dates change (if farmer is selected)
    useEffect(() => {
        if (farmer) {
            farmerReportService.getFarmerReport(farmer.id, {
                startDate: appliedStartDate || undefined,
                endDate: appliedEndDate || undefined,
                paymentStatus: paymentStatusFilter,
            }).then(report => setReportData(report))
                .catch(error => {
                    console.error('Error fetching farmer report:', error);
                    toast({ title: 'Error fetching report', variant: 'destructive' });
                });
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [paymentStatusFilter, farmer, appliedStartDate, appliedEndDate]);

    const handleSearch = async () => {
        if (!mobileNumber || mobileNumber.length < 10) {
            toast({ title: 'Please enter valid 10-digit mobile number', variant: 'destructive' });
            return;
        }

        setLoading(true);
        try {
            // First find the farmer
            const foundFarmer = await farmerService.findByMobile(mobileNumber);
            if (!foundFarmer) {
                toast({ title: 'Farmer not found with this mobile number', variant: 'destructive' });
                setFarmer(null);
                setReportData(null);
                return;
            }

            setFarmer(foundFarmer);
            // fetchReport will be called automatically via useEffect

        } catch (error) {
            console.error('Error fetching farmer report:', error);
            toast({ title: 'Error fetching report', variant: 'destructive' });
        } finally {
            setLoading(false);
        }
    };

    const handleKeyPress = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') handleSearch();
    };

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('en-IN', {
            style: 'currency',
            currency: 'INR',
            maximumFractionDigits: 0,
        }).format(amount);
    };

    const exportToPDF = () => {
        if (!reportData || !farmer) return;

        const doc = new jsPDF();

        // Header
        doc.setFontSize(20);
        doc.text('Farmer Report', 105, 15, { align: 'center' });

        doc.setFontSize(14);
        doc.text(farmer.name, 105, 25, { align: 'center' });
        doc.setFontSize(10);
        doc.text(`Mobile: ${farmer.mobileNumber}`, 105, 32, { align: 'center' });

        // Stats
        doc.setFontSize(11);
        doc.text(`Total Bills: ${reportData.totalBills}`, 14, 45);
        doc.text(`Total Amount: Rs. ${reportData.totalAmount.toLocaleString('en-IN')}`, 14, 52);
        doc.text(`Total Weight: ${(reportData.totalWeight ?? 0).toFixed(2)} Kg`, 14, 59);
        doc.text(`Unpaid: Rs. ${reportData.unpaidAmount.toLocaleString('en-IN')} (${reportData.unpaidBills} bills)`, 120, 45);

        // Bills Table
        const tableData = reportData.bills.map((bill: Bill) => [
            bill.billNumber,
            format(new Date(bill.createdAt), 'dd/MM/yy'),
            bill.finalNetWeight?.toFixed(1) || '0',
            `Rs. ${bill.netAmount.toLocaleString('en-IN')}`,
            bill.paymentStatus || 'UNPAID'
        ]);

        autoTable(doc, {
            head: [['Bill No', 'Date', 'Weight (Kg)', 'Amount', 'Status']],
            body: tableData,
            startY: 70,
            styles: { fontSize: 9 },
            headStyles: { fillColor: [76, 175, 80] },
        });

        doc.save(`${farmer.name}_report.pdf`);
        toast({ title: 'PDF exported successfully!' });
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-background via-muted/20 to-background">
            <header className="header-bar">
                <div className="max-w-4xl mx-auto px-3 sm:px-4 py-3 sm:py-4 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <Link to="/reports">
                            <Button variant="ghost" size="icon" className="hover:bg-muted">
                                <ArrowLeft className="w-10 h-10 text-gray-800" />
                            </Button>
                        </Link>
                        <div className="flex items-center gap-3">
                            <div className="logo-container w-10 h-10">
                                <Banana className="w-5 h-5 text-primary-foreground" />
                            </div>
                            <span className="font-bold text-foreground text-lg">{t('farmerReport')}</span>
                        </div>
                    </div>
                    <LanguageSwitch />
                </div>
            </header>

            <main className="max-w-4xl mx-auto px-3 sm:px-4 py-4 sm:py-6">
                {/* Search Section */}
                <div className="form-section mb-6 animate-fade-in">
                    <div className="flex items-center gap-2 mb-3">
                        <Search className="w-5 h-5 text-primary" />
                        <span className="font-medium text-foreground">{t('searchFarmerByMobile')}</span>
                    </div>
                    <div className="flex gap-3">
                        <Input
                            type="tel"
                            value={mobileNumber}
                            onChange={(e) => setMobileNumber(e.target.value)}
                            onKeyPress={handleKeyPress}
                            placeholder="Enter 10-digit mobile number"
                            className="input-field flex-1"
                            maxLength={10}
                        />
                        <Button onClick={handleSearch} className="btn-primary px-6" disabled={loading}>
                            {loading ? (
                                <span className="w-5 h-5 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                            ) : 'Search'}
                        </Button>
                    </div>
                </div>

                {/* Compact Filters - Only show when data is loaded */}
                {reportData && (
                    <div className="form-section mb-4 animate-fade-in">
                        {/* Filter Buttons */}
                        <div className="flex gap-3 mb-4">
                            {/* Date Filter Toggle */}
                            <Button
                                variant="outline"
                                onClick={() => setShowDateFilter(!showDateFilter)}
                                className={`gap-2 rounded-xl ${showDateFilter ? 'border-yellow-400 bg-yellow-50 text-yellow-600' : 'hover:border-yellow-400'}`}
                            >
                                <CalendarDays className="w-4 h-4" />
                                {showDateFilter ? 'Hide Date Filter' : 'Filter by Date'}
                            </Button>

                            {/* Payment Status Toggle */}
                            <Button
                                variant="outline"
                                onClick={() => setShowStatusFilter(!showStatusFilter)}
                                className={`gap-2 rounded-xl ${showStatusFilter ? 'border-yellow-400 bg-yellow-50 text-yellow-600' : 'hover:border-yellow-400'}`}
                            >
                                <Filter className="w-4 h-4" />
                                {showStatusFilter ? 'Hide Status Filter' : 'Filter by Status'}
                            </Button>
                        </div>

                        {/* Date Filter */}
                        {showDateFilter && (
                            <div className="form-section mb-4 animate-fade-in">
                                <div className="flex flex-wrap gap-4 items-end">
                                    <div>
                                        <label className="text-sm text-muted-foreground block mb-1">Start Date</label>
                                        <Input
                                            type="date"
                                            value={startDate}
                                            onChange={(e) => setStartDate(e.target.value)}
                                            className="w-40 cursor-pointer"
                                            onClick={(e) => (e.target as HTMLInputElement).showPicker?.()}
                                        />
                                    </div>
                                    <div>
                                        <label className="text-sm text-muted-foreground block mb-1">End Date</label>
                                        <Input
                                            type="date"
                                            value={endDate}
                                            min={startDate}
                                            onChange={(e) => setEndDate(e.target.value)}
                                            className="w-40 cursor-pointer"
                                            onClick={(e) => (e.target as HTMLInputElement).showPicker?.()}
                                        />
                                    </div>
                                    <Button onClick={() => { setAppliedStartDate(startDate); setAppliedEndDate(endDate); }} className="btn-primary">Apply</Button>
                                    <Button onClick={() => { setStartDate(''); setEndDate(''); setAppliedStartDate(''); setAppliedEndDate(''); }} variant="outline">Clear All</Button>
                                </div>
                            </div>
                        )}

                        {/* Status Filter */}
                        {showStatusFilter && (
                            <div className="flex gap-3 flex-wrap">
                                <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => setPaymentStatusFilter('ALL')}
                                    className={`rounded-full px-6 py-2 ${paymentStatusFilter === 'ALL'
                                        ? 'bg-yellow-500 hover:bg-yellow-600 text-gray-900 border-yellow-500'
                                        : 'bg-white border-gray-300 hover:bg-gray-50 text-gray-700 hover:text-black'
                                        }`}
                                >
                                    All
                                </Button>
                                <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => setPaymentStatusFilter('PAID')}
                                    className={`rounded-full px-6 py-2 ${paymentStatusFilter === 'PAID'
                                        ? 'bg-green-500 hover:bg-green-600 text-white border-green-500'
                                        : 'bg-white border-green-500 hover:bg-green-50 text-green-600 hover:text-black'
                                        }`}
                                >
                                    ✅ Paid
                                </Button>
                                <Button
                                    size="sm"
                                    variant="outline"
                                    onClick={() => setPaymentStatusFilter('UNPAID')}
                                    className={`rounded-full px-6 py-2 ${paymentStatusFilter === 'UNPAID'
                                        ? 'bg-red-500 hover:bg-red-600 text-white border-red-500'
                                        : 'bg-white border-red-500 hover:bg-red-50 text-red-600 hover:text-black'
                                        }`}
                                >
                                    ❌ Unpaid
                                </Button>
                            </div>
                        )}
                    </div>
                )
                }

                {/* Farmer Info Card - Now uses backend-filtered data directly */}
                {
                    farmer && reportData && (
                        <div className="animate-fade-in">
                            {/* Farmer Header */}
                            <div className="form-section mb-4 bg-gradient-to-r from-primary/10 to-accent/10">
                                <div className="flex items-center justify-between">
                                    <div className="flex items-center gap-4">
                                        <div className="w-16 h-16 rounded-full bg-primary/20 flex items-center justify-center">
                                            <User className="w-8 h-8 text-primary" />
                                        </div>
                                        <div>
                                            <h2 className="text-xl font-bold text-foreground">{farmer.name}</h2>
                                            <p className="text-muted-foreground">{farmer.mobileNumber}</p>
                                        </div>
                                    </div>
                                    <Button onClick={exportToPDF} variant="outline" className="gap-2 border-red-500 text-red-600 hover:bg-red-600 hover:text-white">
                                        <Download className="w-4 h-4" />
                                        Export PDF
                                    </Button>
                                </div>
                            </div>

                            {/* Stats Grid - Uses backend-filtered stats */}
                            <div className="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
                                <div className="stat-card p-4">
                                    <div className="flex items-center gap-2 mb-2">
                                        <FileText className="w-4 h-4 text-primary" />
                                        <span className="text-xs text-muted-foreground">{reportData.isFiltered ? 'Filtered Bills' : 'Total Bills'}</span>
                                    </div>
                                    <p className="text-2xl font-bold text-foreground">{reportData.totalBills}</p>
                                    {reportData.isFiltered && <p className="text-xs text-muted-foreground">of {reportData.totalBillsUnfiltered}</p>}
                                </div>

                                <div className="stat-card p-4">
                                    <div className="flex items-center gap-2 mb-2">
                                        <IndianRupee className="w-4 h-4 text-green-600" />
                                        <span className="text-xs text-muted-foreground">{reportData.isFiltered ? 'Filtered Amount' : 'Total Amount'}</span>
                                    </div>
                                    <p className="text-2xl font-bold text-green-600">{formatCurrency(reportData.totalAmount ?? 0)}</p>
                                </div>

                                <div className="stat-card p-4">
                                    <div className="flex items-center gap-2 mb-2">
                                        <Scale className="w-4 h-4 text-blue-600" />
                                        <span className="text-xs text-muted-foreground">{reportData.isFiltered ? 'Filtered Weight' : 'Total Weight'}</span>
                                    </div>
                                    <p className="text-2xl font-bold text-blue-600">{(reportData.totalWeight ?? 0).toFixed(1)} Kg</p>
                                </div>

                                <div className="stat-card p-4">
                                    <div className="flex items-center gap-2 mb-2">
                                        <AlertCircle className="w-4 h-4 text-red-600" />
                                        <span className="text-xs text-muted-foreground">Unpaid Amount</span>
                                    </div>
                                    <p className="text-2xl font-bold text-red-600">{formatCurrency(reportData.unpaidAmount ?? 0)}</p>
                                    <p className="text-xs text-muted-foreground">{reportData.unpaidBills} bills pending</p>
                                </div>
                            </div>

                            {/* Bills List - Uses backend-filtered bills */}
                            <div className="form-section">
                                <h3 className="font-bold text-foreground mb-4 flex items-center gap-2">
                                    <FileText className="w-5 h-5 text-primary" />
                                    Bills ({reportData.totalBills}{reportData.isFiltered ? ` of ${reportData.totalBillsUnfiltered}` : ''})
                                </h3>

                                {reportData.bills.length === 0 ? (
                                    <p className="text-center text-muted-foreground py-8">No bills match the filter criteria</p>
                                ) : (
                                    <div className="space-y-3">
                                        {reportData.bills.map((bill: Bill, index: number) => (
                                            <div key={bill.id} className="flex items-center justify-between p-3 bg-muted/30 rounded-lg hover:bg-muted/50 transition-colors" style={{ animationDelay: `${index * 0.05}s` }}>
                                                <div>
                                                    <p className="font-semibold text-foreground">{bill.billNumber}</p>
                                                    <p className="text-sm text-muted-foreground">{format(new Date(bill.createdAt), 'dd MMM yyyy')}</p>
                                                </div>
                                                <div className="text-right">
                                                    <p className="font-bold text-foreground">{formatCurrency(bill.netAmount)}</p>
                                                    <span className={`text-xs px-2 py-0.5 rounded-full ${bill.paymentStatus === 'PAID'
                                                        ? 'bg-green-100 text-green-700'
                                                        : 'bg-red-100 text-red-700'
                                                        }`}>
                                                        {bill.paymentStatus || 'UNPAID'}
                                                    </span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                )}
                            </div>
                        </div>
                    )
                }

                {/* No Results */}
                {
                    mobileNumber && !loading && !farmer && (
                        <div className="text-center py-16 animate-fade-in">
                            <div className="w-20 h-20 mx-auto rounded-full bg-muted/50 flex items-center justify-center mb-4">
                                <User className="w-10 h-10 text-muted-foreground" />
                            </div>
                            <p className="text-muted-foreground text-lg">No farmer found</p>
                            <p className="text-muted-foreground/70 text-sm mt-1">Try searching with a different mobile number</p>
                        </div>
                    )
                }
            </main >
        </div >
    );
};

export default FarmerReport;
