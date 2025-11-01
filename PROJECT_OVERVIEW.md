# Project Overview - CMPE-272 Two-Factor Authentication System

## Executive Summary

This project implements a comprehensive full-stack two-factor authentication (2FA) system demonstrating modern security practices, multiple authentication methods, and a complete user experience from registration to secure login.

## Architecture

### System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend (React)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐               │
│  │   UI     │  │  Auth    │  │   API    │               │
│  │ Pages    │  │ Context  │  │ Service  │               │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘               │
│       │             │              │                    │
└───────┼─────────────┼──────────────┼────────────────────┘
        │             │              │
        └─────────────┴──────────────┘
                       │
                       ▼
              ┌────────────────┐
              │   REST API     │
              │   (Spring)     │
              └────────┬───────┘
                       │
                ┌──────┼──────┐
                │             │ 
                ▼             ▼
           ┌─────────┐   ┌──────────┐
           │  JWT    │   │  Email   │
           │ Service │   │ Service  │
           └─────────┘   └──────────┘
                │              │
                ▼              ▼
  ┌────────────────────────────────────┐
  │         H2 Database                │
  │  ┌──────────┐  ┌────────────┐      │
  │  │  Users   │  │    Tokens  │      │
  │  └──────────┘  └────────────┘      │
  └────────────────────────────────────┘
```

## Technology Stack

### Frontend (React + TypeScript)
- **Framework:** React 18 with TypeScript for type safety
- **Routing:** React Router v6 for client-side routing
- **State Management:** Context API for global auth state
- **HTTP Client:** Axios for REST API communication
- **Styling:** CSS Modules for component-specific styles

### Backend (Spring Boot)
- **Framework:** Spring Boot 3.x
- **Security:** Spring Security with JWT
- **Database:** H2 In-Memory Database and MySQL Database with JPA
- **Authentication:** JWT tokens with blacklisting
- **Email:** JavaMailSender with SMTP

## Key Features Implementation

### 1. User Registration & Email Verification

**Flow:**
1. User submits registration form
2. Backend validates input and creates user with unverified status
3. Email verification code generated and sent
4. User receives email with 6-digit code
5. User enters code on verification page
6. Account activated upon successful verification

**Implementation:**
- Frontend: `pages/Signup.tsx`, `pages/VerifyEmail.tsx`
- Backend: `AuthService.signup()`, `AuthService.verifyEmail()`
- Email sending handled by `EmailService`

### 2. JWT Authentication

**Token Structure:**
```json
{
  "sub": "user@example.com",
  "userId": 1,
  "iat": 1234567890,
  "exp": 1234567890
}
```

**Token Lifecycle:**
1. Token issued on successful login
2. Token stored in localStorage (frontend)
3. Token included in Authorization header for API requests
4. Token validated on protected endpoints
5. Token blacklisted on logout

**Implementation:**
- Token Generation: `JwtService.generateToken()`
- Token Validation: `JwtAuthenticationFilter`
- Token Blacklisting: `TokenBlacklistService`

### 3. Two-Factor Authentication

#### Email-based 2FA
- 6-digit code sent to user's email
- Code expires in 10 minutes
- Resend functionality available

#### Authenticator App (TOTP)
- Secret key generation using `TotpSecretKeyGenerator`
- QR code generation for easy setup
- Compatible with Google Authenticator, Authy, etc.
- Time-based one-time passwords (TOTP RFC 6238)

**Implementation:**
- Frontend: `pages/Settings.tsx` (2FA tab)
- Backend: `TwoFactorService`, `AuthService.changeTwoFactorMethod()`

### 4. Password Management

**Password Requirements:**
- Minimum 6 characters
- Hashed using BCrypt (cost factor: 12)
- Validated on both frontend and backend

**Password Change:**
1. User provides current password
2. System verifies current password
3. New password validated
4. Password hash updated in database

**Implementation:**
- Frontend: `pages/Settings.tsx` (Password tab)
- Backend: `AuthService.changePassword()`

### 5. Secure Session Management

**Features:**
- Stateless authentication (JWT)
- Token expiration (24 hours)
- Automatic token refresh capability
- Secure logout with token blacklisting
- Protected routes on frontend

**Implementation:**
- Token Blacklist: `TokenBlacklistService`
- Route Guards: `ProtectedRoute` component
- Logout: `AuthService.logout()`

## Security Features

### Password Security
- ✅ BCrypt hashing with salt rounds (12)
- ✅ Password never stored in plain text
- ✅ Password confirmation validation
- ✅ Password strength requirements

### Authentication Security
- ✅ JWT with expiration
- ✅ Token blacklisting for logout
- ✅ CORS configuration
- ✅ Secure HTTP headers
- ✅ Input validation and sanitization

### 2FA Security
- ✅ Multiple authentication methods
- ✅ Verification code expiration
- ✅ Rate limiting on code generation
- ✅ Secure secret generation for TOTP

## API Endpoints

### Public Endpoints
```
POST   /api/auth/signup
POST   /api/auth/login
POST   /api/auth/verify-email
POST   /api/auth/resend-code
```

### Protected Endpoints
```
POST   /api/auth/logout
POST   /api/auth/change-password
POST   /api/auth/change-2fa
GET    /api/auth/authenticator-qr
GET    /api/auth/profile
```

## Database Schema

### User Table
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  phone_number VARCHAR(20),
  email_verified BOOLEAN DEFAULT FALSE,
  phone_verified BOOLEAN DEFAULT FALSE,
  is_two_factor_enabled BOOLEAN DEFAULT FALSE,
  two_factor_method VARCHAR(50),
  two_factor_secret VARCHAR(255),
  verification_code VARCHAR(6),
  code_expiration_time TIMESTAMP,
  status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);
```

