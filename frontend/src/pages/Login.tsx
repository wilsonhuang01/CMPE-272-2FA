import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import './Login.css';

// Simple logging utility for frontend
const logger = {
  info: (message: string, data?: any) => {
    console.log(`[LOGIN-INFO] ${message}`, data || '');
  },
  warn: (message: string, data?: any) => {
    console.warn(`[LOGIN-WARN] ${message}`, data || '');
  },
  error: (message: string, data?: any) => {
    console.error(`[LOGIN-ERROR] ${message}`, data || '');
  },
  debug: (message: string, data?: any) => {
    console.debug(`[LOGIN-DEBUG] ${message}`, data || '');
  }
};

const Login: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    logger.info('Login form submitted', { email: formData.email });
    setError('');

    setLoading(true);
    try {
      // Step 1: Initiate login - this will validate credentials and send verification code
      const response = await authService.login(formData.email, formData.password);
      
      logger.info('Login response received', { 
        email: formData.email,
        response: response,
        twoFactorMethod: response.twoFactorMethod 
      });
      // Redirect to verification page with email and 2FA method
      navigate('/verify-login', { 
        state: { 
          email: formData.email,
          twoFactorMethod: response.twoFactorMethod 
        } 
      });
    } catch (err: any) {
      logger.error('Login initiation failed', { 
        email: formData.email, 
        error: err.response?.data?.message || err.message 
      });
      setError(err.response?.data?.message || 'Invalid email or password');
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    logger.debug('Form field changed', { field: e.target.name, hasValue: !!e.target.value });
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    });
  };

  return (
    <div className="login-container">
      <div className="login-card">
        <h2>Login</h2>
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

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading} className="submit-button">
            {loading ? 'Logging in...' : 'Login'}
          </button>
        </form>

        <p className="signup-link">
          Don't have an account? <a href="/signup">Sign up</a>
        </p>
      </div>
    </div>
  );
};

export default Login;
