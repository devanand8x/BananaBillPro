import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, ArrowLeft, Phone, Send } from 'lucide-react';
import apiClient from '@/services/apiClient';

const ForgotPassword: React.FC = () => {
  const { t } = useLanguage();
  const { toast } = useToast();
  const navigate = useNavigate();

  const [mobile, setMobile] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSendOtp = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!mobile || mobile.length !== 10) {
      toast({ title: t('enterValidMobile'), variant: 'destructive' });
      return;
    }

    setLoading(true);
    try {
      await apiClient.post('/auth/send-otp', {
        mobile,
        action: 'reset_password'
      });

      sessionStorage.setItem('reset_mobile', mobile);

      toast({ title: t('otpSent'), description: t('checkPhone') });
      navigate('/reset-password');
    } catch (error: unknown) {
      const axiosError = error as { response?: { data?: { error?: string } } };
      toast({
        title: t('otpSendFailed'),
        description: axiosError.response?.data?.error || t('tryAgain'),
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-muted/30 to-background flex flex-col">
      <header className="flex justify-between items-center p-4 border-b border-border/50 bg-card/50 backdrop-blur-sm">
        <div className="flex items-center gap-3">
          <div className="logo-container">
            <Banana className="w-6 h-6 text-primary-foreground" />
          </div>
          <span className="text-xl font-bold text-foreground">Banana Bill</span>
        </div>
        <LanguageSwitch />
      </header>

      <main className="flex-1 flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          <div className="bg-card/95 backdrop-blur-md rounded-3xl shadow-xl p-8 border border-border/50 animate-scale-in">
            <Link to="/login" className="inline-flex items-center text-muted-foreground hover:text-primary mb-6 transition-colors">
              <ArrowLeft className="w-4 h-4 mr-2" />
              {t('backToLogin')}
            </Link>

            <div className="text-center mb-8">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-primary/10 flex items-center justify-center">
                <Phone className="w-8 h-8 text-primary" />
              </div>
              <h1 className="text-2xl font-bold text-foreground">{t('forgotPassword')}</h1>
              <p className="text-muted-foreground mt-2 text-sm">
                {t('enterMobileForOtp')}
              </p>
            </div>

            <form onSubmit={handleSendOtp} className="space-y-5">
              <div className="animate-fade-in">
                <Label htmlFor="mobile" className="form-label">{t('mobileNumber')}</Label>
                <div className="relative group">
                  <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input
                    id="mobile"
                    type="tel"
                    value={mobile}
                    onChange={(e) => setMobile(e.target.value.replace(/\D/g, '').slice(0, 10))}
                    className="input-field pl-12"
                    placeholder="10-digit mobile number"
                    maxLength={10}
                  />
                </div>
              </div>

              <Button
                type="submit"
                className="btn-primary w-full h-12 text-base animate-fade-in"
                disabled={loading || mobile.length !== 10}
              >
                {loading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                    {t('sending')}
                  </span>
                ) : (
                  <span className="flex items-center gap-2">
                    <Send className="w-4 h-4" />
                    {t('sendOtp')}
                  </span>
                )}
              </Button>
            </form>
          </div>
        </div>
      </main>
    </div>
  );
};

export default ForgotPassword;
