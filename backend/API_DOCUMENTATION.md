# Authentication API Documentation

## Overview
This Spring Boot application provides a comprehensive authentication system with support for:
- User signup with email verification
- Login with optional two-factor authentication (2FA)
- Password change functionality
- 2FA method configuration (Email, SMS, Authenticator App)
- JWT-based authentication

## Base URL
```
http://localhost:8080/api/auth
```

## Authentication Endpoints

### 1. User Signup
**POST** `/signup`

Creates a new user account with optional 2FA setup.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "confirmPassword": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "phoneNumber": "+1234567890",
  "twoFactorMethod": "EMAIL" // Optional: EMAIL, SMS, AUTHENTICATOR_APP
}
```

**Response:**
```json
{
  "message": "User created successfully. Please check your email for verification code."
}
```

### 2. Login
**POST** `/login`

Authenticates a user and returns a JWT token.

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "twoFactorCode": "123456" // Optional, required if 2FA is enabled
}
```

**Response (without 2FA):**
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

**Response (with 2FA required):**
```json
{
  "message": "Two-factor authentication required",
  "requiresTwoFactor": true,
  "twoFactorMethod": "EMAIL"
}
```

### 3. Email Verification
**POST** `/verify-email`

Verifies the email address using the verification code sent during signup.

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456"
}
```

**Response:**
```json
{
  "message": "Email verified successfully"
}
```

### 4. Phone Verification
**POST** `/verify-phone`

Verifies the phone number using the verification code sent via SMS.

**Request Body:**
```json
{
  "email": "user@example.com",
  "code": "123456",
  "phoneNumber": "+1234567890"
}
```

**Response:**
```json
{
  "message": "Phone verified successfully"
}
```

### 5. Change Password
**POST** `/change-password`
**Authorization Required:** Bearer Token

Changes the user's password.

**Request Body:**
```json
{
  "currentPassword": "oldpassword123",
  "newPassword": "newpassword123",
  "confirmNewPassword": "newpassword123"
}
```

**Response:**
```json
{
  "message": "Password changed successfully"
}
```

### 6. Change 2FA Method
**POST** `/change-2fa`
**Authorization Required:** Bearer Token

Changes the user's two-factor authentication method.

**Request Body:**
```json
{
  "password": "currentpassword123",
  "newTwoFactorMethod": "SMS",
  "phoneNumber": "+1234567890" // Required if switching to SMS
}
```

**Response:**
```json
{
  "message": "Two-factor authentication method changed. Please verify your new method."
}
```

### 7. Get Authenticator App QR Code
**GET** `/authenticator-qr`
**Authorization Required:** Bearer Token

Returns the QR code URL for setting up authenticator app 2FA.

**Response:**
```json
{
  "message": "otpauth://totp/Authentication%20Service:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=Authentication%20Service"
}
```

### 8. Resend Verification Code
**POST** `/resend-code`

Resends verification codes for email or phone verification.

**Query Parameters:**
- `email`: User's email address
- `type`: "email" or "phone"

**Example:**
```
POST /api/auth/resend-code?email=user@example.com&type=email
```

**Response:**
```json
{
  "message": "Email verification code sent"
}
```

## 2FA Methods

### 1. Email Verification
- Sends a 6-digit code to the user's email
- Code expires in 10 minutes
- Used for both email verification and 2FA

### 2. SMS Verification
- Sends a 6-digit code to the user's phone number via Twilio
- Code expires in 10 minutes
- Used for both phone verification and 2FA

### 3. Authenticator App (TOTP)
- Generates a secret key for TOTP-based authentication
- Provides QR code URL for easy setup
- Compatible with Google Authenticator, Authy, etc.

## Security Features

1. **Password Hashing**: Uses BCrypt for secure password storage
2. **JWT Tokens**: Stateless authentication with configurable expiration
3. **Email Verification**: Required for account activation
4. **2FA Support**: Multiple methods for enhanced security
5. **Rate Limiting**: Built-in protection against brute force attacks

## Configuration

### Environment Variables
```bash
# Email Configuration
EMAIL_USERNAME=your-email@gmail.com
EMAIL_PASSWORD=your-app-password

# SMS Configuration (Twilio)
TWILIO_ACCOUNT_SID=your-twilio-account-sid
TWILIO_AUTH_TOKEN=your-twilio-auth-token
TWILIO_PHONE_NUMBER=+1234567890

# JWT Configuration
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000
```

### Database
- Uses H2 in-memory database for development
- H2 Console available at: http://localhost:8080/h2-console
- Database URL: jdbc:h2:mem:testdb
- Username: sa, Password: password

## Error Handling

All endpoints return appropriate HTTP status codes:
- `200 OK`: Successful operation
- `400 Bad Request`: Invalid input or business logic error
- `401 Unauthorized`: Authentication required or failed
- `403 Forbidden`: Access denied
- `500 Internal Server Error`: Server error

Error responses include a message field with details:
```json
{
  "message": "Error description"
}
```

## Usage Examples

### Complete Signup Flow
1. **Signup**: `POST /api/auth/signup`
2. **Verify Email**: `POST /api/auth/verify-email`
3. **Login**: `POST /api/auth/login`
4. **Access Protected Resources**: Include JWT token in Authorization header

### 2FA Setup Flow
1. **Login**: `POST /api/auth/login`
2. **Change 2FA Method**: `POST /api/auth/change-2fa`
3. **Verify New Method**: Use appropriate verification endpoint
4. **Future Logins**: Include 2FA code in login request

## Testing

You can test the API using tools like:
- Postman
- curl
- Any REST client

Example curl command for signup:
```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "confirmPassword": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```
