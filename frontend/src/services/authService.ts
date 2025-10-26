import api from './api';
import { SignupData, VerificationData, AuthResponse } from '../types';

// Simple logging utility for frontend
const logger = {
  info: (message: string, data?: any) => {
    console.log(`[INFO] ${message}`, data || '');
  },
  warn: (message: string, data?: any) => {
    console.warn(`[WARN] ${message}`, data || '');
  },
  error: (message: string, data?: any) => {
    console.error(`[ERROR] ${message}`, data || '');
  },
  debug: (message: string, data?: any) => {
    console.debug(`[DEBUG] ${message}`, data || '');
  }
};

export const authService = {
  async signup(data: SignupData): Promise<AuthResponse> {
    logger.info('Starting signup process', { email: data.email });
    try {
      const response = await api.post<AuthResponse>('/signup', data);
      logger.info('Signup successful', { email: data.email });
      return response.data;
    } catch (error) {
      logger.error('Signup failed', { email: data.email, error });
      throw error;
    }
  },

  async login(email: string, password: string): Promise<AuthResponse> {
    logger.info('Initiating login', { email });
    try {
      const response = await api.post<AuthResponse>('/login', { email, password });
      logger.info('Login initiation successful, verification code sent', { email });
      return response.data;
    } catch (error) {
      logger.error('Login initiation failed', { email, error });
      throw error;
    }
  },

  async verifyLogin(email: string, code: string): Promise<AuthResponse> {
    logger.info('Completing login', { email });
    try {
      const response = await api.post<AuthResponse>('/login-verify', { email, code });
      logger.info('Login completed successfully', { email });
      return response.data;
    } catch (error) {
      logger.error('Login completion failed', { email, error });
      throw error;
    }
  },

  async verifyEmail(data: VerificationData): Promise<AuthResponse> {
    logger.info('Starting email verification', { email: data.email });
    try {
      const response = await api.post<AuthResponse>('/verify-email', data);
      logger.info('Email verification successful', { email: data.email });
      return response.data;
    } catch (error) {
      logger.error('Email verification failed', { email: data.email, error });
      throw error;
    }
  },

  async resendCode(email: string, type: 'email' | 'phone'): Promise<AuthResponse> {
    logger.info('Resending verification code', { email, type });
    try {
      const response = await api.post<AuthResponse>(`/resend-code?email=${email}&type=${type}`);
      logger.info('Verification code resent successfully', { email, type });
      return response.data;
    } catch (error) {
      logger.error('Failed to resend verification code', { email, type, error });
      throw error;
    }
  },

  async getProfile(): Promise<AuthResponse> {
    logger.info('Fetching user profile');
    try {
      const response = await api.get<AuthResponse>('/profile');
      logger.info('User profile fetched successfully');
      return response.data;
    } catch (error) {
      logger.error('Failed to fetch user profile', { error });
      throw error;
    }
  },

  async changePassword(currentPassword: string, newPassword: string, confirmNewPassword: string): Promise<AuthResponse> {
    logger.info('Starting password change process');
    try {
      const response = await api.post<AuthResponse>('/change-password', {
        currentPassword,
        newPassword,
        confirmNewPassword,
      });
      logger.info('Password changed successfully');
      return response.data;
    } catch (error) {
      logger.error('Password change failed', { error });
      throw error;
    }
  },

  async change2FA(password: string, newTwoFactorMethod: string, phoneNumber?: string): Promise<AuthResponse> {
    logger.info('Starting 2FA method change', { newTwoFactorMethod });
    try {
      const response = await api.post<AuthResponse>('/change-2fa', {
        password,
        newTwoFactorMethod,
        phoneNumber,
      });
      logger.info('2FA method changed successfully', { newTwoFactorMethod });
      return response.data;
    } catch (error) {
      logger.error('2FA method change failed', { newTwoFactorMethod, error });
      throw error;
    }
  },

  async getAuthenticatorQR(): Promise<AuthResponse> {
    logger.info('Requesting authenticator QR code');
    try {
      const response = await api.get<AuthResponse>('/authenticator-qr');
      logger.info('Authenticator QR code generated successfully');
      return response.data;
    } catch (error) {
      logger.error('Failed to generate authenticator QR code', { error });
      throw error;
    }
  },

  async logout(): Promise<AuthResponse> {
    logger.info('Starting logout process');
    try {
      const response = await api.post<AuthResponse>('/logout');
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      logger.info('Logout successful');
      return response.data;
    } catch (error) {
      logger.error('Logout failed', { error });
      throw error;
    }
  },
};
