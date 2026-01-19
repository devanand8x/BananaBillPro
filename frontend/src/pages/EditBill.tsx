import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import { billService, BillWithFarmer } from '@/services/billService';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, ArrowLeft, User, Phone, MapPin, Truck, Scale, Calculator } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function EditBill() {
    const { t } = useLanguage();
    const { toast } = useToast();
    const navigate = useNavigate();
    const { id } = useParams<{ id: string }>();

    const [loading, setLoading] = useState(false);
    const [fetching, setFetching] = useState(true);
    const [farmerName, setFarmerName] = useState('');
    const [farmerMobile, setFarmerMobile] = useState('');
    const [farmerId, setFarmerId] = useState('');
    const [address, setAddress] = useState('');
    const [vehicleNumber, setVehicleNumber] = useState('');

    const [grossWeight, setGrossWeight] = useState<number>(0);
    const [pattiWeight, setPattiWeight] = useState<number>(0);
    const [boxCount, setBoxCount] = useState<number>(0);
    const [tutWastage, setTutWastage] = useState<number>(0);
    const [ratePerKg, setRatePerKg] = useState<number>(0);
    const [majuri, setMajuri] = useState<number>(0);

    // Live calculations
    const netWeight = Math.max(0, grossWeight - pattiWeight - boxCount);
    const dandaWeight = netWeight * 0.07;
    const finalNetWeight = netWeight + dandaWeight + tutWastage;
    const totalAmount = finalNetWeight * ratePerKg;
    const netAmount = Math.max(0, totalAmount - majuri);

    // Fetch existing bill data
    useEffect(() => {
        const fetchBill = async () => {
            if (!id) return;
            try {
                const bill = await billService.getById(id) as BillWithFarmer;
                if (bill) {
                    setFarmerId(bill.farmerId);
                    setFarmerName(bill.farmer.name);
                    setFarmerMobile(bill.farmer.mobile);
                    setAddress(bill.farmer.village || '');
                    setVehicleNumber(bill.vehicleNumber || '');
                    setGrossWeight(bill.grossWeight);
                    setPattiWeight(bill.pattiWeight);
                    setBoxCount(bill.boxCount);
                    setTutWastage(bill.tutWastage);
                    setRatePerKg(bill.ratePerKg);
                    setMajuri(bill.majuri);
                }
            } catch (error) {
                console.error('Error fetching bill:', error);
                toast({ title: 'Error loading bill', variant: 'destructive' });
            } finally {
                setFetching(false);
            }
        };
        fetchBill();
    }, [id, toast]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!farmerName || !farmerMobile || grossWeight <= 0 || ratePerKg <= 0) {
            toast({ title: 'Please fill all required fields', variant: 'destructive' });
            return;
        }

        setLoading(true);
        try {
            await billService.update(id!, {
                farmerId: farmerId,
                vehicleNumber: vehicleNumber || null,
                grossWeight: grossWeight,
                pattiWeight: pattiWeight,
                boxCount: boxCount,
                tutWastage: tutWastage,
                ratePerKg: ratePerKg,
                majuri: majuri,
            });

            toast({ title: 'Bill updated successfully!' });
            navigate(`/bill/${id}`);
        } catch (error: unknown) {
            const message = error instanceof Error ? error.message : 'Error updating bill';
            toast({ title: 'Error updating bill', description: message, variant: 'destructive' });
        } finally {
            setLoading(false);
        }
    };

    if (fetching) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-yellow-500"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-gray-50">
            {/* Header */}
            <header className="bg-white border-b border-gray-200 sticky top-0 z-10">
                <div className="max-w-4xl mx-auto px-4 py-4 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <Link to="/history">
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
                            <span className="font-bold text-gray-800 text-lg">Edit Bill</span>
                        </div>
                    </div>
                    <LanguageSwitch />
                </div>
            </header>

            <main className="max-w-4xl mx-auto px-4 py-6 pb-8">
                <form onSubmit={handleSubmit} className="space-y-6">

                    {/* Farmer Details */}
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                        <h2 className="text-lg font-semibold mb-5 flex items-center gap-3 text-gray-800">
                            <User className="w-5 h-5 text-yellow-500" />
                            {t('farmerDetails')}
                        </h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('farmerMobile')} *</Label>
                                <div className="relative">
                                    <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                                    <Input
                                        type="tel"
                                        value={farmerMobile}
                                        disabled
                                        className="pl-10 h-12 bg-gray-100 border-gray-200 rounded-lg cursor-not-allowed"
                                        placeholder="Mobile number"
                                    />
                                </div>
                            </div>
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('farmerName')} *</Label>
                                <Input
                                    value={farmerName}
                                    disabled
                                    className="h-12 bg-gray-100 border-gray-200 rounded-lg cursor-not-allowed"
                                    placeholder="Farmer name"
                                />
                            </div>
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('address')}</Label>
                                <div className="relative">
                                    <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                                    <Input
                                        value={address}
                                        disabled
                                        className="pl-10 h-12 bg-gray-100 border-gray-200 rounded-lg cursor-not-allowed"
                                        placeholder="Address"
                                    />
                                </div>
                            </div>
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('vehicleNumber')}</Label>
                                <div className="relative">
                                    <Truck className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                                    <Input
                                        value={vehicleNumber}
                                        onChange={(e) => setVehicleNumber(e.target.value)}
                                        className="pl-10 h-12 bg-gray-50 border-gray-200 rounded-lg"
                                        placeholder="Vehicle number"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Weight Calculation */}
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                        <h2 className="text-lg font-semibold mb-5 flex items-center gap-3 text-gray-800">
                            <Scale className="w-5 h-5 text-yellow-500" />
                            {t('weightCalculation')}
                        </h2>
                        <div className="grid grid-cols-3 gap-4">
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('grossWeight')} *</Label>
                                <Input
                                    type="number"
                                    value={grossWeight || ''}
                                    onChange={(e) => setGrossWeight(Number(e.target.value))}
                                    className="h-12 bg-gray-50 border-gray-200 rounded-lg"
                                    placeholder="0"
                                />
                            </div>
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('pattiWeight')}</Label>
                                <Input
                                    type="number"
                                    value={pattiWeight || ''}
                                    onChange={(e) => setPattiWeight(Number(e.target.value))}
                                    className="h-12 bg-gray-50 border-gray-200 rounded-lg"
                                    placeholder="0"
                                />
                            </div>
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('boxCount')}</Label>
                                <Input
                                    type="number"
                                    value={boxCount || ''}
                                    onChange={(e) => setBoxCount(Number(e.target.value))}
                                    className="h-12 bg-gray-50 border-gray-200 rounded-lg"
                                    placeholder="0"
                                />
                            </div>

                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('netWeight')}</Label>
                                <div className="h-12 bg-gray-200 rounded-lg flex items-center px-4 text-gray-700 font-medium">
                                    {netWeight.toFixed(2)}
                                </div>
                            </div>
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('dandaWeight')}</Label>
                                <div className="h-12 bg-gray-200 rounded-lg flex items-center px-4 text-gray-700 font-medium">
                                    {dandaWeight.toFixed(2)}
                                </div>
                            </div>
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('tutWastage')}</Label>
                                <Input
                                    type="number"
                                    value={tutWastage || ''}
                                    onChange={(e) => setTutWastage(Number(e.target.value))}
                                    className="h-12 bg-gray-50 border-gray-200 rounded-lg"
                                    placeholder="0"
                                />
                            </div>
                        </div>

                        <div className="mt-4">
                            <Label className="text-sm text-gray-800 font-semibold mb-2 block">{t('finalNetWeight')}</Label>
                            <div className="h-12 rounded-lg flex items-center px-4 text-gray-800 font-bold text-lg" style={{ backgroundColor: '#F9BC061A' }}>
                                {finalNetWeight.toFixed(2)}
                            </div>
                        </div>
                    </div>

                    {/* Payment Calculation */}
                    <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                        <h2 className="text-lg font-semibold mb-5 flex items-center gap-3 text-gray-800">
                            <Calculator className="w-5 h-5 text-green-600" />
                            {t('paymentCalculation')}
                        </h2>
                        <div className="grid grid-cols-2 gap-4">
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('ratePerKg')} *</Label>
                                <Input
                                    type="number"
                                    value={ratePerKg || ''}
                                    onChange={(e) => setRatePerKg(Number(e.target.value))}
                                    className="h-12 bg-gray-50 border-gray-200 rounded-lg"
                                    placeholder="0"
                                />
                            </div>
                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('totalAmount')}</Label>
                                <div className="h-12 bg-gray-200 rounded-lg flex items-center px-4 text-gray-700 font-medium">
                                    {totalAmount.toFixed(2)}
                                </div>
                            </div>

                            <div>
                                <Label className="text-sm text-gray-800 mb-2 block">{t('majuri')}</Label>
                                <Input
                                    type="number"
                                    value={majuri || ''}
                                    onChange={(e) => setMajuri(Number(e.target.value))}
                                    className="h-12 bg-gray-50 border-gray-200 rounded-lg"
                                    placeholder="0"
                                />
                            </div>
                            <div>
                                <Label className="text-sm font-bold mb-2 block" style={{ color: '#318153' }}>{t('netAmount')}</Label>
                                <div className="h-12 rounded-lg flex items-center px-4 text-white font-bold text-lg" style={{ backgroundColor: '#318153' }}>
                                    {netAmount.toFixed(2)}
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Submit Button */}
                    <Button
                        type="submit"
                        className="w-full h-14 text-lg font-semibold text-gray-800 rounded-xl"
                        style={{ backgroundColor: '#F59E0B' }}
                        disabled={loading}
                    >
                        {loading ? (
                            <span className="flex items-center gap-2">
                                <span className="w-5 h-5 border-2 border-gray-800/30 border-t-gray-800 rounded-full animate-spin" />
                                Updating...
                            </span>
                        ) : 'Update Bill'}
                    </Button>
                </form>
            </main>
        </div>
    );
}
