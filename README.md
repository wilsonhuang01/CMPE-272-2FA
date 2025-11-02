# CMPE-272 Two-Factor Authentication System

A comprehensive full-stack authentication system with multiple 2FA methods, built with Spring Boot and React.

## Overview

This project demonstrates a complete authentication system featuring:

- User registration with email verification
- JWT-based authentication
- Multiple two-factor authentication methods:
  - Email verification
  - Authenticator app (TOTP)
- Password management
- Secure session handling
- Token blacklisting for logout

## Project Structure

```
CMPE-272-2FA/
├── backend/              # Spring Boot backend
│   ├── src/
│   │   └── main/java/com/wilson/cmpe272/
│   │       ├── controller/   # REST endpoints
│   │       ├── service/      # Business logic
│   │       ├── entity/       # JPA entities
│   │       ├── repository/   # Data access
│   │       └── config/       # Security configuration
│   ├── Dockerfile
│   └── pom.xml
├── frontend/             # React TypeScript frontend
│   ├── src/
│   │   ├── pages/       # Page components
│   │   ├── services/    # API services
│   │   ├── context/     # State management
│   │   └── types/       # TypeScript types
│   ├── Dockerfile
│   ├── nginx.conf
│   └── package.json
├── docker-compose.yml    # Docker Compose configuration
└── README.md
```

## Features

### Backend Features

- ✅ User Registration & Email Verification
- ✅ JWT Authentication with Stateless Sessions
- ✅ BCrypt Password Hashing
- ✅ Email-based 2FA
- ✅ Authenticator App 2FA (TOTP)
- ✅ Password Change
- ✅ Token Blacklisting (Secure Logout)
- ✅ CORS Configuration
- ✅ Comprehensive Error Handling

### Frontend Features

- ✅ Modern React UI with TypeScript
- ✅ User Registration & Login
- ✅ Email Verification Flow
- ✅ Password Management
- ✅ 2FA Configuration
- ✅ QR Code Display for Authenticator Setup
- ✅ Protected Routes
- ✅ Responsive Design
- ✅ Token Management

## Tech Stack

### Backend
- **Java 17**
- **Spring Boot 3.x**
- **Spring Security**
- **JWT (Java JWT)**
- **H2 Database**
- **JPA/Hibernate**
- **JavaMailSender** (for emails)

### Frontend
- **React 18**
- **TypeScript**
- **React Router**
- **Axios**
- **Context API**

## Getting Started

### Prerequisites

**For Local Development:**
- Java 17 or higher
- Maven 3.x
- Node.js 14+
- npm or yarn
- Gmail account (for email)

**For Docker Deployment (Recommended):**
- Docker Desktop or Docker Engine
- Docker Compose
- Gmail account (for email)

### Backend Setup

1. **Navigate to backend directory:**
```bash
cd backend
```

2. **Configure email settings** in `src/main/resources/application.properties`:
```properties
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

3. **Run the application:**
```bash
mvn spring-boot:run
```

Or using the Makefile:
```bash
make run
```

The backend will start on `http://localhost:8080`

### Frontend Setup

1. **Navigate to frontend directory:**
```bash
cd frontend
```

2. **Install dependencies:**
```bash
npm install
```

3. **Start the development server:**
```bash
npm start
```

The frontend will start on `http://localhost:3000`

### Docker Setup (Recommended)

Run the entire stack (MySQL, Backend, Frontend) with Docker Compose:

1. **From the root directory**, create a `.env` file (optional):
```bash
# Copy from backend/env.example if needed
cp backend/env.example .env
```

2. **Start all services:**
```bash
docker-compose up --build
```

3. **Access the application:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- MySQL: localhost:3306

4. **Stop services:**
```bash
docker-compose down
```

**Note:** The frontend API URL is configured via the `REACT_APP_API_BASE_URL` environment variable during build. By default, it points to `http://localhost:8080/api/auth` for local Docker Compose setups.

## Usage Guide

### 1. User Registration

