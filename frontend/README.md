# Frontend - 2FA Authentication System

A modern React TypeScript application showcasing a comprehensive two-factor authentication system.

## Features

- 🔐 User Registration with Email Verification
- 🔑 Secure Login with JWT Authentication
- 📧 Email-based Two-Factor Authentication
- 🔑 Authenticator App (TOTP) Support
- 🔒 Password Change Functionality
- ⚙️ Settings Management
- 🎨 Modern, Responsive UI

## Tech Stack

- **React 18** - UI Library
- **TypeScript** - Type Safety
- **React Router** - Routing
- **Axios** - HTTP Client
- **Context API** - State Management

## Getting Started

### Prerequisites

- Node.js 14+ 
- npm or yarn
- Backend server running on http://localhost:8080

### Installation

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

4. Open http://localhost:3000 in your browser

## Project Structure

```
frontend/
├── public/
├── src/
│   ├── context/          # Authentication context
│   │   └── AuthContext.tsx
│   ├── services/         # API services
│   │   ├── api.ts
│   │   └── authService.ts
│   ├── pages/            # Page components
│   │   ├── Login.tsx
│   │   ├── Signup.tsx
│   │   ├── VerifyEmail.tsx
│   │   ├── Dashboard.tsx
│   │   └── Settings.tsx
│   ├── types/            # TypeScript types
│   │   └── index.ts
│   ├── App.tsx
│   └── index.tsx
├── package.json
└── README.md
```

## Usage

### 1. Sign Up

1. Navigate to `/signup`
2. Fill in your details:
   - Email
   - First Name
   - Last Name
   - Password (minimum 6 characters)
   - Confirm Password
   - Phone Number (optional)
   - Two-Factor Method (optional)
3. Click "Sign Up"
4. Verify your email with the code sent to your inbox

### 2. Login

1. Navigate to `/login`
2. Enter your email and password
3. If 2FA is enabled, enter the verification code
4. You'll be redirected to the dashboard upon successful login

### 3. Dashboard

- View your account information
- See your current 2FA settings
- Navigate to settings or logout

### 4. Settings

**Change Password:**
1. Go to Settings
2. Enter your current password
3. Enter and confirm your new password
4. Click "Change Password"

**Update 2FA Method:**
1. Go to Settings
2. Switch to "Two-Factor Authentication" tab
3. Enter your password
4. Select a new 2FA method
6. Click "Update 2FA Method"
7. For Authenticator App, scan the QR code

## API Integration

The frontend communicates with the backend at `http://localhost:8080/api/auth`:

### Endpoints Used:
- `POST /signup` - User registration
- `POST /login` - User authentication
- `POST /verify-email` - Email verification
- `POST /change-password` - Password change
- `POST /change-2fa` - Update 2FA method
- `GET /authenticator-qr` - Get QR code for authenticator
- `GET /profile` - Get user profile
- `POST /logout` - User logout
- `POST /resend-code` - Resend verification code

## Authentication Flow

1. **Registration:**
   - User signs up → Email verification code sent
   - User verifies email → Account activated

2. **Login:**
   - User logs in with credentials
   - If 2FA enabled → Verification code required
   - JWT token issued and stored

3. **Protected Routes:**
   - Token stored in localStorage
   - Token included in Authorization header
   - Auto-redirect to login if token expires

4. **Logout:**
   - Token invalidated on backend
   - Local storage cleared
   - User redirected to login

## Configuration

### API Base URL

The API base URL is configured via environment variable `REACT_APP_API_BASE_URL`:

**For Local Development:**
Update the API base URL in `src/services/api.ts`:
```typescript
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api/auth';
```

**For Docker/Production:**
Set the `REACT_APP_API_BASE_URL` environment variable during build:
```bash
REACT_APP_API_BASE_URL=http://your-backend-url/api/auth npm run build
```

Or configure it in `docker-compose.yml`:
```yaml
frontend:
  build:
    args:
      REACT_APP_API_BASE_URL: ${REACT_APP_API_BASE_URL:-http://localhost:8080/api/auth}
```

### Docker Deployment

The frontend includes Docker support with a multi-stage build:

**Build and run with Docker:**
```bash
# Build the image
docker build -t cmpe272-frontend -f Dockerfile .

# Run the container
docker run -p 3000:80 cmpe272-frontend
```

**Or use Docker Compose (from project root):**
```bash
docker-compose up frontend
```

The Dockerfile uses:
- Node.js for building the React app
- Nginx for serving the production build
- Health checks for container monitoring

## Scripts

- `npm start` - Start development server
- `npm build` - Build for production
- `npm test` - Run tests
- `npm eject` - Eject from Create React App

## Notes

- Ensure the backend server is running before starting the frontend
- The application uses localStorage for token persistence
- All HTTP requests include authentication tokens automatically
- Responsive design works on mobile and desktop

## Troubleshooting

**CORS Errors:**
- Ensure backend CORS is configured to allow requests from `http://localhost:3000` (or your frontend URL)
- For Docker deployments, ensure the backend service name is correctly configured

**Connection Errors:**
- Verify backend is running on port 8080
- Check `REACT_APP_API_BASE_URL` environment variable (for Docker) or `API_BASE_URL` in `src/services/api.ts` (for local development)
- In Docker Compose, the frontend container communicates with backend via the service name

**Docker Build Issues:**
- If `npm ci` fails due to package lock mismatch, the Dockerfile uses `npm install --legacy-peer-deps` as a workaround
- To fix permanently, update `package-lock.json` locally: `npm install`

**Authentication Issues:**
- Clear localStorage and try logging in again
- Check browser console for error messages
- Verify JWT token is being sent in Authorization header
