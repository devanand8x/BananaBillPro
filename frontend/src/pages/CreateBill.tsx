import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import { farmerService, billService } from '@/services/billService';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, ArrowLeft, User, Phone, MapPin, Truck, Scale, Calculator } from 'lucide-react';
import { Link } from 'react-router-dom';

const CreateBill: React.FC = () => {
  const { t } = useLanguage();
  const { toast } = useToast();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const prefillMobile = searchParams.get('mobile') || '';

  const [loading, setLoading] = useState(false);
  const [farmerName, setFarmerName] = useState('');
  const [farmerMobile, setFarmerMobile] = useState(prefillMobile);
  const [address, setAddress] = useState('');
  const [vehicleNumber, setVehicleNumber] = useState('');

  const [grossWeight, setGrossWeight] = useState<number>(0);
  const [pattiWeight, setPattiWeight] = useState<number>(0);
  const [boxCount, setBoxCount] = useState<number>(0);
  const [tutWastage, setTutWastage] = useState<number>(0);
  const [ratePerKg, setRatePerKg] = useState<number>(0);
  const [majuri, setMajuri] = useState<number>(0);

  const netWeight = Math.max(0, grossWeight - pattiWeight - boxCount);
  const dandaWeight = netWeight * 0.07;
  const finalNetWeight = netWeight + dandaWeight + tutWastage;
  const totalAmount = finalNetWeight * ratePerKg;
  const netAmount = Math.max(0, totalAmount - majuri);

  useEffect(() => {
    if (farmerMobile.length >= 10) {
      const fetchFarmer = async () => {
        const farmer = await farmerService.findByMobile(farmerMobile);
        if (farmer) {
          setFarmerName(farmer.name);
          setAddress(farmer.address || '');
        }
      };
      fetchFarmer();
    }
  }, [farmerMobile]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!farmerName || !farmerMobile || grossWeight <= 0 || ratePerKg <= 0) {
      toast({ title: 'Please fill all required fields', variant: 'destructive' });
      return;
    }

    setLoading(true);
    try {
      const farmer = await farmerService.upsert({ mobileNumber: farmerMobile, name: farmerName, address });

      const bill = await billService.create({
        farmerId: farmer.id,
        vehicleNumber: vehicleNumber || null,
        grossWeight: grossWeight,
        pattiWeight: pattiWeight,
        boxCount: boxCount,
        tutWastage: tutWastage,
        ratePerKg: ratePerKg,
        majuri: majuri,
      });

      toast({ title: 'Bill created successfully!' });
      navigate(`/bill/${bill.id}`);
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : 'Error creating bill';
      toast({ title: 'Error creating bill', description: message, variant: 'destructive' });
    } finally {
      setLoading(false);
    }
  };

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
              <span className="font-bold text-foreground text-lg">{t('createNewBill')}</span>
            </div>
          </div>
          <LanguageSwitch />
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-6 pb-8">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="form-section animate-fade-in">
            <h2 className="text-lg font-semibold mb-5 flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center">
                <User className="w-4 h-4 text-primary" />
              </div>
              {t('farmerDetails')}
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
              <div>
                <Label className="form-label">{t('farmerMobile')} *</Label>
                <div className="relative group">
                  <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input type="tel" value={farmerMobile} onChange={(e) => setFarmerMobile(e.target.value)} className="input-field pl-11" placeholder="Mobile number" />
                </div>
              </div>
              <div>
                <Label className="form-label">{t('farmerName')} *</Label>
                <Input value={farmerName} onChange={(e) => setFarmerName(e.target.value)} className="input-field" placeholder="Farmer name" />
              </div>
              <div>
                <Label className="form-label">{t('address')}</Label>
                <div className="relative group">
                  <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input value={address} onChange={(e) => setAddress(e.target.value)} className="input-field pl-11" placeholder="Address" />
                </div>
              </div>
              <div>
                <Label className="form-label">{t('vehicleNumber')}</Label>
                <div className="relative group">
                  <Truck className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input value={vehicleNumber} onChange={(e) => setVehicleNumber(e.target.value)} className="input-field pl-11" placeholder="Vehicle number" />
                </div>
              </div>
            </div>
          </div>

          <div className="form-section animate-fade-in" style={{ animationDelay: '0.1s' }}>
            <h2 className="text-lg font-semibold mb-5 flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center">
                <Scale className="w-4 h-4 text-primary" />
              </div>
              {t('weightCalculation')}
            </h2>
            <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
              <div>
                <Label className="form-label">{t('grossWeight')} *</Label>
                <Input type="number" value={grossWeight || ''} onChange={(e) => setGrossWeight(Number(e.target.value))} className="input-field" placeholder="0" />
              </div>
              <div>
                <Label className="form-label">{t('pattiWeight')}</Label>
                <Input type="number" value={pattiWeight || ''} onChange={(e) => setPattiWeight(Number(e.target.value))} className="input-field" placeholder="0" />
              </div>
              <div>
                <Label className="form-label">{t('boxCount')}</Label>
                <Input type="number" value={boxCount || ''} onChange={(e) => setBoxCount(Number(e.target.value))} className="input-field" placeholder="0" />
              </div>
              <div>
                <Label className="form-label">{t('netWeight')}</Label>
                <Input type="number" value={netWeight.toFixed(2)} readOnly className="input-field bg-muted/50 font-medium" />
              </div>
              <div>
                <Label className="form-label">{t('dandaWeight')}</Label>
                <Input type="number" value={dandaWeight.toFixed(2)} readOnly className="input-field bg-muted/50 font-medium" />
              </div>
              <div>
                <Label className="form-label">{t('tutWastage')}</Label>
                <Input type="number" value={tutWastage || ''} onChange={(e) => setTutWastage(Number(e.target.value))} className="input-field" placeholder="0" />
              </div>
              <div className="col-span-2 md:col-span-3">
                <Label className="form-label font-semibold text-foreground">{t('finalNetWeight')}</Label>
                <Input type="number" value={finalNetWeight.toFixed(2)} readOnly className="input-field bg-gradient-to-r from-primary/10 to-primary/5 font-bold text-lg border-primary/30" />
              </div>
            </div>
          </div>

          <div className="form-section animate-fade-in" style={{ animationDelay: '0.2s' }}>
            <h2 className="text-lg font-semibold mb-5 flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-accent/10 flex items-center justify-center">
                <Calculator className="w-4 h-4 text-accent" />
              </div>
              {t('paymentCalculation')}
            </h2>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="form-label">{t('ratePerKg')} *</Label>
                <Input type="number" value={ratePerKg || ''} onChange={(e) => setRatePerKg(Number(e.target.value))} className="input-field" placeholder="0" />
              </div>
              <div>
                <Label className="form-label">{t('totalAmount')}</Label>
                <Input type="number" value={totalAmount.toFixed(2)} readOnly className="input-field bg-muted/50 font-medium" />
              </div>
              <div>
                <Label className="form-label">{t('majuri')}</Label>
                <Input type="number" value={majuri || ''} onChange={(e) => setMajuri(Number(e.target.value))} className="input-field" placeholder="0" />
              </div>
              <div>
                <Label className="form-label font-semibold text-accent">{t('netAmount')}</Label>
                <Input type="number" value={netAmount.toFixed(2)} readOnly className="input-field bg-gradient-to-r from-accent to-accent/80 text-accent-foreground font-bold text-xl border-0" />
              </div>
            </div>
          </div>

          <Button type="submit" className="btn-primary w-full h-14 text-lg" disabled={loading}>
            {loading ? (
              <span className="flex items-center gap-2">
                <span className="w-5 h-5 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                Creating...
              </span>
            ) : t('generateBill')}
          </Button>
        </form>
      </main>
    </div>
  );
};

export default CreateBill;
