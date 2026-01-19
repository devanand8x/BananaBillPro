/**
 * Sentry initialization - Optional error tracking
 * Only loads in production with VITE_SENTRY_DSN set
 * 
 * This module is safe even if @sentry/react is not installed
 * The dynamic import will fail gracefully if package is missing
 */

export const initSentry = async (): Promise<void> => {
    // Only initialize in production with DSN configured
    if (!import.meta.env.PROD || !import.meta.env.VITE_SENTRY_DSN) {
        return;
    }

    try {
        // Dynamic import using string literal to avoid build-time resolution
        // This will fail gracefully if @sentry/react is not installed
        const sentryModuleName = '@sentry/react';
        const Sentry = await import(/* @vite-ignore */ sentryModuleName);

        if (Sentry && typeof Sentry.init === 'function') {
            Sentry.init({
                dsn: import.meta.env.VITE_SENTRY_DSN,
                environment: import.meta.env.MODE || 'production',
                integrations: [
                    Sentry.browserTracingIntegration?.(),
                    Sentry.replayIntegration?.(),
                ].filter(Boolean),
                tracesSampleRate: 0.1,
                replaysSessionSampleRate: 0.1,
                replaysOnErrorSampleRate: 1.0,
            });
        }
    } catch (error) {
        // Sentry package not available - expected in dev or if not installed
        // Silently continue without error tracking
        // Do not log to avoid console spam
    }
};
