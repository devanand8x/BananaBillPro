import React, { useRef, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { useToast } from '@/hooks/use-toast';
import { BillWithFarmer, reportService } from '@/services/billService';
import { cloudinaryService } from '@/services/cloudinaryService';
import { useLanguage } from '@/contexts/LanguageContext';
import { Banana, ArrowLeft, Download, Share2 } from 'lucide-react';
import { format } from 'date-fns';
import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';

interface StatementData {
    farmer: {
        name: string;
        mobileNumber?: string;
        mobile?: string;
    };
    bills: BillWithFarmer[];
    totalAmount: number;
    startDate?: string;
    endDate?: string;
}

const StatementPreview: React.FC = () => {
    const location = useLocation();
    const { toast } = useToast();
    const statementRef = useRef<HTMLDivElement>(null);
    const [sharing, setSharing] = useState(false);
    const { t } = useLanguage();

    // Get statement data from navigation state
    const statementData = location.state as StatementData;

    if (!statementData) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <p className="text-muted-foreground mb-4">No statement data found</p>
                    <Link to="/history">
                        <Button>Go to Bill History</Button>
                    </Link>
                </div>
            </div>
        );
    }

    const { farmer, bills, totalAmount, startDate, endDate } = statementData;

    const formatCurrency = (amount: number) => {
        return new Intl.NumberFormat('en-IN', {
            maximumFractionDigits: 0,
        }).format(amount);
    };

    const captureAndShare = async () => {
        if (!statementRef.current) return;

        setSharing(true);
        try {
            toast({ title: 'Capturing statement...' });

            // Capture the statement as image
            const canvas = await html2canvas(statementRef.current, {
                scale: 2,
                backgroundColor: '#ffffff',
                useCORS: true,
                logging: false,
            });

            console.log('Canvas captured:', canvas.width, 'x', canvas.height);

            // Convert to blob with null check
            const blob = await new Promise<Blob | null>((resolve) => {
                canvas.toBlob((blob) => resolve(blob), 'image/png', 0.95);
            });

            if (!blob) {
                throw new Error('Failed to create image blob');
            }

            console.log('Blob created:', blob.size, 'bytes');

            // Upload to Cloudinary using the service
            toast({ title: 'Uploading to cloud...' });
            const filename = `statement_${farmer.name.replace(/[^a-zA-Z0-9]/g, '_')}_${Date.now()}`;
            const imageUrl = await cloudinaryService.uploadImage(blob, filename);

            console.log('Upload successful:', imageUrl);

            // Send via backend WhatsApp API (Twilio) - like individual bills
            toast({ title: 'Sending to WhatsApp...' });
            await reportService.sendStatementToWhatsApp(
                farmer.mobileNumber || farmer.mobile || '',
                farmer.name,
                bills.length,
                totalAmount,
                imageUrl
            );

            console.log('Statement sent via WhatsApp!');
            toast({ title: 'Statement sent successfully!' });
        } catch (error: unknown) {
            const errorMessage = error instanceof Error ? error.message : 'Unknown error';
            console.error('Error sharing statement:', error);

            // Fallback: Open WhatsApp with text message
            const whatsappMessage = `🍌 *BANANA BILL STATEMENT*\n\n👤 ${farmer.name}\n📊 ${bills.length} Bills\n💰 Total: ₹${formatCurrency(totalAmount)}`;
            const whatsappUrl = `https://wa.me/91${farmer.mobileNumber || farmer.mobile}?text=${encodeURIComponent(whatsappMessage)}`;
            window.open(whatsappUrl, '_blank');
            toast({ title: 'Opened WhatsApp (image upload failed)', description: errorMessage, variant: 'destructive' });
        } finally {
            setSharing(false);
        }
    };

    const downloadPDF = async () => {
        if (!statementRef.current) return;

        try {
            toast({ title: 'Generating PDF...' });

            const canvas = await html2canvas(statementRef.current, {
                scale: 2,
                backgroundColor: '#ffffff',
                useCORS: true,
            });

            const imgData = canvas.toDataURL('image/png');
            const pdf = new jsPDF({
                orientation: 'portrait',
                unit: 'mm',
                format: 'a4',
            });

            const pdfWidth = pdf.internal.pageSize.getWidth();
            const pdfHeight = pdf.internal.pageSize.getHeight();
            const imgWidth = canvas.width;
            const imgHeight = canvas.height;
            const ratio = Math.min(pdfWidth / imgWidth, pdfHeight / imgHeight);
            const imgX = (pdfWidth - imgWidth * ratio) / 2;
            const imgY = 10;

            pdf.addImage(imgData, 'PNG', imgX, imgY, imgWidth * ratio, imgHeight * ratio);
            pdf.save(`Statement_${farmer.name.replace(/\s+/g, '_')}_${format(new Date(), 'ddMMyyyy')}.pdf`);

            toast({ title: 'PDF downloaded!' });
        } catch (error) {
            toast({ title: 'Error downloading PDF', variant: 'destructive' });
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-background via-muted/20 to-background">
            <header className="header-bar">
                <div className="max-w-4xl mx-auto px-3 sm:px-4 py-3 sm:py-4 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <Link to="/history">
                            <Button variant="ghost" size="icon" className="hover:bg-muted">
                                <ArrowLeft className="w-10 h-10 text-gray-800" />
                            </Button>
                        </Link>
                        <div className="flex items-center gap-3">
                            <div className="logo-container w-10 h-10">
                                <Banana className="w-5 h-5 text-primary-foreground" />
                            </div>
                            <span className="font-bold text-foreground text-lg">Statement</span>
                        </div>
                    </div>
                    <div className="flex gap-2">
                        <Button variant="outline" onClick={downloadPDF} className="gap-2 rounded-xl border-2 hover:border-primary/50">
                            <Download className="w-4 h-4" /><span className="hidden sm:inline">{t('downloadPdf')}</span>
                        </Button>
                        <Button onClick={captureAndShare} disabled={sharing} className="btn-accent gap-2">
                            {sharing ? (
                                <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                            ) : (
                                <Share2 className="w-4 h-4" />
                            )}
                            <span className="hidden sm:inline">{t('shareWhatsapp')}</span>
                        </Button>
                    </div>
                </div>
            </header>

            <main className="max-w-lg mx-auto px-3 sm:px-4 py-4 sm:py-6">
                {/* Statement Card - This will be captured as image */}
                <div
                    ref={statementRef}
                    className="bg-white rounded-2xl shadow-xl overflow-hidden"
                >
                    {/* Header - Same as Individual Bill */}
                    <div className="px-6 pt-6 pb-4">
                        <div className="flex items-start gap-4 mb-3">
                            {/* Circular Logo */}
                            <div className="w-16 h-16 flex-shrink-0 rounded-full border-2 bg-white flex items-center justify-center overflow-hidden p-1" style={{ borderColor: '#1a5f2a' }}>
                                <img
                                    src="/bananalogo.png"
                                    alt="Banana"
                                    className="w-full h-full object-contain scale-110"
                                    onError={(e) => {
                                        e.currentTarget.style.display = 'none';
                                        e.currentTarget.parentElement!.innerHTML = '<div class="text-4xl leading-none">🍌</div>';
                                    }}
                                />
                            </div>
                            <div className="flex-1 pt-1">
                                <h1 className="text-2xl font-bold ml-4" style={{ color: '#1a5f2a', letterSpacing: '-0.01em' }}>RUCHI BANANA EXPORT</h1>
                                <p className="text-gray-1000 text-sm ml-8">Raver, Tal. Raver, Dist. Jalgaon - 425508</p>
                            </div>
                        </div>

                        {/* Horizontal Green Line */}
                        <div className="mb-4" style={{ height: '2px', background: '#1a5f2a' }}></div>

                        {/* Trader Info Row */}
                        <div className="grid grid-cols-2 gap-4 mb-2 text-sm">
                            <div className="space-y-1">
                                <div>
                                    <span className="text-gray-800">Mo. No.: </span>
                                    <span className="font-semibold text-gray-900">9022636114</span>
                                </div>
                                <div>
                                    <span className="text-gray-800">Date: </span>
                                    <span className="font-semibold text-gray-900">{format(new Date(), 'dd-MMM-yyyy')}</span>
                                </div>
                            </div>
                            <div className="text-right">
                                <p className="text-lg font-bold" style={{ color: '#1a5f2a' }}>STATEMENT</p>
                            </div>
                        </div>
                    </div>

                    {/* Yellow Divider */}
                    <div className="bg-gradient-to-r from-yellow-400 to-yellow-500 py-0.5"></div>

                    {/* Farmer Info */}
                    <div className="p-5 border-b border-gray-200 bg-gray-50">
                        <div className="space-y-2">
                            <div className="flex">
                                <span className="text-gray-600 w-28 flex-shrink-0">Farmer Name</span>
                                <span className="text-gray-600 mr-3">:</span>
                                <span className="font-bold text-gray-900">{farmer.name}</span>
                            </div>
                            <div className="flex">
                                <span className="text-gray-600 w-28 flex-shrink-0">Mobile No.</span>
                                <span className="text-gray-600 mr-3">:</span>
                                <span className="text-gray-900">{farmer.mobileNumber || farmer.mobile}</span>
                            </div>
                            {startDate && endDate && (
                                <div className="flex">
                                    <span className="text-gray-600 w-28 flex-shrink-0">Period</span>
                                    <span className="text-gray-600 mr-3">:</span>
                                    <span className="text-gray-900">{format(new Date(startDate), 'dd/MM/yyyy')} - {format(new Date(endDate), 'dd/MM/yyyy')}</span>
                                </div>
                            )}
                        </div>
                    </div>

                    {/* Bills List */}
                    <div className="p-4">
                        <p className="text-sm font-semibold text-gray-600 mb-3 px-2">BILL DETAILS</p>
                        <div className="space-y-2">
                            {bills.map((bill) => (
                                <div key={bill.id} className="flex justify-between items-center bg-gray-50 rounded-lg p-3">
                                    <div>
                                        <p className="font-medium text-gray-900">{bill.billNumber}</p>
                                        <p className="text-xs text-gray-500">{format(new Date(bill.createdAt), 'dd/MM/yyyy')} | {(bill.finalNetWeight ?? 0).toFixed(0)} kg</p>
                                    </div>
                                    <p className="font-bold text-green-600">₹{formatCurrency(bill.netAmount ?? 0)}</p>
                                </div>
                            ))}
                        </div>
                    </div>

                    {/* Total */}
                    <div className="bg-gray-900 p-6 text-center">
                        <p className="text-gray-400 text-sm mb-1">TOTAL AMOUNT ({bills.length} Bills)</p>
                        <p className="text-4xl font-bold text-white">₹{formatCurrency(totalAmount)}</p>
                    </div>

                    {/* Footer */}
                    <div className="bg-gray-100 p-3 text-center">
                        <p className="text-xs text-gray-500">
                            Generated on {format(new Date(), 'dd MMM yyyy, hh:mm a')}
                        </p>
                    </div>
                </div>

                {/* Action Buttons */}

            </main>
        </div>
    );
};

export default StatementPreview;
