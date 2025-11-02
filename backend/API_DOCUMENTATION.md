# Authentication API Documentation

## Overview

This Spring Boot REST API provides a comprehensive authentication system with support for:
- User signup with email verification
- Two-step login with optional two-factor authentication (2FA)
- Multiple 2FA methods: Email verification codes and Authenticator app (TOTP)
- JWT-based stateless authentication
- Password management
- Secure logout with token blacklisting
- User profile management

## Base URL

**Development:**
```
http://localhost:8080/api/auth
```

**Production:**
```
https://your-api-domain.com/api/auth
```

**Health Check:**
```
GET /actuator/health
```

## Authentication

### JWT Token Usage

Protected endpoints require a JWT token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

Tokens are issued after successful login and expire after 24 hours by default.

## API Endpoints

### Public Endpoints (No Authentication Required)

---

### 1. User Signup

**POST** `/api/auth/signup`

Creates a new user account and sends an email verification code.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "twoFactorMethod": "EMAIL"
}
```

**Field Validation:**
- `email`: Required, must be valid email format, unique
- `password`: Required, minimum 6 characters
- `confirmPassword`: Required, must match password
- `firstName`: Optional
- `lastName`: Optional
- `phoneNumber`: Optional
- `twoFactorMethod`: Optional, values: `EMAIL`, `AUTHENTICATOR_APP`

**Response (200 OK):**
```json
{
  "message": "User created successfully. Please check your email for verification code."
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Email already exists"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "firstName": "Test",
    "lastName": "User",
    "twoFactorMethod": "EMAIL"
  }'
```

---

### 2. Login (Initiate)

**POST** `/api/auth/login`

Initiates the login process. If 2FA is enabled, returns `requiresTwoFactor: true` and sends verification code.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK - No 2FA):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "twoFactorMethod": null,
  "isTwoFactorEnabled": false,
  "requiresTwoFactor": false
}
```

**Response (200 OK - 2FA Required):**
```json
{
  "message": "Two-factor authentication required",
  "requiresTwoFactor": true,
  "twoFactorMethod": "EMAIL",
  "email": "user@example.com"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Invalid email or password"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'
```

---

### 3. Login Verification (Complete Login with 2FA)

**POST** `/api/auth/login-verify`

Completes the login process by verifying the 2FA code sent during login initiation.

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "twoFactorMethod": "EMAIL",
  "isTwoFactorEnabled": true,
  "requiresTwoFactor": false
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Invalid or expired verification code"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/login-verify \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "123456"
  }'
```

---

### 4. Email Verification

**POST** `/api/auth/verify-email`

Verifies the email address using the verification code sent during signup.

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response (200 OK):**
```json
{
  "message": "Email verified successfully"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Invalid or expired verification code"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "code": "123456"
  }'
```

---

### 5. Resend Verification Code

**POST** `/api/auth/resend-code`

Resends a verification code for email or 2FA verification.

**Query Parameters:**
- `email` (required): User's email address
- `type` (required): Type of code to resend - `"email"` or `"2fa"`

**Example Request:**
```
POST /api/auth/resend-code?email=user@example.com&type=email
```

**Response (200 OK):**
```json
{
  "message": "Email verification code sent"
}
```

**cURL Example:**
```bash
curl -X POST "http://localhost:8080/api/auth/resend-code?email=test@example.com&type=email"
```

---

### 6. Health Check

**GET** `/actuator/health`

Returns the health status of the application. Useful for load balancer health checks.

**Response (200 OK):**
```json
{
  "status": "UP"
}
```

**cURL Example:**
```bash
curl http://localhost:8080/actuator/health
```

---

### Protected Endpoints (JWT Authentication Required)

All endpoints below require a valid JWT token in the Authorization header:
```
Authorization: Bearer <token>
```

---

### 7. Get User Profile

**GET** `/api/auth/profile`

Retrieves the current authenticated user's profile information.

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "twoFactorMethod": "EMAIL",
  "isTwoFactorEnabled": true,
  "requiresTwoFactor": true
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Unauthorized"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

### 8. Change Password

**POST** `/api/auth/change-password`

Changes the authenticated user's password.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "currentPassword": "oldpassword123",
  "newPassword": "newpassword123",
  "confirmNewPassword": "newpassword123"
}
```

