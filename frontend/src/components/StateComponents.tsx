import React, { memo } from 'react';
import { Loader2, LucideIcon } from 'lucide-react';

interface LoadingStateProps {
    message?: string;
    fullScreen?: boolean;
    size?: 'sm' | 'md' | 'lg';
}

interface EmptyStateProps {
    icon?: LucideIcon;
    title: string;
    description?: string;
    action?: React.ReactNode;
}

interface ErrorStateProps {
    title?: string;
    description?: string;
    onRetry?: () => void;
}

/**
 * Loading State Component
 */
export const LoadingState: React.FC<LoadingStateProps> = memo(({
    message = 'Loading...',
    fullScreen = false,
    size = 'md'
}) => {
    const sizeClasses = {
        sm: 'w-4 h-4',
        md: 'w-8 h-8',
        lg: 'w-12 h-12'
    };

    const content = (
        <div className="flex flex-col items-center justify-center gap-3">
            <Loader2 className={`${sizeClasses[size]} animate-spin text-primary`} />
            {message && <p className="text-muted-foreground text-sm">{message}</p>}
        </div>
    );

    if (fullScreen) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-background">
                {content}
            </div>
        );
    }

    return (
        <div className="flex items-center justify-center py-12">
            {content}
        </div>
    );
});

LoadingState.displayName = 'LoadingState';

/**
 * Empty State Component
 */
export const EmptyState: React.FC<EmptyStateProps> = memo(({
    icon: Icon,
    title,
    description,
    action
}) => {
    return (
        <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
            {Icon && (
                <div className="w-16 h-16 rounded-full bg-muted flex items-center justify-center mb-4">
                    <Icon className="w-8 h-8 text-muted-foreground" />
                </div>
            )}
            <h3 className="text-lg font-semibold text-foreground mb-2">{title}</h3>
            {description && (
                <p className="text-muted-foreground text-sm max-w-sm mb-4">{description}</p>
            )}
            {action}
        </div>
    );
});

EmptyState.displayName = 'EmptyState';

/**
 * Error State Component
 */
export const ErrorState: React.FC<ErrorStateProps> = memo(({
    title = 'Something went wrong',
    description = 'An error occurred. Please try again.',
    onRetry
}) => {
    return (
        <div className="flex flex-col items-center justify-center py-12 px-4 text-center">
            <div className="w-16 h-16 rounded-full bg-destructive/10 flex items-center justify-center mb-4">
                <span className="text-3xl">⚠️</span>
            </div>
            <h3 className="text-lg font-semibold text-foreground mb-2">{title}</h3>
            <p className="text-muted-foreground text-sm max-w-sm mb-4">{description}</p>
            {onRetry && (
                <button
                    onClick={onRetry}
                    className="px-4 py-2 bg-primary text-primary-foreground rounded-lg hover:bg-primary/90 transition-colors"
                >
                    Try Again
                </button>
            )}
        </div>
    );
});

ErrorState.displayName = 'ErrorState';
