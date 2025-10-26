import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';
import { TwoFactorMethod, User } from '../types';
import './Settings.css';

// Simple logging utility for frontend
const logger = {
  info: (message: string, data?: any) => {
    console.log(`[SETTINGS-INFO] ${message}`, data || '');
  },
  warn: (message: string, data?: any) => {
    console.warn(`[SETTINGS-WARN] ${message}`, data || '');
  },
  error: (message: string, data?: any) => {
    console.error(`[SETTINGS-ERROR] ${message}`, data || '');
  },
  debug: (message: string, data?: any) => {
    console.debug(`[SETTINGS-DEBUG] ${message}`, data || '');
  }
};

const Settings: React.FC = () => {
  const navigate = useNavigate();
  const { user, updateUser, logout } = useAuth();

  const [activeTab, setActiveTab] = useState<'password' | '2fa'>('password');

  // Password change form
  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmNewPassword: '',
  });

  // 2FA form
  const [twoFactorData, setTwoFactorData] = useState({
    password: '',
    newTwoFactorMethod: '',
    phoneNumber: '',
  });

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  React.useEffect(() => {
    logger.info('Settings component mounted', {
      userId: user?.id,
      email: user?.email,
      currentTwoFactorMethod: user?.twoFactorMethod
    });
  }, [user]);

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    logger.debug('Password form field changed', {
      field: e.target.name,
      hasValue: !!e.target.value
    });
    setPasswordData({
      ...passwordData,
      [e.target.name]: e.target.value,
    });
  };

  const handle2FAChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    logger.debug('2FA form field changed', {
      field: e.target.name,
      value: e.target.name === 'password' ? '[REDACTED]' : e.target.value
    });
    setTwoFactorData({
      ...twoFactorData,
      [e.target.name]: e.target.value,
    });
  };

  const handlePasswordSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    logger.info('Password change form submitted');
    setError('');
    setSuccess('');

    if (passwordData.newPassword !== passwordData.confirmNewPassword) {
      logger.warn('Password confirmation mismatch');
      setError('New passwords do not match');
      return;
    }

    if (passwordData.newPassword.length < 6) {
      logger.warn('Password too short', { length: passwordData.newPassword.length });
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);
    try {
      const response = await authService.changePassword(
        passwordData.currentPassword,
        passwordData.newPassword,
        passwordData.confirmNewPassword
      );
      if (response.message) {
        logger.info('Password changed successfully');
        setSuccess('Password changed successfully!');
        setPasswordData({
          currentPassword: '',
          newPassword: '',
          confirmNewPassword: '',
        });
      }
    } catch (err: any) {
      logger.error('Password change failed', { error: err.response?.data?.message || err.message });
      setError(err.response?.data?.message || 'Failed to change password');
    } finally {
      setLoading(false);
    }
  };

  const handle2FASubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    logger.info('2FA method change form submitted', {
      newMethod: twoFactorData.newTwoFactorMethod
    });
    setError('');
    setSuccess('');

    setLoading(true);
    try {
      const response = await authService.change2FA(
        twoFactorData.password,
        twoFactorData.newTwoFactorMethod,
        twoFactorData.phoneNumber || undefined
      );

      if (response.message) {
        if (twoFactorData.newTwoFactorMethod === TwoFactorMethod.AUTHENTICATOR_APP) {
          logger.info('Authenticator app selected, navigating to verification page');
          // Navigate to the new verification page with QR code
          navigate('/verify-authenticator', {
            state: { qrCode: response.qrCode || '' }
          });
        } else {
          logger.info('2FA method changed to email, fetching updated user profile');
          try {
            const userProfile = await authService.getProfile();
            if (userProfile.id && userProfile.email) {
              const updatedUser: User = {
                id: userProfile.id,
                email: userProfile.email,
                firstName: userProfile.firstName || '',
                lastName: userProfile.lastName || '',
                twoFactorMethod: userProfile.twoFactorMethod,
                isTwoFactorEnabled: userProfile.isTwoFactorEnabled || false,
              };
              logger.info('Updating user context with fresh profile data', {
                userId: updatedUser.id,
                email: updatedUser.email,
                twoFactorMethod: updatedUser.twoFactorMethod
              });
              updateUser(updatedUser);
            }
          } catch (profileError) {
            logger.warn('Failed to fetch updated user profile', { error: profileError });
            // Don't fail the whole operation if profile fetch fails
          }
          setSuccess('2FA method updated successfully!');
        }
        setTwoFactorData({
          password: '',
          newTwoFactorMethod: '',
          phoneNumber: '',
        });
      }
    } catch (err: any) {
      logger.error('2FA method change failed', {
        newMethod: twoFactorData.newTwoFactorMethod,
        error: err.response?.data?.message || err.message
      });
      setError(err.response?.data?.message || 'Failed to change 2FA method');
    } finally {
      setLoading(false);
    }
  };


  const handleLogout = async () => {
    logger.info('Logout initiated from settings');
    try {
      await authService.logout();
      logout();
      logger.info('Logout successful, navigating to login');
      navigate('/login');
    } catch (error) {
      logger.error('Logout failed', { error });
    }
  };

  const handleTabChange = (tab: 'password' | '2fa') => {
    logger.info('Settings tab changed', { tab });
    setActiveTab(tab);
  };

  const handleNavigateToDashboard = () => {
    logger.info('Navigating back to dashboard');
    navigate('/dashboard');
  };

  return (
    <div className="settings-container">
      <div className="settings-nav">
        <h1>Settings</h1>
        <div className="nav-buttons">
          <button onClick={handleNavigateToDashboard} className="nav-button">
            Dashboard
          </button>
          <button onClick={handleLogout} className="nav-button">
            Logout
          </button>
        </div>
      </div>

      <div className="settings-content">
        <div className="settings-tabs">
          <button
            className={`tab-button ${activeTab === 'password' ? 'active' : ''}`}
            onClick={() => handleTabChange('password')}
          >
            Change Password
          </button>
          <button
            className={`tab-button ${activeTab === '2fa' ? 'active' : ''}`}
            onClick={() => handleTabChange('2fa')}
          >
            Two-Factor Authentication
          </button>
        </div>

        <div className="settings-card">
          {activeTab === 'password' ? (
            <form onSubmit={handlePasswordSubmit}>
              <h3>Change Password</h3>

              <div className="form-group">
                <label>Current Password</label>
                <input
                  type="password"
                  name="currentPassword"
                  value={passwordData.currentPassword}
                  onChange={handlePasswordChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>New Password</label>
                <input
                  type="password"
                  name="newPassword"
                  value={passwordData.newPassword}
                  onChange={handlePasswordChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>Confirm New Password</label>
                <input
                  type="password"
                  name="confirmNewPassword"
                  value={passwordData.confirmNewPassword}
                  onChange={handlePasswordChange}
                  required
                />
              </div>

              {error && <div className="error-message">{error}</div>}
              {success && <div className="success-message">{success}</div>}

              <button type="submit" disabled={loading} className="submit-button">
                {loading ? 'Changing Password...' : 'Change Password'}
              </button>
            </form>
          ) : (
            <form onSubmit={handle2FASubmit}>
              <h3>Two-Factor Authentication</h3>
              <p>Current method: {user?.isTwoFactorEnabled ? user.twoFactorMethod : 'None'}</p>

              <div className="form-group">
                <label>Your Password</label>
                <input
                  type="password"
                  name="password"
                  value={twoFactorData.password}
                  onChange={handle2FAChange}
                  required
                />
              </div>

              <div className="form-group">
                <label>New 2FA Method</label>
                <select
                  name="newTwoFactorMethod"
                  value={twoFactorData.newTwoFactorMethod}
                  onChange={handle2FAChange}
                  required
                >
                  <option value="">Select method</option>
                  <option value={TwoFactorMethod.EMAIL}>Email</option>
                  <option value={TwoFactorMethod.AUTHENTICATOR_APP}>Authenticator App</option>
                </select>
              </div>

              {error && <div className="error-message">{error}</div>}
              {success && <div className="success-message">{success}</div>}


              <button type="submit" disabled={loading} className="submit-button">
                {loading ? 'Updating...' : 'Update 2FA Method'}
              </button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default Settings;