**Response (200 OK):**
```json
{
  "message": "Password changed successfully"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Current password is incorrect"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/change-password \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "currentPassword": "oldpassword123",
    "newPassword": "newpassword123",
    "confirmNewPassword": "newpassword123"
  }'
```

---

### 9. Change 2FA Method

**POST** `/api/auth/change-2fa`

Changes the user's two-factor authentication method.

**Headers:**
```
Authorization: Bearer <token>
```

**Request Body:**
```json
{
  "password": "currentpassword123",
  "newTwoFactorMethod": "AUTHENTICATOR_APP"
}
```

**Valid Values for `newTwoFactorMethod`:**
- `EMAIL`: Email verification codes
- `AUTHENTICATOR_APP`: TOTP-based authenticator app (Google Authenticator, Authy, etc.)

**Response (200 OK):**
```json
{
  "message": "2FA method change successful to: AUTHENTICATOR_APP"
}
```

**Response (when switching to AUTHENTICATOR_APP):**
```json
{
  "message": "Changing 2FA method to authenticator app. Verification required.",
  "qrCode": "otpauth://totp/Authentication%20Service:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=Authentication%20Service"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Invalid password"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/change-2fa \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "password": "currentpassword123",
    "newTwoFactorMethod": "EMAIL"
  }'
```

---

### 10. Get Authenticator QR Code

**GET** `/api/auth/authenticator-qr`

Returns the QR code URL (otpauth://) for setting up authenticator app 2FA. User must have AUTHENTICATOR_APP as their 2FA method.

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "message": "otpauth://totp/Authentication%20Service:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=Authentication%20Service",
  "qrCode": "otpauth://totp/Authentication%20Service:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=Authentication%20Service"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Authenticator app 2FA is not enabled for this user"
}
```

**cURL Example:**
```bash
curl -X GET http://localhost:8080/api/auth/authenticator-qr \
  -H "Authorization: Bearer <token>"
```

**Note:** Use the `qrCode` field value to generate a QR code image for the user to scan with their authenticator app.

---

### 11. Verify Authenticator Code

**POST** `/api/auth/verify-authenticator`

Verifies an authenticator app TOTP code. Used during 2FA method change or login verification.

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response (200 OK):**
```json
{
  "message": "Authenticator code verified successfully"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Invalid authenticator code"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/verify-authenticator \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "code": "123456"
  }'
```

---

### 12. Logout

**POST** `/api/auth/logout`

Logs out the user by blacklisting the current JWT token. Token cannot be used again after logout.

**Headers:**
```
Authorization: Bearer <token>
```

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

**cURL Example:**
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer <token>"
```

**Note:** After logout, the token becomes invalid and cannot be used for subsequent requests. User must login again to get a new token.

---

## 2FA Methods

The API supports two two-factor authentication methods:

### 1. Email Verification Codes

- **How it works:** User receives a 6-digit code via email
- **Code expiration:** 10 minutes
- **Use cases:**
  - Email verification during signup
  - 2FA during login (if EMAIL is the chosen 2FA method)

**Flow:**
1. User requests login or verification
2. System generates 6-digit code
3. Code sent to user's email
4. User submits code within 10 minutes
5. System verifies code

### 2. Authenticator App (TOTP)

- **How it works:** Time-based One-Time Password (TOTP) using authenticator apps
- **Compatible apps:** Google Authenticator, Microsoft Authenticator, Authy, etc.
- **Setup:**
  1. User changes 2FA method to `AUTHENTICATOR_APP`
  2. System generates secret key and QR code
  3. User scans QR code with authenticator app
  4. User verifies with a code from the app
  5. 2FA method is enabled

