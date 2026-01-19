import { Component, ErrorInfo, ReactNode } from 'react';
import { Button } from '@/components/ui/button';
import { AlertTriangle } from 'lucide-react';

interface Props {
    children: ReactNode;
    fallback?: ReactNode;
}

interface State {
    hasError: boolean;
    error: Error | null;
}

/**
 * Error Boundary Component
 * Catches JavaScript errors in child component tree
 * Prevents entire app crash
 */
class ErrorBoundary extends Component<Props, State> {
    constructor(props: Props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error: Error): State {
        return { hasError: true, error };
    }

    componentDidCatch(error: Error, errorInfo: ErrorInfo) {
        // Log to error reporting service (e.g., Sentry)
        console.error('ErrorBoundary caught an error:', error, errorInfo);

        // TODO: Send to error tracking service
        // logger.error('Component Error', { error, errorInfo });
    }

    handleReset = () => {
        this.setState({ hasError: false, error: null });
    };

    render() {
        if (this.state.hasError) {
            // Custom fallback UI
            if (this.props.fallback) {
                return this.props.fallback;
            }

            // Default error UI
            return (
                <div className="min-h-screen flex items-center justify-center bg-background p-4">
                    <div className="max-w-md w-full bg-card rounded-lg shadow-lg p-6 text-center">
                        <AlertTriangle className="w-16 h-16 text-destructive mx-auto mb-4" />
                        <h1 className="text-2xl font-bold mb-2">Oops! Something went wrong</h1>
                        <p className="text-muted-foreground mb-6">
                            {this.state.error?.message || 'An unexpected error occurred'}
                        </p>
                        <div className="space-y-2">
                            <Button onClick={this.handleReset} className="w-full">
                                Try Again
                            </Button>
                            <Button
                                variant="outline"
                                onClick={() => window.location.href = '/dashboard'}
                                className="w-full"
                            >
                                Go to Dashboard
                            </Button>
                        </div>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
