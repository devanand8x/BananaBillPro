import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, Phone, ArrowLeft, KeyRound } from 'lucide-react';
import { InputOTP, InputOTPGroup, InputOTPSlot } from '@/components/ui/input-otp';

const OtpLogin: React.FC = () => {
  const [mobile, setMobile] = useState('');
  const [otp, setOtp] = useState('');
  const [loading, setLoading] = useState(false);
  const [otpSent, setOtpSent] = useState(false);
  const { t } = useLanguage();
  const { toast } = useToast();
  const navigate = useNavigate();
  const { sendOtp, verifyOtp } = useAuth();

  const handleSendOtp = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!mobile) {
      toast({ title: t('enterMobile'), variant: 'destructive' });
      return;
    }

    const cleanMobile = mobile.replace(/\D/g, '');
    if (cleanMobile.length !== 10) {
      toast({ title: t('enterValidMobile'), variant: 'destructive' });
      return;
    }

    setLoading(true);

    try {
      const { error } = await sendOtp(cleanMobile);

      if (error) throw error;

      setOtpSent(true);
      toast({
        title: t('otpSent'),
        description: t('otpSentToMobile')
      });
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : t('somethingWentWrong');
      toast({
        title: 'Error',
        description: message,
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();

    if (otp.length !== 6) {
      toast({ title: t('enter6DigitOtp'), variant: 'destructive' });
      return;
    }

    setLoading(true);

    const cleanMobile = mobile.replace(/\D/g, '');

    try {
      const { error } = await verifyOtp(cleanMobile, otp);

      if (error) throw error;

      toast({ title: t('loginSuccess') });
      navigate('/dashboard');
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : t('otpVerifyFailed');
      toast({
        title: t('wrongOtp'),
        description: message,
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

            <h1 className="text-2xl font-bold text-center mb-2 text-foreground">
              {t('loginWithOtp')}
            </h1>
            <p className="text-center text-muted-foreground mb-8">
              {otpSent ? t('enter6DigitOtpSent') : t('enterMobileToReceiveOtp')}
            </p>

            {!otpSent ? (
              <form onSubmit={handleSendOtp} className="space-y-6">
                <div>
                  <Label htmlFor="mobile" className="form-label">{t('mobileNumber')}</Label>
                  <div className="relative group">
                    <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                    <Input
                      id="mobile"
                      type="tel"
                      value={mobile}
                      onChange={(e) => setMobile(e.target.value)}
                      className="input-field pl-12"
                      placeholder={t('enterRegisteredMobile')}
                      maxLength={10}
                    />
                  </div>
                </div>

                <Button type="submit" className="btn-primary w-full h-12" disabled={loading}>
                  {loading ? (
                    <span className="flex items-center gap-2">
                      <span className="w-4 h-4 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                      {t('sending')}
                    </span>
                  ) : t('sendOtp')}
                </Button>
              </form>
            ) : (
              <form onSubmit={handleVerifyOtp} className="space-y-6">
                <div className="flex flex-col items-center gap-4">
                  <div className="w-14 h-14 rounded-full bg-primary/10 flex items-center justify-center">
                    <KeyRound className="w-7 h-7 text-primary" />
                  </div>

                  <InputOTP
                    maxLength={6}
                    value={otp}
                    onChange={setOtp}
                  >
                    <InputOTPGroup>
                      <InputOTPSlot index={0} />
                      <InputOTPSlot index={1} />
                      <InputOTPSlot index={2} />
                      <InputOTPSlot index={3} />
                      <InputOTPSlot index={4} />
                      <InputOTPSlot index={5} />
                    </InputOTPGroup>
                  </InputOTP>
                </div>

                <Button type="submit" className="btn-primary w-full h-12" disabled={loading}>
                  {loading ? (
                    <span className="flex items-center gap-2">
                      <span className="w-4 h-4 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                      {t('verifying')}
                    </span>
                  ) : t('verifyOtp')}
                </Button>

                <Button
                  type="button"
                  variant="ghost"
                  className="w-full"
                  onClick={() => {
                    setOtpSent(false);
                    setOtp('');
                  }}
                >
                  {t('changeMobileNumber')}
                </Button>
              </form>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default OtpLogin;
