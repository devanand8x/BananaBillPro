import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { useToast } from '@/hooks/use-toast';
import apiClient, { setTokens, clearTokens, getAccessToken, getRefreshToken } from '@/services/apiClient';
import { AxiosError } from 'axios';

// Helper to extract error message from axios errors
const getErrorMessage = (error: unknown, fallback: string): string => {
  if (error instanceof AxiosError) {
    return error.response?.data?.message ?? fallback;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return fallback;
};

interface User {
  id: string;
  name: string;
  mobileNumber: string;
  email: string;
}

interface AuthContextType {
  user: User | null;
  loading: boolean;
  signIn: (mobile: string, password: string) => Promise<{ error: Error | null }>;
  signUp: (name: string, mobile: string, password: string) => Promise<{ error: Error | null }>;
  signOut: () => Promise<void>;
  signOutAll: () => Promise<void>;
  sendOtp: (mobile: string) => Promise<{ error: Error | null }>;
  verifyOtp: (mobile: string, token: string) => Promise<{ error: Error | null }>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

/**
 * Authentication Provider
 * Manages user authentication state with JWT access and refresh tokens
 */
export const AuthProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const { toast } = useToast();

  useEffect(() => {
    // Check if user is logged in
    const accessToken = getAccessToken();
    const savedUser = localStorage.getItem('user');

    if (accessToken && savedUser) {
      setUser(JSON.parse(savedUser));
    }

    setLoading(false);
  }, []);

  /**
   * Sign in with mobile and password
   * Returns access token (15 min) and refresh token (7 days)
   */
  const signIn = async (mobile: string, password: string) => {
    try {
      const response = await apiClient.post('/auth/login', { mobile, password });
      const data = response.data.data || response.data;

      // Store tokens
      setTokens(data.accessToken, data.refreshToken);

      // Store user info
      const userData = {
        id: data.userId,
        name: data.userName,
        mobileNumber: mobile,
        email: ''
      };
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);

      toast({ title: 'Login successful!' });
      return { error: null };
    } catch (error: unknown) {
      const message = getErrorMessage(error, 'Login failed');
      toast({ title: 'Login failed', description: message, variant: 'destructive' });
      return { error: new Error(message) };
    }
  };

  /**
   * Sign up with name, mobile and password
   */
  const signUp = async (name: string, mobile: string, password: string) => {
    try {
      const response = await apiClient.post('/auth/register', { name, mobile, password });
      const data = response.data.data || response.data;

      // Store tokens
      setTokens(data.accessToken, data.refreshToken);

      // Store user info
      const userData = {
        id: data.userId,
        name: name,
        mobileNumber: mobile,
        email: ''
      };
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);

      toast({ title: 'Registration successful!' });
      return { error: null };
    } catch (error: unknown) {
      const message = getErrorMessage(error, 'Registration failed');
      toast({ title: 'Registration failed', description: message, variant: 'destructive' });
      return { error: new Error(message) };
    }
  };

  /**
   * Sign out - revoke current session
   */
  const signOut = async () => {
    try {
      const refreshToken = getRefreshToken();
      if (refreshToken) {
        await apiClient.post('/auth/logout', { refreshToken });
      }
    } catch (error) {
      console.warn('Logout API call failed, clearing local state anyway');
    } finally {
      clearTokens();
      localStorage.removeItem('user');
      setUser(null);
      toast({ title: 'Logged out successfully' });
    }
  };

  /**
   * Sign out from all devices - revoke all sessions
   */
  const signOutAll = async () => {
    try {
      await apiClient.post('/auth/logout-all');
      toast({ title: 'Logged out from all devices' });
    } catch (error) {
      console.warn('Logout all failed');
    } finally {
      clearTokens();
      localStorage.removeItem('user');
      setUser(null);
    }
  };

  /**
   * Send OTP to mobile number
   */
  const sendOtp = async (mobile: string) => {
    try {
      await apiClient.post('/auth/send-otp', { mobile });
      toast({ title: 'OTP Sent!', description: 'Check your phone for the OTP' });
      return { error: null };
    } catch (error: unknown) {
      const message = getErrorMessage(error, 'Failed to send OTP');
      toast({ title: 'Error', description: message, variant: 'destructive' });
      return { error: new Error(message) };
    }
  };

  /**
   * Verify OTP and login
   */
  const verifyOtp = async (mobile: string, token: string) => {
    try {
      const response = await apiClient.post('/auth/verify-otp', { mobile, otp: token });
      const data = response.data.data || response.data;

      // Store tokens
      setTokens(data.accessToken, data.refreshToken);

      // Store user info
      const userData = {
        id: data.userId,
        name: data.userName || mobile,
        mobileNumber: mobile,
        email: ''
      };
      localStorage.setItem('user', JSON.stringify(userData));
      setUser(userData);

      toast({ title: 'OTP verified successfully!' });
      return { error: null };
    } catch (error: unknown) {
      const message = getErrorMessage(error, 'OTP verification failed');
      toast({ title: 'Verification failed', description: message, variant: 'destructive' });
      return { error: new Error(message) };
    }
  };

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      signIn,
      signUp,
      signOut,
      signOutAll,
      sendOtp,
      verifyOtp,
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