**Flow:**
1. User changes 2FA to `AUTHENTICATOR_APP`
2. Get QR code via `/authenticator-qr` endpoint
3. Scan QR code with authenticator app
4. Verify setup with `/verify-authenticator` endpoint
5. Future logins require code from authenticator app

---

## Authentication Flows

### Complete Registration Flow

```mermaid
sequenceDiagram
    participant Client
    participant API
    participant Email Service
    participant Database

    Client->>API: POST /signup
    API->>Database: Create user (unverified)
    API->>Email Service: Send verification code
    Email Service-->>Client: Email with code
    API-->>Client: 200 OK (check email)
    Client->>API: POST /verify-email
    API->>Database: Verify email, activate account
    API-->>Client: 200 OK (email verified)
```

**Steps:**
1. `POST /api/auth/signup` - Create account, receive verification code via email
2. `POST /api/auth/verify-email` - Verify email with code
3. `POST /api/auth/login` - Login with credentials
4. (If 2FA enabled) `POST /api/auth/login-verify` - Complete login with 2FA code

### Login Flow (Without 2FA)

```
1. POST /api/auth/login
   → Request: { email, password }
   → Response: { token, user info }

2. Use token in Authorization header for protected endpoints
```

### Login Flow (With 2FA - Email)

```
1. POST /api/auth/login
   → Request: { email, password }
   → Response: { requiresTwoFactor: true, twoFactorMethod: "EMAIL" }
   → Email sent with code

2. POST /api/auth/login-verify
   → Request: { email, code }
   → Response: { token, user info }

3. Use token in Authorization header for protected endpoints
```

### Login Flow (With 2FA - Authenticator App)

```
1. POST /api/auth/login
   → Request: { email, password }
   → Response: { requiresTwoFactor: true, twoFactorMethod: "AUTHENTICATOR_APP" }

2. User opens authenticator app, gets code

3. POST /api/auth/login-verify
   → Request: { email, code }
   → Response: { token, user info }

4. Use token in Authorization header for protected endpoints
```

### Setup Authenticator App 2FA

```
1. POST /api/auth/change-2fa
   → Request: { password, newTwoFactorMethod: "AUTHENTICATOR_APP" }
   → Response: { qrCode: "otpauth://..." }

2. GET /api/auth/authenticator-qr (optional, if QR code not in step 1)
   → Response: { qrCode: "otpauth://..." }

3. User scans QR code with authenticator app

4. POST /api/auth/verify-authenticator
   → Request: { email, code }
   → Response: { message: "Verified successfully" }
```

---

## Response Format

### Success Response

All successful responses return HTTP `200 OK` with a JSON body. Response structure varies by endpoint:

**Authentication Response (with token):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "twoFactorMethod": "EMAIL",
  "isTwoFactorEnabled": true,
  "requiresTwoFactor": false,
  "message": "Optional message",
  "qrCode": "Optional QR code URL"
}
```

**Simple Success Response:**
```json
{
  "message": "Operation completed successfully"
}
```

### Error Response

All errors return appropriate HTTP status codes with error details:

**400 Bad Request:**
```json
{
  "message": "Error description"
}
```

**401 Unauthorized:**
```json
{
  "message": "Unauthorized"
}
```

**500 Internal Server Error:**
```json
{
  "message": "Internal server error"
}
```

---

## HTTP Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful request |
| 400 | Bad Request | Invalid input, validation error, business logic error |
| 401 | Unauthorized | Missing or invalid JWT token, authentication failed |
| 403 | Forbidden | Valid token but insufficient permissions |
| 500 | Internal Server Error | Server-side error |

---

## Security Features

### Password Security
- **Hashing:** BCrypt with cost factor 10
- **Minimum Length:** 6 characters
- **Validation:** Password confirmation must match
- **Current Password:** Required for password changes

### JWT Tokens
- **Algorithm:** HS256 (HMAC with SHA-256)
- **Expiration:** 24 hours (86400000 ms) - configurable
- **Claims:** User ID, email, expiration time
- **Stateless:** No server-side session storage
- **Blacklisting:** Tokens can be invalidated on logout

### Email Verification
- **Required:** Email must be verified before account activation
- **Code Format:** 6-digit numeric code
- **Expiration:** 10 minutes
- **Resend:** Available via `/resend-code` endpoint

### CORS Protection
- **Configurable:** Allowed origins set via `ALLOWED_ORIGINS` environment variable
- **Multiple Origins:** Supports comma-separated list
- **Credentials:** Allowed for authenticated requests

### Token Blacklisting
- **Logout:** Tokens are blacklisted when user logs out
- **Security:** Prevents token reuse after logout
- **Storage:** In-memory (consider Redis for distributed systems)

---

## Configuration

### Environment Variables

```bash
# Database Configuration
DB_HOST=localhost
DB_PORT=3306
DB_NAME=cmpe272
DB_USERNAME=cmpe272_user
DB_PASSWORD=your_password

