import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { authService } from '../services/authService';
import './VerifyEmail.css';

const VerifyEmail: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const email = location.state?.email || '';
  
  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    setLoading(true);
    try {
      const response = await authService.verifyEmail({ email, code });
      if (response.message) {
        navigate('/login');
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Verification failed');
    } finally {
      setLoading(false);
    }
  };

  const handleResendCode = async () => {
    try {
      await authService.resendCode(email, 'email');
    } catch (err: any) {
      setError('Failed to resend code');
    }
  };

  return (
    <div className="verify-container">
      <div className="verify-card">
        <h2>Verify Your Email</h2>
        <p>Enter the verification code sent to {email}</p>
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Verification Code</label>
            <input
              type="text"
              value={code}
              onChange={(e) => setCode(e.target.value)}
              placeholder="Enter 6-digit code"
              required
              maxLength={6}
            />
          </div>

          {error && <div className="error-message">{error}</div>}

          <button type="submit" disabled={loading} className="submit-button">
            {loading ? 'Verifying...' : 'Verify Email'}
          </button>
        </form>

        <button onClick={handleResendCode} className="resend-button">
          Resend Code
        </button>
      </div>
    </div>
  );
};

export default VerifyEmail;
