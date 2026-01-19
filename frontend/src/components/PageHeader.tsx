import React, { memo } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { ChevronLeft } from 'lucide-react';
import { Banana } from 'lucide-react';
import { useLanguage } from '@/contexts/LanguageContext';
import LanguageSwitch from './LanguageSwitch';

interface PageHeaderProps {
    title: string;
    backTo?: string;
    showLogo?: boolean;
    showLanguageSwitch?: boolean;
    rightContent?: React.ReactNode;
    className?: string;
}

/**
 * Reusable Page Header Component
 * Provides consistent layout for page titles, navigation, and actions
 */
const PageHeader: React.FC<PageHeaderProps> = memo(({
    title,
    backTo,
    showLogo = true,
    showLanguageSwitch = true,
    rightContent,
    className = ''
}) => {
    const { t } = useLanguage();

    return (
        <header className={`bg-white border-b border-gray-200 sticky top-0 z-30 ${className}`}>
            <div className="max-w-7xl mx-auto px-4 py-4 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">

                {/* Left Section: Back Button / Logo / Title */}
                <div className="flex items-center gap-3 w-full sm:w-auto">
                    {backTo && (
                        <Link to={backTo}>
                            <Button variant="ghost" size="icon" className="hover:bg-gray-100 rounded-full">
                                <ChevronLeft className="w-5 h-5 text-gray-600" />
                            </Button>
                        </Link>
                    )}

                    {showLogo && (
                        <div className="bg-yellow-100 p-2 rounded-lg">
                            <Banana className="w-5 h-5 text-yellow-600" />
                        </div>
                    )}

                    <h1 className="text-xl font-bold text-gray-800 truncate flex-1 sm:flex-none">
                        {title}
                    </h1>
                </div>

                {/* Right Section: Actions / Language Switch */}
                <div className="flex items-center gap-3 w-full sm:w-auto justify-end">
                    {rightContent}
                    {showLanguageSwitch && <LanguageSwitch />}
                </div>

            </div>
        </header>
    );
});

PageHeader.displayName = 'PageHeader';

export default PageHeader;
