import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/auth';

// Simple logging utility for frontend
const logger = {
  info: (message: string, data?: any) => {
    console.log(`[API-INFO] ${message}`, data || '');
  },
  warn: (message: string, data?: any) => {
    console.warn(`[API-WARN] ${message}`, data || '');
  },
  error: (message: string, data?: any) => {
    console.error(`[API-ERROR] ${message}`, data || '');
  },
  debug: (message: string, data?: any) => {
    console.debug(`[API-DEBUG] ${message}`, data || '');
  }
};

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Add token to requests
api.interceptors.request.use((config) => {
  logger.debug('Making API request', { 
    method: config.method?.toUpperCase(), 
    url: config.url,
    baseURL: config.baseURL 
  });
  
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
    logger.debug('Authorization token added to request');
  } else {
    logger.debug('No authorization token found');
  }
  return config;
});

// Handle responses
api.interceptors.response.use(
  (response) => {
    logger.debug('API response received', { 
      status: response.status, 
      url: response.config.url 
    });
    return response;
  },
  (error) => {
    logger.error('API request failed', { 
      status: error.response?.status,
      message: error.message,
      url: error.config?.url
    });
    
    if (error.response?.status === 401) {
      logger.warn('Unauthorized access detected, redirecting to login');
      // Handle unauthorized access
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
