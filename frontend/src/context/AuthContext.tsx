import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, AuthResponse } from '../types';

// Simple logging utility for frontend
const logger = {
  info: (message: string, data?: any) => {
    console.log(`[AUTH-CONTEXT-INFO] ${message}`, data || '');
  },
  warn: (message: string, data?: any) => {
    console.warn(`[AUTH-CONTEXT-WARN] ${message}`, data || '');
  },
  error: (message: string, data?: any) => {
    console.error(`[AUTH-CONTEXT-ERROR] ${message}`, data || '');
  },
  debug: (message: string, data?: any) => {
    console.debug(`[AUTH-CONTEXT-DEBUG] ${message}`, data || '');
  }
};

interface AuthContextType {
  user: User | null;
  token: string | null;
  login: (response: AuthResponse) => void;
  logout: () => void;
  updateUser: (userData: User) => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(null);
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {
    logger.debug('AuthProvider initializing, checking for stored credentials');
    // Load user data from localStorage on mount
    const storedToken = localStorage.getItem('token');
    const storedUser = localStorage.getItem('user');
    
    if (storedToken && storedUser) {
      logger.info('Found stored credentials, restoring user session', { 
        hasToken: !!storedToken, 
        hasUser: !!storedUser 
      });
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    } else {
      logger.debug('No stored credentials found');
    }
  }, []);

  const login = (response: AuthResponse) => {
    logger.info('Processing login response', { 
      hasToken: !!response.token, 
      hasId: !!response.id,
      email: response.email 
    });
    
    if (response.token && response.id) {
      const userData: User = {
        id: response.id,
        email: response.email!,
        firstName: response.firstName!,
        lastName: response.lastName!,
        twoFactorMethod: response.twoFactorMethod,
        isTwoFactorEnabled: response.isTwoFactorEnabled || false,
      };
      
      logger.info('Setting user session', { 
        userId: userData.id, 
        email: userData.email,
        twoFactorEnabled: userData.isTwoFactorEnabled 
      });
      
      setToken(response.token);
      setUser(userData);
      localStorage.setItem('token', response.token);
      localStorage.setItem('user', JSON.stringify(userData));
    } else {
      logger.warn('Login response missing required data', { response });
    }
  };

  const updateUser = (userData: User) => {
    logger.info('Updating user data in AuthContext', { 
      userId: userData.id, 
      email: userData.email,
      twoFactorEnabled: userData.isTwoFactorEnabled,
      twoFactorMethod: userData.twoFactorMethod,
      timestamp: new Date().toISOString()
    });
    setUser(userData);
    localStorage.setItem('user', JSON.stringify(userData));
    logger.info('User data updated successfully in AuthContext and localStorage');
  };

  const logout = () => {
    logger.info('Processing logout, clearing user session');
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    logger.info('User session cleared successfully');
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, updateUser, isAuthenticated: !!user }}>
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
