import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, ArrowLeft, Lock, KeyRound, Check } from 'lucide-react';
import apiClient from '@/services/apiClient';

const ResetPassword: React.FC = () => {
  const { t } = useLanguage();
  const { toast } = useToast();
  const navigate = useNavigate();

  const [mobile, setMobile] = useState('');
  const [otp, setOtp] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [step, setStep] = useState<'otp' | 'password'>('otp');
  const [token, setToken] = useState('');

  useEffect(() => {
    const storedMobile = sessionStorage.getItem('reset_mobile');
    if (!storedMobile) {
      toast({ title: t('enterMobileFirst'), variant: 'destructive' });
      navigate('/forgot-password');
      return;
    }
    setMobile(storedMobile);
  }, [navigate, toast, t]);

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault();

    if (otp.length !== 6) {
      toast({ title: t('enter6DigitOtp'), variant: 'destructive' });
      return;
    }

    setLoading(true);
    try {
      const response = await apiClient.post('/auth/verify-otp', {
        mobile,
        otp,
        action: 'reset_password'
      });

      setToken(response.data.token);
      setStep('password');
      toast({ title: t('otpVerified'), description: t('setNewPassword') });
    } catch (error: unknown) {
      const axiosError = error as { response?: { data?: { error?: string } } };
      toast({
        title: t('wrongOtp'),
        description: axiosError.response?.data?.error || t('tryAgain'),
        variant: 'destructive'
      });
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();

    // Strong password validation
    const hasUpperCase = /[A-Z]/.test(password);
    const hasLowerCase = /[a-z]/.test(password);
    const hasNumber = /[0-9]/.test(password);
    const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);

    if (password.length < 8) {
      toast({ title: 'Password must be at least 8 characters', variant: 'destructive' });
      return;
    }

    if (!hasUpperCase) {
      toast({ title: 'Password must contain at least one uppercase letter (A-Z)', variant: 'destructive' });
      return;
    }

    if (!hasLowerCase) {
      toast({ title: 'Password must contain at least one lowercase letter (a-z)', variant: 'destructive' });
      return;
    }

    if (!hasNumber) {
      toast({ title: 'Password must contain at least one number (0-9)', variant: 'destructive' });
      return;
    }

    if (!hasSpecialChar) {
      toast({ title: 'Password must contain at least one special character (!@#$%^&*)', variant: 'destructive' });
      return;
    }

    if (password !== confirmPassword) {
      toast({ title: t('passwordMismatch'), variant: 'destructive' });
      return;
    }

    setLoading(true);
    try {
      await apiClient.post('/auth/update-password', { password }, {
        headers: { Authorization: `Bearer ${token}` }
      });

      sessionStorage.removeItem('reset_mobile');

      toast({ title: t('passwordChanged'), description: t('nowLogin') });
      navigate('/login');
    } catch (error: unknown) {
      const axiosError = error as { response?: { data?: { error?: string } } };
      toast({
        title: t('passwordChangeFailed'),
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
            <Link to="/forgot-password" className="inline-flex items-center text-muted-foreground hover:text-primary mb-6 transition-colors">
              <ArrowLeft className="w-4 h-4 mr-2" />
              {t('goBack')}
            </Link>

            <div className="text-center mb-8">
              <div className="w-16 h-16 mx-auto mb-4 rounded-full bg-primary/10 flex items-center justify-center">
                {step === 'otp' ? (
                  <KeyRound className="w-8 h-8 text-primary" />
                ) : (
                  <Lock className="w-8 h-8 text-primary" />
                )}
              </div>
              <h1 className="text-2xl font-bold text-foreground">
                {step === 'otp' ? t('enterOtp') : t('newPassword')}
              </h1>
              <p className="text-muted-foreground mt-2 text-sm">
                {step === 'otp'
                  ? `${t('otpSentTo')} ${mobile}`
                  : t('enterNewPassword')
                }
              </p>
            </div>

            {step === 'otp' ? (
              <form onSubmit={handleVerifyOtp} className="space-y-5">
                <div className="animate-fade-in">
                  <Label htmlFor="otp" className="form-label">OTP</Label>
                  <Input
                    id="otp"
                    type="text"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                    className="input-field text-center text-2xl tracking-widest"
                    placeholder="● ● ● ● ● ●"
                    maxLength={6}
                  />
                </div>

                <Button
                  type="submit"
                  className="btn-primary w-full h-12 text-base"
                  disabled={loading || otp.length !== 6}
                >
                  {loading ? (
                    <span className="flex items-center gap-2">
                      <span className="w-4 h-4 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                      {t('verifying')}
                    </span>
                  ) : (
                    <span className="flex items-center gap-2">
                      <Check className="w-4 h-4" />
                      {t('verifyOtp')}
                    </span>
                  )}
                </Button>
              </form>
            ) : (
              <form onSubmit={handleResetPassword} className="space-y-5">
                <div className="animate-fade-in">
                  <Label htmlFor="password" className="form-label">{t('newPassword')}</Label>
                  <div className="relative group">
                    <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                    <Input
                      id="password"
                      type="password"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      className="input-field pl-12"
                      placeholder={t('minChars')}
                    />
                  </div>
                </div>

                <div className="animate-fade-in" style={{ animationDelay: '0.1s' }}>
                  <Label htmlFor="confirmPassword" className="form-label">{t('confirmPassword')}</Label>
                  <div className="relative group">
                    <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                    <Input
                      id="confirmPassword"
                      type="password"
                      value={confirmPassword}
                      onChange={(e) => setConfirmPassword(e.target.value)}
                      className="input-field pl-12"
                      placeholder={t('reenterPassword')}
                    />
                  </div>
                </div>

                <Button
                  type="submit"
                  className="btn-primary w-full h-12 text-base"
                  disabled={loading || password.length < 8}
                >
                  {loading ? (
                    <span className="flex items-center gap-2">
                      <span className="w-4 h-4 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                      {t('changing')}
                    </span>
                  ) : (
                    <span className="flex items-center gap-2">
                      <Check className="w-4 h-4" />
                      {t('changePassword')}
                    </span>
                  )}
                </Button>
              </form>
            )}
          </div>
        </div>
      </main>
    </div>
  );
};

export default ResetPassword;
