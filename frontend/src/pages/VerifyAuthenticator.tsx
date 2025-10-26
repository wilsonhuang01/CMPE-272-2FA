import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';
import { User } from '../types';
import QRCode from 'react-qr-code';
import './VerifyAuthenticator.css';

// Simple logging utility for frontend
const logger = {
  info: (message: string, data?: any) => {
    console.log(`[VERIFY-AUTHENTICATOR-INFO] ${message}`, data || '');
  },
  warn: (message: string, data?: any) => {
    console.warn(`[VERIFY-AUTHENTICATOR-WARN] ${message}`, data || '');
  },
  error: (message: string, data?: any) => {
    console.error(`[VERIFY-AUTHENTICATOR-ERROR] ${message}`, data || '');
  },
  debug: (message: string, data?: any) => {
    console.debug(`[VERIFY-AUTHENTICATOR-DEBUG] ${message}`, data || '');
  }
};

const VerifyAuthenticator: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, updateUser } = useAuth();
  
  const [qrCode, setQrCode] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  // Get QR code from location state or fetch it
  useEffect(() => {
    logger.info('VerifyAuthenticator component mounted', { 
      userId: user?.id, 
      email: user?.email,
      hasLocationState: !!location.state,
      qrCodeFromState: !!location.state?.qrCode
    });

    if (location.state?.qrCode) {
      setQrCode(location.state.qrCode);
      logger.info('QR code received from location state');
    } else {
      logger.warn('No QR code in location state, redirecting to settings');
      navigate('/settings');
    }
  }, [location.state, navigate, user]);

  const handleVerificationSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    logger.info('Authenticator verification form submitted');
    setError('');
    setSuccess('');

    if (!verificationCode || verificationCode.length !== 6) {
      setError('Please enter a valid 6-digit code');
      return;
    }

    setLoading(true);
    try {
      const response = await authService.verifyAuthenticatorCode(user?.email || '', verificationCode);
      if (response.message) {
        logger.info('Authenticator verification successful');
        setSuccess('Authenticator app verified and enabled successfully!');
        
        // Fetch updated user profile to get the latest 2FA status
        try {
          logger.info('Fetching updated user profile after authenticator verification');
          // Add a small delay to ensure database is updated
          await new Promise(resolve => setTimeout(resolve, 500));
          const profileResponse = await authService.getProfile();
          logger.info('Profile response received', {
            hasId: !!profileResponse.id,
            hasEmail: !!profileResponse.email,
            twoFactorMethod: profileResponse.twoFactorMethod,
            isTwoFactorEnabled: profileResponse.isTwoFactorEnabled,
            message: profileResponse.message
          });
          
          if (profileResponse.id && profileResponse.email) {
            logger.info('Updating user context with latest profile data');
            const updatedUser: User = {
              id: profileResponse.id,
              email: profileResponse.email,
              firstName: profileResponse.firstName || '',
              lastName: profileResponse.lastName || '',
              twoFactorMethod: profileResponse.twoFactorMethod,
              isTwoFactorEnabled: profileResponse.isTwoFactorEnabled || false,
            };
            logger.info('Updated user data', {
              id: updatedUser.id,
              email: updatedUser.email,
              twoFactorMethod: updatedUser.twoFactorMethod,
              isTwoFactorEnabled: updatedUser.isTwoFactorEnabled
            });
            updateUser(updatedUser);
            logger.info('User context updated successfully');
            
            // Navigate back to settings after successful verification
            setTimeout(() => {
              navigate('/settings');
            }, 2000);
          } else {
            logger.warn('Profile response missing required fields', { profileResponse });
          }
        } catch (profileErr: any) {
          logger.error('Failed to fetch updated profile after verification', { 
            error: profileErr,
            message: profileErr.message,
            response: profileErr.response?.data
          });
        }
      }
    } catch (err: any) {
      logger.error('Authenticator verification failed', { 
        error: err.response?.data?.message || err.message 
      });
      setError(err.response?.data?.message || 'Failed to verify authenticator code');
    } finally {
      setLoading(false);
    }
  };

  const handleCancel = () => {
    logger.info('Verification cancelled, navigating back to settings');
    navigate('/settings');
  };

  const handleBackToSettings = () => {
    logger.info('Navigating back to settings');
    navigate('/settings');
  };

  return (
    <div className="verify-authenticator-container">
      <div className="verify-authenticator-nav">
        <h1>Verify Authenticator Setup</h1>
        <div className="nav-buttons">
          <button onClick={handleBackToSettings} className="nav-button">
            Back to Settings
          </button>
        </div>
      </div>

      <div className="verify-authenticator-content">
        <div className="verify-authenticator-card">
          {qrCode && (
            <div className="qr-container">
              <h3>Step 1: Scan QR Code</h3>
              <p>Scan this QR code with your authenticator app (Google Authenticator, Authy, etc.):</p>
              <div className="qr-code-wrapper">
                <QRCode value={qrCode} size={256} />
              </div>
            </div>
          )}

          <div className="verification-container">
            <h3>Step 2: Enter Verification Code</h3>
            <p>Enter the 6-digit code from your authenticator app:</p>
            <form onSubmit={handleVerificationSubmit}>
              <div className="form-group">
                <label>Verification Code</label>
                <input
                  type="text"
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value)}
                  placeholder="123456"
                  maxLength={6}
                  required
                  className="verification-input"
                />
              </div>
              
              {error && <div className="error-message">{error}</div>}
              {success && <div className="success-message">{success}</div>}
              
              <div className="button-group">
                <button type="submit" disabled={loading} className="submit-button">
                  {loading ? 'Verifying...' : 'Verify Code'}
                </button>
                <button type="button" onClick={handleCancel} className="cancel-button">
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default VerifyAuthenticator;
