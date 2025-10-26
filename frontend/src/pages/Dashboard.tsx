import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';
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

  React.useEffect(() => {
    logger.info('Dashboard component mounted', { 
      userId: user?.id, 
      email: user?.email,
      twoFactorEnabled: user?.isTwoFactorEnabled 
    });
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
        <h1>2FA Authentication System</h1>
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
                ? user.twoFactorMethod || 'None' 
                : 'Not Enabled'}
            </span>
          </div>
          <div className="info-row">
            <span className="info-label">2FA Status:</span>
            <span className="info-value">
              {user?.isTwoFactorEnabled ? '✅ Enabled' : '❌ Disabled'}
            </span>
          </div>
        </div>

        <div className="features-card">
          <h3>Available Features</h3>
          <div className="features-grid">
            <div className="feature-item">
              <h4>🔐 Security</h4>
              <p>Multi-factor authentication support</p>
            </div>
            <div className="feature-item">
              <h4>✉️ Email Verification</h4>
              <p>Secure email-based verification</p>
            </div>
            <div className="feature-item">
              <h4>🔑 Authenticator App</h4>
              <p>TOTP-based authentication</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
