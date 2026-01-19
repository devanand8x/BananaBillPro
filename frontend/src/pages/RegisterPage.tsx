import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { useToast } from '@/hooks/use-toast';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, User, Phone, Lock } from 'lucide-react';

const RegisterPage: React.FC = () => {
  const [name, setName] = useState('');
  const [mobile, setMobile] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { signUp } = useAuth();
  const { t } = useLanguage();
  const { toast } = useToast();
  const navigate = useNavigate();

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name || !mobile || !password || !confirmPassword) {
      toast({ title: 'Please fill all fields', variant: 'destructive' });
      return;
    }
    if (password !== confirmPassword) {
      toast({ title: 'Passwords do not match', variant: 'destructive' });
      return;
    }
    
    setLoading(true);
    const { error } = await signUp(name, mobile, password);
    setLoading(false);
    
    if (error) {
      toast({ title: 'Registration failed', description: error.message, variant: 'destructive' });
    } else {
      toast({ title: 'Registration successful!' });
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
      
      <main className="flex-1 flex items-center justify-center p-4 py-8">
        <div className="w-full max-w-md">
          <div className="bg-card/95 backdrop-blur-md rounded-3xl shadow-xl p-8 border border-border/50 animate-scale-in">
            <div className="text-center mb-6">
              <div className="logo-container mx-auto mb-4 w-16 h-16">
                <Banana className="w-8 h-8 text-primary-foreground" />
              </div>
              <h1 className="text-2xl font-bold text-foreground">{t('register')}</h1>
              <p className="text-muted-foreground mt-2 text-sm">Create your account to get started</p>
            </div>
            
            <form onSubmit={handleRegister} className="space-y-4">
              <div className="animate-fade-in" style={{ animationDelay: '0.1s' }}>
                <Label htmlFor="name" className="form-label">{t('name')}</Label>
                <div className="relative group">
                  <User className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input id="name" value={name} onChange={(e) => setName(e.target.value)} className="input-field pl-12" placeholder="Enter your name" />
                </div>
              </div>
              
              <div className="animate-fade-in" style={{ animationDelay: '0.15s' }}>
                <Label htmlFor="mobile" className="form-label">{t('mobileNumber')}</Label>
                <div className="relative group">
                  <Phone className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input id="mobile" type="tel" value={mobile} onChange={(e) => setMobile(e.target.value)} className="input-field pl-12" placeholder="Enter mobile number" />
                </div>
              </div>
              
              <div className="animate-fade-in" style={{ animationDelay: '0.2s' }}>
                <Label htmlFor="password" className="form-label">{t('password')}</Label>
                <div className="relative group">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} className="input-field pl-12" placeholder="Enter password" />
                </div>
              </div>
              
              <div className="animate-fade-in" style={{ animationDelay: '0.25s' }}>
                <Label htmlFor="confirmPassword" className="form-label">{t('confirmPassword')}</Label>
                <div className="relative group">
                  <Lock className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-muted-foreground group-focus-within:text-primary transition-colors" />
                  <Input id="confirmPassword" type="password" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} className="input-field pl-12" placeholder="Confirm password" />
                </div>
              </div>
              
              <Button type="submit" className="btn-primary w-full h-12 text-base animate-fade-in" style={{ animationDelay: '0.3s' }} disabled={loading}>
                {loading ? (
                  <span className="flex items-center gap-2">
                    <span className="w-4 h-4 border-2 border-primary-foreground/30 border-t-primary-foreground rounded-full animate-spin" />
                    Creating account...
                  </span>
                ) : t('register')}
              </Button>
            </form>
            
            <p className="mt-6 text-center text-sm text-muted-foreground animate-fade-in" style={{ animationDelay: '0.35s' }}>
              Already have an account?{' '}
              <Link to="/login" className="text-primary font-semibold hover:underline transition-colors">{t('login')}</Link>
            </p>
          </div>
        </div>
      </main>
    </div>
  );
};

export default RegisterPage;
