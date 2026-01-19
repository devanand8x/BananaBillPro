import { Suspense, lazy } from 'react';
import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "@/contexts/AuthContext";
import { LanguageProvider } from "@/contexts/LanguageContext";
import ErrorBoundary from "@/components/ErrorBoundary";
import { CACHE_TIME } from "@/lib/constants";

// ============================================================
// LAZY LOADED PAGES - Code splitting for better performance
// Each page is loaded only when needed, reducing initial bundle
// ============================================================

// Auth pages (smaller, load quickly)
const LoginPage = lazy(() => import("./pages/LoginPage"));
const RegisterPage = lazy(() => import("./pages/RegisterPage"));
const ForgotPassword = lazy(() => import("./pages/ForgotPassword"));
const ResetPassword = lazy(() => import("./pages/ResetPassword"));
const OtpLogin = lazy(() => import("./pages/OtpLogin"));

// Main app pages (larger, lazy load for better performance)
const Dashboard = lazy(() => import("./pages/Dashboard"));
const CreateBill = lazy(() => import("./pages/CreateBill"));
const BillPreview = lazy(() => import("./pages/BillPreview"));
const BillHistory = lazy(() => import("./pages/BillHistory"));
const EditBill = lazy(() => import("./pages/EditBill"));

// Report pages (heaviest, definitely lazy load)
const Reports = lazy(() => import("./pages/Reports"));
const FarmerReport = lazy(() => import("./pages/FarmerReport"));
const StatementPreview = lazy(() => import("./pages/StatementPreview"));

// Static pages
const NotFound = lazy(() => import("./pages/NotFound"));

// ============================================================
// QUERY CLIENT - Configured for optimal caching
// ============================================================
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: CACHE_TIME.MEDIUM,
      gcTime: CACHE_TIME.LONG,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
});

// ============================================================
// LOADING FALLBACK - Shown while lazy components load
// ============================================================
const PageLoader = () => (
  <div className="min-h-screen flex items-center justify-center bg-background">
    <div className="flex flex-col items-center gap-3">
      <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary"></div>
      <p className="text-sm text-muted-foreground">Loading...</p>
    </div>
  </div>
);

// ============================================================
// ROUTE GUARDS
// ============================================================
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return <PageLoader />;
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return <Suspense fallback={<PageLoader />}>{children}</Suspense>;
};

const PublicRoute = ({ children }: { children: React.ReactNode }) => {
  const { user, loading } = useAuth();

  if (loading) {
    return <PageLoader />;
  }

  if (user) {
    return <Navigate to="/dashboard" replace />;
  }

  return <Suspense fallback={<PageLoader />}>{children}</Suspense>;
};

// ============================================================
// APP COMPONENT - With Error Boundaries
// ============================================================
const App = () => (
  <QueryClientProvider client={queryClient}>
    <LanguageProvider>
      <AuthProvider>
        <TooltipProvider>
          <Toaster />
          <Sonner />
          <ErrorBoundary>
            <BrowserRouter>
              <Suspense fallback={<PageLoader />}>
                <Routes>
                  {/* Redirects */}
                  <Route path="/" element={<Navigate to="/login" replace />} />

                  {/* Public Routes (Auth) - Separate error boundary */}
                  <Route path="/login" element={
                    <ErrorBoundary>
                      <PublicRoute><LoginPage /></PublicRoute>
                    </ErrorBoundary>
                  } />
                  <Route path="/register" element={
                    <ErrorBoundary>
                      <PublicRoute><RegisterPage /></PublicRoute>
                    </ErrorBoundary>
                  } />
                  <Route path="/forgot-password" element={<PublicRoute><ForgotPassword /></PublicRoute>} />
                  <Route path="/reset-password" element={<PublicRoute><ResetPassword /></PublicRoute>} />
                  <Route path="/otp-login" element={<PublicRoute><OtpLogin /></PublicRoute>} />

                  {/* Protected Routes (App) - Separate error boundary */}
                  <Route path="/dashboard" element={
                    <ErrorBoundary>
                      <ProtectedRoute><Dashboard /></ProtectedRoute>
                    </ErrorBoundary>
                  } />
                  <Route path="/create-bill" element={
                    <ErrorBoundary>
                      <ProtectedRoute><CreateBill /></ProtectedRoute>
                    </ErrorBoundary>
                  } />
                  <Route path="/bill/:id" element={<ProtectedRoute><BillPreview /></ProtectedRoute>} />
                  <Route path="/history" element={<ProtectedRoute><BillHistory /></ProtectedRoute>} />
                  <Route path="/edit-bill/:id" element={<ProtectedRoute><EditBill /></ProtectedRoute>} />

                  {/* Protected Routes (Reports) - Separate error boundary */}
                  <Route path="/reports" element={
                    <ErrorBoundary>
                      <ProtectedRoute><Reports /></ProtectedRoute>
                    </ErrorBoundary>
                  } />
                  <Route path="/farmer-report" element={<ProtectedRoute><FarmerReport /></ProtectedRoute>} />
                  <Route path="/statement" element={<ProtectedRoute><StatementPreview /></ProtectedRoute>} />

                  {/* 404 */}
                  <Route path="*" element={<NotFound />} />
                </Routes>
              </Suspense>
            </BrowserRouter>
          </ErrorBoundary>
        </TooltipProvider>
      </AuthProvider>
    </LanguageProvider>
  </QueryClientProvider>
);

export default App;
