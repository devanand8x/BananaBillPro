import React from 'react';
import { useLanguage } from '@/contexts/LanguageContext';
import { Button } from '@/components/ui/button';

const LanguageSwitch: React.FC = () => {
  const { language, setLanguage } = useLanguage();

  return (
    <div className="flex gap-1 bg-secondary rounded-lg p-1">
      {(['en', 'hi', 'mr'] as const).map((lang) => (
        <Button
          key={lang}
          variant={language === lang ? 'default' : 'ghost'}
          size="sm"
          onClick={() => setLanguage(lang)}
          className={language === lang ? 'bg-primary text-primary-foreground' : 'text-muted-foreground'}
        >
          {lang.toUpperCase()}
        </Button>
      ))}
    </div>
  );
};

export default LanguageSwitch;
