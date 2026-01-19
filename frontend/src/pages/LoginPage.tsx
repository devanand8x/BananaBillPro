import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, Phone, Lock } from 'lucide-react';

const LoginPage: React.FC = () => {
  const [mobile, setMobile] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { signIn } = useAuth();
  const { t } = useLanguage();
  const { toast } = useToast();
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!mobile || !password) {
      toast({ title: 'Please fill all fields', variant: 'destructive' });
      return;
    }

    setLoading(true);
    const { error } = await signIn(mobile, password);
    setLoading(false);

    if (error) {
      toast({ title: 'Login failed', description: error.message, variant: 'destructive' });
    } else {
      navigate('/dashboard');
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-muted/30 to-background flex flex-col">
      <header className="flex justify-between items-center p-4 border-b border-border/50 bg-card/50 backdrop-blur-sm">
        <div className="flex items-center gap-3">
          <div className="logo-container">
            <Banana className="w-6 h-6 text-primary-foreground" />
          </div>
          <span className="text-xl font-bold bg-gradient-to-r from-foreground to-foreground/70 bg-clip-text">Banana Bill</span>
        </div>
        <LanguageSwitch />
      </header>

      <main className="flex-1 flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          <div className="bg-card/95 backdrop-blur-md rounded-3xl shadow-xl p-8 border border-border/50 animate-scale-in">
            <div className="text-center mb-8">
              <div className="logo-container mx-auto mb-4 w-16 h-16">
                <Banana className="w-8 h-8 text-primary-foreground" />
              </div>
              <h1 className="text-2xl font-bold text-foreground">{t('login')}</h1>
              <p className="text-muted-foreground mt-2 text-sm">Welcome back! Sign in to continue</p>
            </div>

            <form onSubmit={handleLogin} className="space-y-5">
              <div className="animate-fade-in" style={{ animationDelay: '0.1s' }}>
                <Label htmlFor="mobile" className="form-label">{t('mobileNumber')}</Label>
                <div className="relative group">
                  <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input
                    id="mobile"
                    type="tel"
                    value={mobile}
                    onChange={(e) => setMobile(e.target.value)}
                    className="input-field pl-12"
                    placeholder="Enter mobile number"
                  />
                </div>
              </div>

              <div className="animate-fade-in" style={{ animationDelay: '0.15s' }}>
                <Label htmlFor="password" className="form-label">{t('password')}</Label>
                <div className="relative group">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="input-field pl-12"
                    placeholder="Enter password"
                  />
                </div>
              </div>

              <Button type="submit" className="btn-primary w-full h-12 text-base animate-fade-in" style={{ animationDelay: '0.2s' }} disabled={loading}>
                {loading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                    Loading...
                  </span>
                ) : t('login')}
              </Button>
            </form>

            <div className="mt-6 space-y-3 animate-fade-in" style={{ animationDelay: '0.25s' }}>
              <Link to="/otp-login">
                <Button variant="outline" className="w-full h-11 border-yellow-400 text-gray-700 hover:bg-yellow-50 hover:text-gray-900 hover:border-yellow-500">
                  <Phone className="w-4 h-4 mr-2" />
                  Login with OTP
                </Button>
              </Link>

              <Link to="/forgot-password" className="block text-center text-sm text-primary hover:underline">
                Forgot Password?
              </Link>
            </div>

            <div className="mt-6 text-center animate-fade-in" style={{ animationDelay: '0.3s' }}>
              <p className="text-sm text-muted-foreground">
                Don't have an account?{' '}
                <Link to="/register" className="text-primary font-semibold hover:underline transition-colors">
                  {t('register')}
                </Link>
              </p>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
};

export default LoginPage;
