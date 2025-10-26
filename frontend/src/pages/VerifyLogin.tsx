import React, { useState, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authService } from '../services/authService';
import { useAuth } from '../context/AuthContext';
import { TwoFactorMethod } from '../types';
import './VerifyLogin.css';

// Simple logging utility for frontend
const logger = {
  info: (message: string, data?: any) => {
    console.log(`[VERIFY-LOGIN-INFO] ${message}`, data || '');
  },
  warn: (message: string, data?: any) => {
    console.warn(`[VERIFY-LOGIN-WARN] ${message}`, data || '');
  },
  error: (message: string, data?: any) => {
    console.error(`[VERIFY-LOGIN-ERROR] ${message}`, data || '');
  },
  debug: (message: string, data?: any) => {
    console.debug(`[VERIFY-LOGIN-DEBUG] ${message}`, data || '');
  }
};

const VerifyLogin: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();
  
  // Get email and 2FA method from navigation state
  const email = location.state?.email || '';
  const twoFactorMethod = location.state?.twoFactorMethod || TwoFactorMethod.EMAIL;
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  // Generate dynamic instruction text based on 2FA method
  const getInstructionText = useCallback(() => {
    logger.debug('Generating instruction text', { 
      twoFactorMethod, 
      email,
      twoFactorMethodType: typeof twoFactorMethod 
    });
    
    switch (twoFactorMethod) {
      case TwoFactorMethod.EMAIL:
        return `We've sent a verification code to your email address at ${email}`;
      case TwoFactorMethod.AUTHENTICATOR_APP:
        return `Please enter the 6-digit code from your authenticator app for ${email}`;
      default:
        logger.warn('Unknown 2FA method, using default instruction', { twoFactorMethod });
        return `We've sent a verification code to ${email}`;
    }
  }, [twoFactorMethod, email]);

  // Redirect to login if no email provided
  React.useEffect(() => {
    if (!email) {
      logger.warn('No email provided, redirecting to login');
      navigate('/login');
    } else {
      logger.info('VerifyLogin component initialized', { 
        email, 
        twoFactorMethod,
        locationState: location.state
      });
    }
  }, [email, twoFactorMethod, navigate, location.state]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    logger.info('Verification code submitted', { email });
    setError('');

    if (!code || code.length !== 6) {
      setError('Please enter a valid 6-digit code');
      return;
    }

    setLoading(true);
    try {
      // Step 2: Verify login - this will verify the code and return JWT token
      const response = await authService.verifyLogin(email, code);
      
      if (response.token) {
        logger.info('Verification successful, logging in', { email });
        login(response);
        navigate('/dashboard');
      } else {
        logger.warn('Verification response missing token', { email, response });
        setError('Verification failed. Please try again.');
      }
    } catch (err: any) {
      logger.error('Verification failed', { 
        email, 
        error: err.response?.data?.message || err.message 
      });
      setError(err.response?.data?.message || 'Invalid verification code. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/\D/g, '').slice(0, 6); // Only allow digits, max 6
    logger.debug('Code field changed', { length: value.length });
    setCode(value);
  };

  const handleBackToLogin = () => {
    logger.info('Navigating back to login');
    navigate('/login');
  };

  return (
    <div className="verify-login-container">
      <div className="verify-login-card">
        <h2>Enter Verification Code</h2>
        <p className="instruction-text">
          {getInstructionText()}
        </p>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Verification Code</label>
            <input
              type="text"
              name="code"
              value={code}
              onChange={handleChange}
              placeholder="Enter 6-digit code"
              maxLength={6}
              required
              className="code-input"
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading || code.length !== 6} className="submit-button">
            {loading ? 'Verifying...' : 'Verify & Login'}
          </button>
        </form>

        <div className="footer-links">
          <button onClick={handleBackToLogin} className="link-button">
            Back to Login
          </button>
        </div>
      </div>
    </div>
  );
};

export default VerifyLogin;