# JWT Configuration
JWT_SECRET=your-secure-random-secret-key-minimum-256-bits
JWT_EXPIRATION=86400000  # 24 hours in milliseconds

# Email Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# CORS Configuration
ALLOWED_ORIGINS=http://localhost:3000,https://yourdomain.com

# Application Profile
SPRING_PROFILES_ACTIVE=prod  # or dev
```

### Database Configuration

**Development (H2):**
- **URL:** `jdbc:h2:mem:testdb`
- **Console:** `http://localhost:8080/h2-console`
- **Username:** `sa`
- **Password:** `password`
- **Auto-schema:** Created automatically

**Production (MySQL):**
- **URL:** `jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&requireSSL=false&serverTimezone=UTC`
- **SSL:** Enabled for secure connections
- **Connection Pool:** HikariCP with optimized settings
- **Schema:** Auto-created via Hibernate `ddl-auto=update`

---

## Testing

### Using cURL

See individual endpoint sections above for cURL examples.

### Using Postman

1. Import the endpoints as a collection
2. Set base URL: `http://localhost:8080/api/auth`
3. For protected endpoints:
   - Go to Authorization tab
   - Select "Bearer Token"
   - Paste JWT token from login response

### Example Test Flow

```bash
# 1. Signup
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123","confirmPassword":"password123"}'

# 2. Verify Email (check email for code, e.g., "123456")
curl -X POST http://localhost:8080/api/auth/verify-email \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","code":"123456"}'

# 3. Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# 4. Use token from step 3 for protected endpoints
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 5. Get Profile
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer $TOKEN"

# 6. Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

---

## Error Handling

### Common Error Messages

| Error Message | Cause | Solution |
|---------------|-------|----------|
| "Email already exists" | Email is already registered | Use different email or login |
| "Invalid email or password" | Wrong credentials | Check email and password |
| "Invalid or expired verification code" | Code wrong or expired | Request new code via `/resend-code` |
| "Unauthorized" | Missing or invalid JWT token | Login to get new token |
| "Password must be at least 6 characters" | Password too short | Use longer password |
| "Passwords do not match" | Password confirmation mismatch | Ensure passwords match |
| "Current password is incorrect" | Wrong current password | Verify current password |

### Validation Errors

The API uses Jakarta Bean Validation. Invalid input returns `400 Bad Request` with specific error messages:

```json
{
  "message": "Email should be valid"
}
```

---

## Rate Limiting

Currently, rate limiting is not implemented. For production deployments, consider:
- Implementing rate limiting for authentication endpoints
- Using Spring Security rate limiting features
- Adding AWS WAF rules for ALB protection

---

## Additional Resources

- **[README.md](./README.md)** - Backend overview and setup guide
- **[QUICK_START.md](./QUICK_START.md)** - Quick start commands
- **[PRODUCTION_SETUP.md](./PRODUCTION_SETUP.md)** - Production deployment guide

---

**Last Updated:** November 2025
