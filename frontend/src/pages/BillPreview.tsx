import React, { useEffect, useState, useRef } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { useToast } from '@/hooks/use-toast';
import { billService, paymentService, BillWithFarmer } from '@/services/billService';
import { generateBillPDF } from '@/services/exportService';
import { cloudinaryService } from '@/services/cloudinaryService';
import { ArrowLeft, Download, Share2, Banana, Printer, CheckCircle } from 'lucide-react';
import { format } from 'date-fns';

const BillPreview: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const { t } = useLanguage();
  const { toast } = useToast();
  const [bill, setBill] = useState<BillWithFarmer | null>(null);
  const [loading, setLoading] = useState(true);
  const billRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const fetchBill = async () => {
      if (!id) return;
      try {
        const data = await billService.getById(id);
        setBill(data);
      } catch (error) {
        console.error('Error fetching bill:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchBill();
  }, [id]);

  const handleDownloadPDF = async () => {
    if (!billRef.current || !bill) return;
    try {
      await generateBillPDF(billRef.current, bill.billNumber);
      toast({ title: 'PDF downloaded successfully!' });
    } catch (error) {
      toast({ title: 'Error downloading PDF', variant: 'destructive' });
    }
  };

  const handleShareWhatsApp = async () => {
    if (!billRef.current || !bill || !id) return;
    try {
      toast({ title: 'Capturing bill...' });

      // Generate bill image
      const { generateBillImage } = await import('@/services/exportService');
      const blob = await generateBillImage(billRef.current);

      // Upload to Cloudinary (same as Statement)
      toast({ title: 'Uploading to cloud...' });
      const filename = `bill_${bill.billNumber}_${Date.now()}`;
      const imageUrl = await cloudinaryService.uploadImage(blob, filename);

      console.log('Image uploaded to Cloudinary:', imageUrl);

      // Send Cloudinary URL to backend for WhatsApp
      toast({ title: 'Sending to WhatsApp...' });
      await billService.sendToWhatsApp(id, imageUrl);

      toast({ title: 'Bill sent via WhatsApp!', description: 'Farmer will receive the bill image' });
    } catch (error) {
      console.error('WhatsApp share error:', error);
      toast({ title: 'Error sending via WhatsApp', variant: 'destructive' });
    }
  };

  const handleMarkAsPaid = async () => {
    if (!bill || !id) return;
    try {
      await paymentService.markAsPaid(id);
      setBill({ ...bill, paymentStatus: 'PAID' });
      toast({ title: 'Paid to Farmer', description: 'Payment status updated' });
    } catch (error) {
      toast({ title: 'Error updating payment status', variant: 'destructive' });
    }
  };

  const handleSendConfirmation = async () => {
    if (!bill || !id) return;
    try {
      await paymentService.sendPaymentConfirmation(id);
      toast({ title: 'Confirmation sent!', description: 'Payment confirmation sent via WhatsApp' });
    } catch (error) {
      toast({ title: 'Error sending confirmation', variant: 'destructive' });
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const formatWeight = (weight: number) => {
    return `${weight.toLocaleString('en-IN', { minimumFractionDigits: weight % 1 !== 0 ? 2 : 0, maximumFractionDigits: 2 })} kg`;
  };

  if (loading) {
    return <div className="min-h-screen flex items-center justify-center"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div></div>;
  }

  if (!bill) {
    return <div className="min-h-screen flex items-center justify-center"><p>Bill not found</p></div>;
  }

  // Calculate weights
  const boxWeight = bill.boxCount ?? 0;
  const netWeight = bill.netWeight ?? 0;
  const dandaWeight = bill.dandaWeight ?? 0;
  const finalNetWeight = bill.finalNetWeight ?? 0;

  return (
    <div className="min-h-screen bg-gradient-to-br from-muted/30 via-background to-muted/30">
      <header className="header-bar no-print">
        <div className="max-w-4xl mx-auto px-4 py-3 flex justify-between items-center">
          {/* Left side: Back arrow, Logo, Title only */}
          <div className="flex items-center gap-2">
            <Link to="/history">
              <Button variant="ghost" size="icon" className="hover:bg-muted">
                <ArrowLeft className="w-10 h-10 text-gray-800" />
              </Button>
            </Link>
            <div className="logo-container w-10 h-10">
              <Banana className="w-5 h-5 text-primary-foreground" />
            </div>
            <span className="font-bold text-foreground text-lg">Bill Preview</span>
          </div>
          {/* Right side: All buttons */}
          <div className="flex items-center gap-2">
            {/* Payment Status Badge */}
            <span className={`px-3 py-1 rounded-full text-xs font-bold ${bill.paymentStatus === 'PAID' ? 'bg-green-100 text-green-600' :
              bill.paymentStatus === 'PARTIAL' ? 'bg-yellow-100 text-yellow-600' :
                'bg-red-100 text-red-600'
              }`}>
              {bill.paymentStatus === 'PAID' ? 'Paid' : bill.paymentStatus === 'PARTIAL' ? 'Partial' : 'Unpaid'}
            </span>
            {/* Mark as Paid / Send Confirmation Button */}
            {bill.paymentStatus === 'PAID' ? (
              <Button variant="outline" onClick={handleSendConfirmation} className="gap-2 rounded-xl border-2 border-primary hover:border-primary/70 hover:bg-primary/5">
                🔔 Send Confirmation
              </Button>
            ) : (
              <Button variant="outline" onClick={handleMarkAsPaid} className="gap-2 rounded-xl border-2 border-green-500 text-green-600 hover:bg-green-50 hover:text-foreground">
                <CheckCircle className="w-4 h-4" /> Mark as Paid
              </Button>
            )}
            <Button variant="outline" onClick={handleDownloadPDF} className="gap-2 rounded-xl border-2 hover:border-primary/50">
              <Download className="w-4 h-4" /><span className="hidden sm:inline">Download PDF</span>
            </Button>
            <Button variant="outline" onClick={() => window.print()} className="rounded-xl border-2 hover:border-primary/50 px-3">
              <Printer className="w-4 h-4" />
            </Button>
            <Button onClick={handleShareWhatsApp} className="btn-accent gap-2">
              <Share2 className="w-4 h-4" /><span className="hidden sm:inline">Share on WhatsApp</span>
            </Button>
          </div>
        </div>
      </header>

      <main className="max-w-xl mx-auto px-4 py-6">
        <div ref={billRef} className="bg-white text-black p-6 max-w-[550px] mx-auto shadow-xl print-area">

          {/* Header */}
          <div className="flex items-center gap-4 mb-2">
            <div className="w-16 h-16 flex-shrink-0 rounded-full overflow-hidden border-2 border-[#1B5E20]">
              <img
                src="/bananalogo.png"
                alt="Ruchi Banana Export Logo"
                className="w-full h-full object-cover"
              />
            </div>
            <div className="flex-1 text-left">
              <h1 className="text-3xl font-bold text-[#1B5E20] tracking-wide ml-2">RUCHI BANANA EXPORT</h1>
              <p className="text-gray-1000 text-large ml-12">Raver, Tal. Raver, Dist. Jalgaon - 425508</p>
            </div>
          </div>

          {/* Green line separator */}
          <div className="h-0.5 bg-[#1B5E20] mb-4"></div>

          {/* Bill Info Row */}
          <div className="flex justify-between mb-1 text-sm">
            <div>Mo. No.: <strong>9022636114</strong></div>
            <div>Bill No.: <strong>{bill.billNumber}</strong></div>
          </div>
          <div className="text-sm mb-4">
            Date: <strong>{format(new Date(bill.createdAt), 'dd-MMM-yyyy')}</strong>
          </div>

          {/* Farmer & Transport Section */}
          <div className="border-l-2 border-[#4CAF50] pl-3 mb-4">
            <h2 className="text-[#1B5E20] font-bold mb-2 text-medium">Farmer &amp; Transport</h2>
            <div className="grid grid-cols-[120px_10px_1fr] gap-y-1 text-sm">
              <span className="text-[#000000]">Farmer Name</span>
              <span>:</span>
              <span className="font-medium">{bill.farmer?.name}</span>

              <span className="text-[#000000]">Mobile No.</span>
              <span>:</span>
              <span className="font-medium">{bill.farmer?.mobile}</span>

              <span className="text-[#000000]">Address</span>
              <span>:</span>
              <span className="font-medium">{bill.farmer?.village}</span>

              <span className="text-[#000000]">Vehicle No.</span>
              <span>:</span>
              <span className="font-medium">{bill.vehicleNumber}</span>
            </div>
          </div>

          {/* Weight & Payment Tables */}
          <div className="grid grid-cols-2 gap-4 ">
            {/* Weight Calculation Table */}
            <div className="border border-gray-200 border-l-2 border-l-[#4CAF50] overflow-hidden">
              <div className="bg-[#E8F5E9] px-3 py-2 h-9 flex items-center">
                <h3 className="text-[#1B5E20] font-bold text-sm" >Weight Calculation</h3>
              </div>
              <div className="divide-y divide-gray-200">
                <div className="flex justify-between px-3 py-2 text-sm">
                  <span>Gross Weight</span>
                  <span className="font-medium">{formatWeight(bill.grossWeight ?? 0)}</span>
                </div>
                <div className="flex justify-between px-3 py-2 text-sm">
                  <span>(-) Patti Weight</span>
                  <span className="font-medium">{formatWeight(bill.pattiWeight ?? 0)}</span>
                </div>
                <div className="flex justify-between px-3 py-2 text-sm">
                  <span>(-) Box Weight</span>
                  <span className="font-medium">{formatWeight(boxWeight)}</span>
                </div>
                <div className="flex justify-between px-3 py-2 text-sm bg-[#E8F5E9]">
                  <span className="font-bold text-[#1B5E20]">Net Weight</span>
                  <span className="font-bold text-[#1B5E20]">{formatWeight(netWeight)}</span>
                </div>
                <div className="flex justify-between px-3 py-2 text-sm">
                  <span>(+) Danda (7%)</span>
                  <span className="font-medium">{formatWeight(dandaWeight)}</span>
                </div>
                <div className="flex justify-between px-3 py-2 text-sm">
                  <span>(+) Tut / Wastage</span>
                  <span className="font-medium">{formatWeight(bill.tutWastage ?? 0)}</span>
                </div>
                <div className="flex justify-between px-3 py-2 text-sm bg-[#E8F5E9]">
                  <span className="font-bold text-[#1B5E20]">Final Net Weight</span>
                  <span className="font-bold text-[#1B5E20]">{formatWeight(finalNetWeight)}</span>
                </div>
              </div>
            </div>

            {/* Payment Summary Table */}
            <div className="border border-gray-200 border-l-2 border-l-[#4CAF50] overflow-hidden flex flex-col">
              <div className="bg-[#E8F5E9] px-3 py-2">
                <h3 className="text-[#1B5E20] font-bold text-sm">Payment Summary</h3>
              </div>
              <div className="divide-y divide-gray-200 flex-1 flex flex-col">
                <div className="flex justify-between px-3 py-2 text-sm">
                  <span>Rate per Kg</span>
                  <span className="font-medium">₹{bill.ratePerKg ?? 0}</span>
                </div>
                <div className="flex justify-between px-3 py-2 text-sm">
                  <span>Total Amount</span>
                  <span className="font-medium">{formatCurrency(bill.totalAmount ?? 0)}</span>
                </div>
                <div className="flex justify-between px-3 py-2 text-sm">
                  <span>(-) Majuri</span>
                  <span className="font-medium">{formatCurrency(bill.majuri ?? 0)}</span>
                </div>
                <div className="flex-1"></div>
                <div className="flex justify-between px-3 py-1 bg-[#E8F5E9] items-center">
                  <span className="font-bold text-[#1B5E20] text-sm">Net Amount</span>
                  <span className="font-bold text-[#1B5E20] text-xl">{formatCurrency(bill.netAmount ?? 0)}</span>
                </div>
              </div>
            </div>
          </div>

          {/* Footer */}
          <div className="mt-2 pt-2 border-t border-gray-200]">
            <p className="text-center text-gray-400 text-xs italic">This is an online generated bill.</p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default BillPreview;
