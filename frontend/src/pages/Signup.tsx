import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { SignupData } from '../types';
import './Signup.css';

// Simple logging utility for frontend
const logger = {
  info: (message: string, data?: any) => {
    console.log(`[SIGNUP-INFO] ${message}`, data || '');
  },
  warn: (message: string, data?: any) => {
    console.warn(`[SIGNUP-WARN] ${message}`, data || '');
  },
  error: (message: string, data?: any) => {
    console.error(`[SIGNUP-ERROR] ${message}`, data || '');
  },
  debug: (message: string, data?: any) => {
    console.debug(`[SIGNUP-DEBUG] ${message}`, data || '');
  }
};

const Signup: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<SignupData>({
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    twoFactorMethod: undefined,
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    logger.info('Signup form submitted', { 
      email: formData.email, 
      twoFactorMethod: formData.twoFactorMethod 
    });
    setError('');

    if (formData.password !== formData.confirmPassword) {
      logger.warn('Password confirmation mismatch');
      setError('Passwords do not match');
      return;
    }

    if (formData.password.length < 6) {
      logger.warn('Password too short', { length: formData.password.length });
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);
    try {
      const response = await authService.signup(formData);
      if (response.message) {
        logger.info('Signup successful, navigating to email verification', { 
          email: formData.email,
          message: response.message 
        });
        navigate('/verify-email', { state: { email: formData.email } });
      }
    } catch (err: any) {
      logger.error('Signup failed', { 
        email: formData.email, 
        error: err.response?.data?.message || err.message 
      });
      setError(err.response?.data?.message || 'Signup failed');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    logger.debug('Form field changed', { 
      field: e.target.name, 
      hasValue: !!e.target.value,
      value: e.target.name === 'password' || e.target.name === 'confirmPassword' ? '[REDACTED]' : e.target.value
    });
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  return (
    <div className="signup-container">
      <div className="signup-card">
        <h2>Create Account</h2>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Email</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>First Name</label>
              <input
                type="text"
                name="firstName"
                value={formData.firstName}
                onChange={handleChange}
                required
              />
            </div>
            <div className="form-group">
              <label>Last Name</label>
              <input
                type="text"
                name="lastName"
                value={formData.lastName}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group">
            <label>Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
            />
          </div>

          <div className="form-group">
            <label>Confirm Password</label>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              required
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading} className="submit-button">
            {loading ? 'Creating Account...' : 'Sign Up'}
          </button>
        </form>

        <p className="login-link">
          Already have an account? <a href="/login">Login</a>
        </p>
      </div>
    </div>
  );
};

export default Signup;
