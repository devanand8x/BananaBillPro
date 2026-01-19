import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { billService, Bill } from '@/services/billService';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, ArrowLeft, Search, Eye, Plus, FileText, Trash2, Edit, Calendar, Filter } from 'lucide-react';
import { format } from 'date-fns';
import { useToast } from '@/hooks/use-toast';

const BillHistory: React.FC = () => {
  const { t } = useLanguage();
  const navigate = useNavigate();
  const { toast } = useToast();
  const [searchMobile, setSearchMobile] = useState('');
  const [bills, setBills] = useState<Bill[]>([]);
  const [allBills, setAllBills] = useState<Bill[]>([]);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [mobileSearched, setMobileSearched] = useState(false);
  const [showDateFilter, setShowDateFilter] = useState(false);
  const [showStatusFilter, setShowStatusFilter] = useState(false);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [paymentStatus, setPaymentStatus] = useState<string[]>(['PAID', 'UNPAID', 'PARTIAL']);

  // Load recent bills on mount
  useEffect(() => {
    loadRecentBills();
  }, []);

  const loadRecentBills = async () => {
    setLoading(true);
    try {
      const results = await billService.getRecent(100);
      console.log('Recent bills payment statuses:', results.map(b => ({
        billNumber: b.billNumber,
        paymentStatus: b.paymentStatus,
        netAmount: b.netAmount
      })));
      setAllBills(results);
      setBills(results);
    } catch (error) {
      console.error('Error loading bills:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async () => {
    if (!searchMobile) {
      await loadRecentBills();
      setMobileSearched(false);
      return;
    }
    setLoading(true);
    setSearched(true);
    setMobileSearched(true); // Set when actual mobile search is done
    setPaymentStatus(['PAID', 'UNPAID', 'PARTIAL']); // Reset filter to All
    try {
      const results = await billService.getByFarmerMobile(searchMobile);
      setAllBills(results); // Store for local filtering
      setBills(results);
    } catch (error) {
      console.error('Error searching bills:', error);
      setAllBills([]);
      setBills([]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleSearch();
  };

  const handleDelete = async (billId: string) => {
    if (!confirm('Are you sure you want to delete this bill?')) return;
    try {
      await billService.delete(billId);
      setBills(bills.filter(b => b.id !== billId));
      setAllBills(allBills.filter(b => b.id !== billId));
      toast({ title: 'Bill deleted successfully' });
    } catch (error) {
      toast({ title: 'Error deleting bill', variant: 'destructive' });
    }
  };

  const applyFilters = async () => {
    // If mobile search is active, filter locally
    if (mobileSearched && allBills.length > 0) {
      let filteredBills = [...allBills];

      console.log('Applying filters locally:', { startDate, endDate, paymentStatus, allBillsCount: allBills.length });
      console.log('Bills dates:', allBills.map(b => ({ bill: b.billNumber, date: b.createdAt })));

      // Apply date filter
      if (startDate) {
        const start = new Date(startDate);
        start.setHours(0, 0, 0, 0); // Start of day
        console.log('Start date filter:', start);
        filteredBills = filteredBills.filter(bill => {
          const billDate = new Date(bill.createdAt);
          return billDate >= start;
        });
        console.log('After start filter:', filteredBills.length);
      }
      if (endDate) {
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999); // Include the entire end day
        console.log('End date filter:', end);
        filteredBills = filteredBills.filter(bill => {
          const billDate = new Date(bill.createdAt);
          return billDate <= end;
        });
        console.log('After end filter:', filteredBills.length);
      }

      // Apply payment status filter
      if (paymentStatus.length < 3) {
        filteredBills = filteredBills.filter(bill => paymentStatus.includes(bill.paymentStatus));
        console.log('After status filter:', filteredBills.length);
      }

      console.log('Final filtered bills:', filteredBills.length);
      setBills(filteredBills);
      return;
    }

    // Otherwise fetch from backend
    setLoading(true);
    try {
      const results = await billService.searchWithFilters({
        mobile: searchMobile || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        paymentStatus: paymentStatus.length < 3 ? paymentStatus.join(',') : undefined
      });
      setBills(results);
      setSearched(true);
    } catch (error) {
      console.error('Error applying filters:', error);
    } finally {
      setLoading(false);
    }
  };

  const clearFilters = () => {
    setStartDate('');
    setEndDate('');
    setPaymentStatus(['PAID', 'UNPAID', 'PARTIAL']);
    setSearchMobile('');
    setBills(allBills);
    setSearched(false);
  };

  const toggleStatus = async (status: string) => {
    // Radio-style: clicking a status selects only that status
    // If already selected, go back to All
    let newStatus: string[];
    if (paymentStatus.length === 1 && paymentStatus.includes(status)) {
      newStatus = ['PAID', 'UNPAID', 'PARTIAL'];
    } else {
      newStatus = [status];
    }
    setPaymentStatus(newStatus);

    // If mobile search is active, filter the current bills locally
    if (mobileSearched && allBills.length > 0) {
      let filteredBills = [...allBills];

      // Apply date filter first
      if (startDate) {
        const start = new Date(startDate);
        start.setHours(0, 0, 0, 0);
        filteredBills = filteredBills.filter(bill => new Date(bill.createdAt) >= start);
      }
      if (endDate) {
        const end = new Date(endDate);
        end.setHours(23, 59, 59, 999);
        filteredBills = filteredBills.filter(bill => new Date(bill.createdAt) <= end);
      }

      // Then apply payment status filter
      if (newStatus.length < 3) {
        filteredBills = filteredBills.filter(bill => newStatus.includes(bill.paymentStatus));
      }

      setBills(filteredBills);
      return;
    }

    // Otherwise fetch from backend
    setLoading(true);
    try {
      const results = await billService.searchWithFilters({
        mobile: searchMobile || undefined,
        startDate: startDate || undefined,
        endDate: endDate || undefined,
        paymentStatus: newStatus.length < 3 ? newStatus.join(',') : undefined
      });
      console.log('Filter results:', results, 'for status:', newStatus);
      setBills(results);
      setSearched(true);
    } catch (error) {
      console.error('Error applying filters:', error);
    } finally {
      setLoading(false);
    }
  };

  const totalAmount = bills.reduce((sum, bill) => sum + (bill.netAmount ?? 0), 0);

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-muted/20 to-background">
      <header className="header-bar">
        <div className="max-w-4xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <Link to="/dashboard"><Button variant="ghost" size="icon" className="hover:bg-muted"><ArrowLeft className="w-10 h-10 text-gray-800" /></Button></Link>
            <div className="flex items-center gap-3">
              <div className="logo-container w-10 h-10">
                <Banana className="w-5 h-5 text-primary-foreground" />
              </div>
              <span className="font-bold text-foreground text-lg">{t('billHistory')}</span>
            </div>
          </div>
          <LanguageSwitch />
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-6">
        {/* Search Box */}
        <div className="form-section mb-4 animate-fade-in">
          <div className="flex gap-3">
            <div className="flex-1 relative group">
              <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
              <Input
                type="tel"
                value={searchMobile}
                onChange={(e) => setSearchMobile(e.target.value)}
                onKeyPress={handleKeyPress}
                className="input-field pl-12"
                placeholder={t('searchByMobile')}
              />
            </div>
            <Button onClick={handleSearch} className="btn-primary px-6" disabled={loading}>
              {loading ? (
                <span className="w-5 h-5 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
              ) : 'Search'}
            </Button>
            {mobileSearched && (
              <Button
                variant="outline"
                onClick={() => {
                  setSearchMobile('');
                  setMobileSearched(false);
                  loadRecentBills();
                }}
                className="px-4"
              >
                Show Recent
              </Button>
            )}
          </div>
        </div>


        <div className="form-section mb-4 animate-fade-in">
          {/* Filter Buttons */}
          <div className="flex gap-3 mb-4">
            <Button
              variant="outline"
              onClick={() => setShowDateFilter(!showDateFilter)}
              className={`gap-2 rounded-xl ${showDateFilter ? 'border-yellow-400 bg-yellow-50 text-yellow-600' : 'hover:border-yellow-400'}`}
            >
              <Calendar className="w-4 h-4" />
              {showDateFilter ? 'Hide Date Filter' : 'Filter by Date'}
            </Button>
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
                <Button onClick={applyFilters} className="btn-primary">Apply</Button>
                <Button onClick={clearFilters} variant="outline">Clear All</Button>
              </div>
            </div>
          )}

          {/* Status Filter */}
          {showStatusFilter && (
            <div className="flex gap-3 flex-wrap">
              <Button
                size="sm"
                variant="outline"
                onClick={async () => {
                  setPaymentStatus(['PAID', 'UNPAID', 'PARTIAL']);

                  // If mobile search is active, filter locally from allBills
                  if (mobileSearched && allBills.length > 0) {
                    let filteredBills = [...allBills];

                    // Apply date filter if set
                    if (startDate) {
                      const start = new Date(startDate);
                      start.setHours(0, 0, 0, 0);
                      filteredBills = filteredBills.filter(bill => new Date(bill.createdAt) >= start);
                    }
                    if (endDate) {
                      const end = new Date(endDate);
                      end.setHours(23, 59, 59, 999);
                      filteredBills = filteredBills.filter(bill => new Date(bill.createdAt) <= end);
                    }
                    // No payment status filter since All is selected

                    setBills(filteredBills);
                    return;
                  }

                  // If no filters are active at all, load recent bills
                  if (!startDate && !endDate && !searchMobile) {
                    await loadRecentBills();
                  } else {
                    // Otherwise fetch from backend with filters
                    setLoading(true);
                    try {
                      const results = await billService.searchWithFilters({
                        mobile: searchMobile || undefined,
                        startDate: startDate || undefined,
                        endDate: endDate || undefined,
                        // No paymentStatus since All is selected
                      });
                      setBills(results);
                    } finally {
                      setLoading(false);
                    }
                  }
                }}
                className={`rounded-full px-6 py-2 ${paymentStatus.length === 3
                  ? 'bg-yellow-500 hover:bg-yellow-600 text-gray-900 border-yellow-500'
                  : 'bg-white border-gray-300 hover:bg-gray-50 text-gray-700 hover:text-black'
                  }`}
              >
                All
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={() => toggleStatus('PAID')}
                className={`rounded-full px-6 py-2 ${paymentStatus.length === 1 && paymentStatus.includes('PAID')
                  ? 'bg-green-500 hover:bg-green-600 text-white border-green-500'
                  : 'bg-white border-green-500 hover:bg-green-50 text-green-600 hover:text-black'
                  }`}
              >
                ✅ Paid
              </Button>
              <Button
                size="sm"
                variant="outline"
                onClick={() => toggleStatus('UNPAID')}
                className={`rounded-full px-6 py-2 ${paymentStatus.length === 1 && paymentStatus.includes('UNPAID')
                  ? 'bg-red-500 hover:bg-red-600 text-white border-red-500'
                  : 'bg-white border-red-500 hover:bg-red-50 text-red-600 hover:text-black'
                  }`}
              >
                ❌ Unpaid
              </Button>
            </div>
          )}
        </div>

        {/* Summary Stats */}
        {
          bills.length > 0 && (
            <div className="grid grid-cols-2 gap-4 mb-6">
              <div className="form-section text-center py-4">
                <p className="text-3xl font-bold text-primary">{bills.length}</p>
                <p className="text-sm text-muted-foreground">Bills</p>
              </div>
              <div className="form-section text-center py-4">
                <p className="text-2xl font-bold text-accent">₹{Math.round(totalAmount).toLocaleString('en-IN')}</p>
                <p className="text-sm text-muted-foreground">Total Amount</p>
              </div>
            </div>
          )
        }

        {/* Send Statement Button - Only show after mobile search is performed */}
        {
          mobileSearched && bills.length > 0 && bills[0]?.farmer && (
            <div className="mb-6">
              <Button
                onClick={() => navigate('/statement', {
                  state: {
                    farmer: bills[0].farmer,
                    bills: bills,
                    totalAmount: totalAmount,
                    startDate,
                    endDate
                  }
                })}
                className="w-full btn-accent gap-2"
              >
                📨 Send Statement via WhatsApp
              </Button>
            </div>
          )
        }

        {/* No Results */}
        {
          searched && bills.length === 0 && (
            <div className="text-center py-16 animate-fade-in">
              <div className="w-20 h-20 mx-auto rounded-full bg-muted/50 flex items-center justify-center mb-4">
                <FileText className="w-10 h-10 text-muted-foreground" />
              </div>
              <p className="text-muted-foreground text-lg">{t('noResults')}</p>
              <p className="text-muted-foreground/70 text-sm mt-1">Try searching with a different mobile number</p>
            </div>
          )
        }

        {/* Bills List */}
        {
          bills.length > 0 && (
            <div className="space-y-4">
              {bills.map((bill, index) => (
                <div key={bill.id} className="form-section card-hover flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 animate-slide-up p-4" style={{ animationDelay: `${index * 0.05}s` }}>
                  <div className="flex-1">
                    <div className="flex flex-wrap items-center gap-2 mb-1">
                      <span className="font-bold text-foreground text-lg">{bill.billNumber}</span>
                      <span className="text-sm text-muted-foreground bg-muted/50 px-2 py-0.5 rounded-full">
                        {format(new Date(bill.createdAt), 'dd/MM/yy')}
                      </span>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${bill.paymentStatus === 'PAID' ? 'bg-green-100 text-green-700' :
                        bill.paymentStatus === 'PARTIAL' ? 'bg-yellow-100 text-yellow-700' :
                          'bg-red-100 text-red-700'
                        }`}>
                        {bill.paymentStatus === 'PAID' ? '✅ Paid' : bill.paymentStatus === 'PARTIAL' ? '⚠️ Partial' : '❌ Unpaid'}
                      </span>
                    </div>
                    <p className="text-sm text-muted-foreground">
                      {bill.farmer?.name || 'Unknown'} • {bill.farmer?.mobile || ''}
                    </p>
                  </div>
                  <div className="flex items-center justify-between sm:justify-end gap-3">
                    <p className="text-xl font-bold text-accent">₹{Math.round(bill.netAmount ?? 0).toLocaleString('en-IN')}</p>
                    <div className="flex gap-2">
                      <Link to={`/bill/${bill.id}`}>
                        <Button variant="outline" size="icon" className="h-9 w-9 rounded-lg">
                          <Eye className="w-4 h-4" />
                        </Button>
                      </Link>
                      <Button variant="outline" size="icon" className="h-9 w-9 rounded-lg" onClick={() => navigate(`/edit-bill/${bill.id}`)}>
                        <Edit className="w-4 h-4" />
                      </Button>
                      <Button variant="destructive" size="icon" className="h-9 w-9 rounded-lg" onClick={() => handleDelete(bill.id)}>
                        <Trash2 className="w-4 h-4" />
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )
        }
      </main >
    </div >
  );
};

export default BillHistory;