1. Open the frontend at `http://localhost:3000`
2. Click "Sign up" or navigate to `/signup`
3. Fill in your details:
   - Email address
   - First and Last name
   - Password (minimum 6 characters)
   - Confirm password
   - Phone number (optional)
   - Two-factor method (optional)
4. Click "Sign Up"
5. Check your email for the verification code
6. Enter the code to verify your email

### 2. Login

1. Navigate to `/login`
2. Enter your email and password
3. If 2FA is enabled, enter the verification code
4. You'll be redirected to the dashboard

### 3. Configure 2FA

1. Login to your account
2. Go to Settings (from dashboard)
3. Click on "Two-Factor Authentication" tab
4. Enter your password
5. Select a 2FA method:
   - **Email (Default)**: Verification code sent via email
   - **Authenticator App**: Scan QR code with Google Authenticator, Authy, etc.
6. Click "Update 2FA Method"

### 4. Change Password

1. Go to Settings
2. Enter your current password
3. Enter and confirm your new password
4. Click "Change Password"

### 5. Logout

1. Click "Logout" from the dashboard or settings page
2. You'll be redirected to the login page

## API Documentation

### Authentication Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/api/auth/signup` | User registration | No |
| POST | `/api/auth/login` | User login | No |
| POST | `/api/auth/verify-email` | Email verification | No |
| POST | `/api/auth/change-password` | Change password | Yes |
| POST | `/api/auth/change-2fa` | Update 2FA method | Yes |
| GET | `/api/auth/authenticator-qr` | Get QR code | Yes |
| POST | `/api/auth/logout` | Logout | Yes |
| GET | `/api/auth/profile` | Get user profile | Yes |
| POST | `/api/auth/resend-code` | Resend verification code | No |

For detailed API documentation, see [backend/API_DOCUMENTATION.md](backend/API_DOCUMENTATION.md)

## Testing

### Backend Tests

Run tests using:
```bash
cd backend
mvn test
```

Or using the Makefile:
```bash
make test
```

### Frontend Tests

```bash
cd frontend
npm test
```

## Database

### Development Mode (H2)
The application uses H2 in-memory database for development when using the `dev` profile.

**H2 Console:** http://localhost:8080/h2-console

**Connection Details:**
- URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

### Production Mode (MySQL)
When using Docker Compose or production profile, the application uses MySQL 8.0.

**MySQL Connection (via Docker):**
- Host: `localhost` (from host machine) or `mysql` (from within Docker network)
- Port: `3306`
- Database: `cmpe272` (configurable via `DB_NAME`)
- Username: `cmpe272_user` (configurable via `DB_USERNAME`)
- Password: Set via `DB_PASSWORD` environment variable

## Security Features

- ✅ JWT-based stateless authentication
- ✅ BCrypt password hashing
- ✅ Token blacklisting for secure logout
- ✅ CORS protection
- ✅ Email verification
- ✅ Multiple 2FA methods
- ✅ Session management
- ✅ Password strength requirements

## Documentation

### Local Development
- [Backend README](backend/README.md)
- [Backend API Documentation](backend/API_DOCUMENTATION.md)
- [JWT Logout Implementation](backend/JWT_LOGOUT_IMPLEMENTATION.md)
- [Frontend README](frontend/README.md)

### AWS Deployment
- [AWS Deployment Guide](AWS_DEPLOYMENT_GUIDE.md)

## Troubleshooting

### Backend Issues

**Email not sending:**
- Check Gmail app password is correctly configured
- Verify SMTP settings in `application.properties`

**Database connection error:**
- H2 starts automatically with the application
- Check if port 8080 is available

### Frontend Issues

**Cannot connect to backend:**
- Ensure backend is running on port 8080
- Check `REACT_APP_API_BASE_URL` environment variable (for Docker) or `API_BASE_URL` in `frontend/src/services/api.ts` (for local development)
- Verify CORS is configured on backend

**Login token issues:**
- Clear browser localStorage
- Check browser console for errors

**Docker build issues:**
- Ensure package.json and package-lock.json are in sync
- If npm ci fails, try rebuilding: `docker-compose build --no-cache frontend`