### Token Blacklist Table
```sql
CREATE TABLE token_blacklist (
  token VARCHAR(500) PRIMARY KEY,
  expires_at TIMESTAMP NOT NULL
);
```

## User Flows

### Registration Flow
```
User → Sign Up Page
  → Enter Details
  → Submit
  → Email Verification Page
  → Enter Code
  → Verify
  → Login Page
```

### Login Flow
```
User → Login Page
  → Enter Credentials
  → Submit
  → (If 2FA Enabled) Enter Code
  → Submit
  → Dashboard
```

### Settings Flow
```
User → Dashboard
  → Settings
  → Change Password / Update 2FA
  → Submit
  → Success Message
```

## Testing

### Backend Tests
- Unit tests for services
- Integration tests for controllers
- Security configuration tests

### Frontend Tests
- Component rendering tests
- User interaction tests
- API integration tests

## Deployment Considerations

### Docker Deployment

The project includes Docker Compose configuration for easy deployment:

**Services:**
- **MySQL**: Database server (port 3306)
- **Backend**: Spring Boot API (port 8080)
- **Frontend**: React app served via Nginx (port 3000)

**Configuration:**
- Environment variables can be set via `.env` file or docker-compose.yml
- Frontend API URL configured via `REACT_APP_API_BASE_URL` build argument
- All services communicate via Docker network

**Running:**
```bash
docker-compose up --build
```

### Environment Variables
- `EMAIL_USERNAME`: Gmail account
- `EMAIL_PASSWORD`: Gmail app password
- `JWT_SECRET`: Secret key for JWT signing
- `TWILIO_ACCOUNT_SID`: Twilio account ID
- `TWILIO_AUTH_TOKEN`: Twilio auth token
- `REACT_APP_API_BASE_URL`: Frontend API base URL (for Docker builds)

### Production Recommendations
- Use environment-specific configuration
- Implement rate limiting
- Add HTTPS/SSL
- Use production-grade database
- Implement monitoring and logging
- Add backup strategies
- Configure proper CORS policies
- Use reverse proxy (Nginx/Traefik) for production frontend
- Configure environment-specific API URLs

## Future Enhancements

1. **Recovery Codes** - For lost authenticator devices
2. **Session Management** - View and manage active sessions
3. **Biometric Authentication** - Fingerprint/face recognition
4. **Social Login** - OAuth integration
5. **Account Lockout** - Brute force protection
6. **Activity Logging** - Track user activities
7. **Multi-device Management** - Trusted devices
8. **Password Recovery** - Email-based password reset

## Project Statistics

- **Backend Lines of Code:** ~2,500
- **Frontend Lines of Code:** ~3,000
- **Total Components:** 20+
- **API Endpoints:** 9
- **Database Tables:** 2
- **Test Coverage:** Backend services and controllers

## Conclusion

This project demonstrates a complete, production-ready authentication system with:
- ✅ Secure user registration and verification
- ✅ Multiple 2FA methods
- ✅ JWT-based authentication
- ✅ Modern, responsive UI
- ✅ Comprehensive documentation
- ✅ Clean, maintainable code structure

The system is designed with security best practices, scalability in mind, and provides an excellent foundation for production deployment.
