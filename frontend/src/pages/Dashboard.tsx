import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';
import { TwoFactorMethod } from '../types';
import './Dashboard.css';

// Simple logging utility for frontend
const logger = {
  info: (message: string, data?: any) => {
    console.log(`[DASHBOARD-INFO] ${message}`, data || '');
  },
  warn: (message: string, data?: any) => {
    console.warn(`[DASHBOARD-WARN] ${message}`, data || '');
  },
  error: (message: string, data?: any) => {
    console.error(`[DASHBOARD-ERROR] ${message}`, data || '');
  },
  debug: (message: string, data?: any) => {
    console.debug(`[DASHBOARD-DEBUG] ${message}`, data || '');
  }
};

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  // Helper function to format 2FA method display
  const formatTwoFactorMethod = (method?: TwoFactorMethod): string => {
    if (!method) return 'None';
    switch (method) {
      case TwoFactorMethod.EMAIL:
        return 'Email Verification';
      case TwoFactorMethod.AUTHENTICATOR_APP:
        return 'Authenticator App (TOTP)';
      default:
        return 'Unknown';
    }
  };

  // Helper function to get 2FA method icon
  const getTwoFactorIcon = (method?: TwoFactorMethod): string => {
    if (!method) return '❌';
    switch (method) {
      case TwoFactorMethod.EMAIL:
        return '✉️';
      case TwoFactorMethod.AUTHENTICATOR_APP:
        return '🔐';
      default:
        return '❓';
    }
  };

  React.useEffect(() => {
    logger.info('Dashboard component mounted', { 
      userId: user?.id, 
      email: user?.email,
      twoFactorEnabled: user?.isTwoFactorEnabled,
      twoFactorMethod: user?.twoFactorMethod
    });
  }, [user]);

  // Log when user data changes (for debugging real-time updates)
  React.useEffect(() => {
    if (user) {
      logger.info('User data updated in Dashboard', {
        userId: user.id,
        email: user.email,
        twoFactorEnabled: user.isTwoFactorEnabled,
        twoFactorMethod: user.twoFactorMethod,
        formattedMethod: formatTwoFactorMethod(user.twoFactorMethod),
        timestamp: new Date().toISOString()
      });
    } else {
      logger.info('User data is null in Dashboard');
    }
  }, [user]);

  const handleLogout = async () => {
    logger.info('Logout initiated from dashboard');
    try {
      await authService.logout();
      logout();
      logger.info('Logout successful, navigating to login');
      navigate('/login');
    } catch (error) {
      logger.error('Logout failed', { error });
    }
  };

  const handleNavigateToSettings = () => {
    logger.info('Navigating to settings page');
    navigate('/settings');
  };

  return (
    <div className="dashboard-container">
      <nav className="dashboard-nav">
        <h1>Profile</h1>
        <div className="nav-buttons">
          <button onClick={handleNavigateToSettings} className="nav-button">
            Settings
          </button>
          <button onClick={handleLogout} className="nav-button">
            Logout
          </button>
        </div>
      </nav>

      <div className="dashboard-content">
        <div className="welcome-card">
          <h2>Welcome, {user?.firstName}!</h2>
          <p>You have successfully logged in to your account.</p>
        </div>

        <div className="info-card">
          <h3>Account Information</h3>
          <div className="info-row">
            <span className="info-label">Name:</span>
            <span className="info-value">{user?.firstName} {user?.lastName}</span>
          </div>
          <div className="info-row">
            <span className="info-label">Email:</span>
            <span className="info-value">{user?.email}</span>
          </div>
          <div className="info-row">
            <span className="info-label">2FA Method:</span>
            <span className="info-value">
              {user?.isTwoFactorEnabled 
                ? `${getTwoFactorIcon(user.twoFactorMethod)} ${formatTwoFactorMethod(user.twoFactorMethod)}`
                : '❌ Not Enabled'}
            </span>
          </div>
          <div className="info-row">
            <span className="info-label">2FA Status:</span>
            <span className="info-value">
              <span className={`status-indicator ${user?.isTwoFactorEnabled ? 'enabled' : 'disabled'}`}>
                {user?.isTwoFactorEnabled ? '✅ Enabled' : '❌ Disabled'}
              </span>
            </span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
