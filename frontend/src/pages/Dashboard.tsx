import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '@/contexts/AuthContext';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';
import { billService } from '@/services/billService';
import LanguageSwitch from '@/components/LanguageSwitch';
import { Banana, Plus, History, LogOut, FileText, Calendar } from 'lucide-react';

const Dashboard: React.FC = () => {
  const { user, signOut } = useAuth();
  const { t } = useLanguage();
  const navigate = useNavigate();
  const [todayCount, setTodayCount] = useState(0);
  const [totalCount, setTotalCount] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchStats = async () => {
      try {
        const [today, total] = await Promise.all([
          billService.getTodayCount(),
          billService.getTotalCount(),
        ]);
        setTodayCount(today);
        setTotalCount(total);
      } catch (error) {
        console.error('Error fetching stats:', error);
      } finally {
        setLoading(false);
      }
    };
    fetchStats();
  }, []);

  const handleLogout = async () => {
    await signOut();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-background via-muted/20 to-background">
      <header className="header-bar">
        <div className="max-w-7xl mx-auto px-4 py-4 flex justify-between items-center">
          <div className="flex items-center gap-3">
            <div className="logo-container">
              <Banana className="w-6 h-6 text-primary-foreground" />
            </div>
            <span className="text-xl font-bold text-foreground">Banana Bill</span>
          </div>
          <div className="flex items-center gap-3">
            <LanguageSwitch />
            <Button variant="ghost" size="icon" onClick={handleLogout} title={t('logout')} className="hover:bg-destructive/10 hover:text-destructive transition-colors">
              <LogOut className="w-5 h-5" />
            </Button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="mb-8 animate-fade-in">
          <h1 className="text-3xl font-bold text-foreground">{t('welcome')} 👋</h1>
          <p className="text-muted-foreground mt-2">Manage your banana bills efficiently</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
          <div className="stat-card animate-slide-up" style={{ animationDelay: '0.1s' }}>
            <div className="flex items-center gap-5">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary/20 to-primary/5 flex items-center justify-center border border-primary/20">
                <Calendar className="w-8 h-8 text-primary" />
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">{t('todaysBills')}</p>
                <p className="text-4xl font-bold text-foreground mt-1">{loading ? '—' : todayCount}</p>
              </div>
            </div>
          </div>

          <div className="stat-card animate-slide-up" style={{ animationDelay: '0.15s' }}>
            <div className="flex items-center gap-5">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-accent/20 to-accent/5 flex items-center justify-center border border-accent/20">
                <FileText className="w-8 h-8 text-accent" />
              </div>
              <div>
                <p className="text-sm font-medium text-muted-foreground">{t('totalBills')}</p>
                <p className="text-4xl font-bold text-foreground mt-1">{loading ? '—' : totalCount}</p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 animate-slide-up" style={{ animationDelay: '0.2s' }}>
          <Link to="/create-bill" className="block group">
            <Button className="btn-primary w-full h-16 text-lg gap-3 group-hover:shadow-golden">
              <Plus className="w-6 h-6 transition-transform group-hover:rotate-90 duration-300" />
              {t('createNewBill')}
            </Button>
          </Link>

          <Link to="/history" className="block group">
            <Button variant="outline" className="w-full h-16 text-lg gap-3 border-2 rounded-xl text-foreground hover:text-foreground hover:border-primary/50 hover:bg-primary/5 transition-all duration-300">
              <History className="w-6 h-6 text-gray-600 transition-transform group-hover:-rotate-45 duration-300" />
              {t('billHistory')}
            </Button>
          </Link>

          <Link to="/reports" className="block group">
            <Button variant="outline" className="w-full h-16 text-lg gap-3 border-2 rounded-xl text-foreground hover:text-foreground hover:border-accent/50 hover:bg-accent/5 transition-all duration-300">
              <FileText className="w-6 h-6 text-gray-600 transition-transform group-hover:scale-110 duration-300" />
              {t('monthlyReports')}
            </Button>
          </Link>
        </div>
      </main>
    </div>
  );
};

export default Dashboard;
