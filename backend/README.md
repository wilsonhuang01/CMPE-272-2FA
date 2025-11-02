# CMPE-272 Backend - Authentication Service

A secure Spring Boot REST API providing comprehensive authentication features with multiple two-factor authentication (2FA) methods.

## Overview

This backend service implements a production-ready authentication system featuring:

- ✅ User registration with email verification
- ✅ JWT-based stateless authentication
- ✅ Multiple 2FA methods:
  - Email verification codes
  - Authenticator app (TOTP) support
- ✅ Password management and security
- ✅ Token blacklisting for secure logout
- ✅ CORS configuration for frontend integration
- ✅ Health check endpoints for monitoring

## Technology Stack

- **Framework:** Spring Boot 3.5.6
- **Language:** Java 21
- **Security:** Spring Security with JWT
- **Database:** 
  - H2 (Development)
  - MySQL 8.0 (Production)
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven
- **Container:** Docker support

## Project Structure

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/wilson/cmpe272/
│   │   │   ├── controller/         # REST API endpoints
│   │   │   │   ├── AuthController.java
│   │   │   │   └── HealthController.java
│   │   │   ├── service/            # Business logic
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── JwtService.java
│   │   │   │   ├── TwoFactorService.java
│   │   │   │   ├── EmailService.java
│   │   │   │   ├── TokenBlacklistService.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   ├── entity/             # JPA entities
│   │   │   │   └── User.java
│   │   │   ├── repository/         # Data access layer
│   │   │   │   └── UserRepository.java
│   │   │   ├── dto/                # Data transfer objects
│   │   │   │   ├── SignupRequest.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── AuthResponse.java
│   │   │   │   └── ...
│   │   │   ├── config/             # Configuration
│   │   │   │   └── SecurityConfig.java
│   │   │   └── Cmpe272Application.java
│   │   └── resources/
│   │       ├── application.properties      # Default config
│   │       ├── application-dev.properties  # Development profile
│   │       └── application-prod.properties # Production profile
│   └── test/                        # Unit and integration tests
├── Dockerfile                       # Docker image definition
├── pom.xml                         # Maven dependencies
├── env.example                     # Environment variables template
├── setup_database.sql              # Database initialization script
├── QUICK_START.md                  # Quick start guide
├── API_DOCUMENTATION.md            # API endpoint documentation
├── PRODUCTION_SETUP.md             # Production deployment guide
└── README.md                      # This file
```

## Prerequisites

- **Java:** JDK 21 or higher
- **Maven:** 3.6+ (or use included Maven wrapper `./mvnw`)
- **Database:**
  - Development: H2 (included, no setup needed)
  - Production: MySQL 8.0+
- **Email Service:** Gmail SMTP (for email verification)
- **Docker:** (Optional) For containerized deployment

## Quick Start

### Development Mode (H2 Database)

The simplest way to run for development:

```bash
# Clone the repository (if not already done)
cd backend

# Run with Maven wrapper
./mvnw spring-boot:run

# Or use system Maven
mvn spring-boot:run
```

The application will start on `http://localhost:8080` with:
- H2 in-memory database (auto-configured)
- H2 Console available at `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: `password`

### Production Mode (MySQL)

1. **Setup MySQL Database:**

```bash
# Create database
mysql -u root -p
CREATE DATABASE cmpe272 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'cmpe272_user'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON cmpe272.* TO 'cmpe272_user'@'%';
FLUSH PRIVILEGES;
EXIT;
```

Or use the provided script:
```bash
mysql -u root -p < setup_database.sql
```

2. **Set Environment Variables:**

```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=cmpe272
export DB_USERNAME=cmpe272_user
export DB_PASSWORD=your_password
export JWT_SECRET=your_secure_random_secret_key_min_256_bits
export MAIL_HOST=smtp.gmail.com
export MAIL_PORT=587
export MAIL_USERNAME=your_email@gmail.com
export MAIL_PASSWORD=your_gmail_app_password
```

3. **Run Application:**

```bash
./mvnw spring-boot:run
```

Or build and run JAR:
```bash
./mvnw clean package
java -jar target/cmpe272-0.0.1-SNAPSHOT.jar
```

## Docker Deployment

### Using Docker Compose (Recommended)

See the root `docker-compose.yml` file for complete setup with MySQL and Frontend.

```bash
# From project root
docker-compose up --build
```

### Standalone Docker

```bash
# Build image
docker build -t cmpe272-backend .

# Run container
docker run -d \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=host.docker.internal \
  -e DB_NAME=cmpe272 \
  -e DB_USERNAME=cmpe272_user \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  -e MAIL_USERNAME=your_email@gmail.com \
  -e MAIL_PASSWORD=your_app_password \
  cmpe272-backend
```

**Note:** For Docker deployment, ensure database is accessible from container network.

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `dev` | No |
| `DB_HOST` | Database host | `localhost` | Yes (prod) |
| `DB_PORT` | Database port | `3306` | No |
| `DB_NAME` | Database name | `cmpe272` | No |
| `DB_USERNAME` | Database username | `cmpe272_user` | Yes (prod) |
| `DB_PASSWORD` | Database password | - | Yes (prod) |
| `JWT_SECRET` | JWT signing secret | `change_this...` | Yes (prod) |
| `JWT_EXPIRATION` | JWT expiration (ms) | `86400000` | No |
| `MAIL_HOST` | SMTP host | `smtp.gmail.com` | No |
| `MAIL_PORT` | SMTP port | `587` | No |
| `MAIL_USERNAME` | SMTP username | - | Yes |
| `MAIL_PASSWORD` | SMTP password/app password | - | Yes |
| `ALLOWED_ORIGINS` | CORS allowed origins (comma-separated) | `http://localhost:3000` | No |

### Application Profiles

#### Development (`dev`)
- Uses H2 in-memory database
- H2 Console enabled
- Auto-creates schema on startup
- No email configuration required (for testing)

#### Production (`prod`)
- Uses MySQL database
- SSL enabled for database connections
- H2 Console disabled
- Requires all environment variables
- Connection pooling optimized
- Enhanced security settings

### Configuration Files

- `application.properties`: Base configuration
- `application-dev.properties`: Development overrides
- `application-prod.properties`: Production overrides

## API Endpoints

### Public Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/signup` | Register new user |
| POST | `/api/auth/login` | Initiate login |
| POST | `/api/auth/login-verify` | Complete login with 2FA |
| POST | `/api/auth/verify-email` | Verify email address |
| POST | `/api/auth/resend-code` | Resend verification code |
| GET | `/actuator/health` | Health check |

### Protected Endpoints (Require JWT)

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/change-password` | Change password |
| POST | `/api/auth/change-2fa` | Change 2FA method |
| GET | `/api/auth/authenticator-qr` | Get TOTP QR code |
| POST | `/api/auth/logout` | Logout (blacklist token) |
| GET | `/api/auth/profile` | Get user profile |

For detailed API documentation with request/response examples, see **[API_DOCUMENTATION.md](./API_DOCUMENTATION.md)**.

## Features

### Authentication Flow

1. **Registration:**
   - User signs up with email and password
   - Email verification code sent
   - User verifies email to activate account

2. **Login:**
   - User provides email and password
   - If 2FA enabled, code is sent/generated
   - User provides 2FA code
   - JWT token issued on successful authentication

3. **Session Management:**
   - Stateless JWT-based sessions
   - Token blacklisting on logout
   - Token expiration handling

### Security Features

- **Password Security:**
  - BCrypt hashing (cost factor 10)
  - Minimum 6 characters enforced
  - Current password verification for changes

- **JWT Authentication:**
  - HS256 algorithm
  - Configurable expiration
  - Stateless sessions
  - Token blacklisting support

- **CORS Protection:**
  - Configurable allowed origins
  - Supports multiple origins (comma-separated)
  - Credentials allowed

- **2FA Methods:**
  - **Email:** 6-digit codes with 10-minute expiration
  - **Authenticator App:** TOTP (Google Authenticator, Authy compatible)

### Database Schema

The application automatically creates the following schema:

**Users Table:**
- `id` (Primary Key)
- `email` (Unique, Not Null)
- `password` (BCrypt hashed)
- `first_name`, `last_name`
- `two_factor_method` (EMAIL, AUTHENTICATOR_APP)
- `two_factor_secret` (TOTP secret for authenticator)
- `is_two_factor_enabled`
- `is_email_verified`
- `email_verification_code`
- `email_verification_expires_at`
- `created_at`, `updated_at`

Schema is auto-created via Hibernate `ddl-auto=update` in both profiles.

## Testing

### Run Tests

```bash
# Run all tests
./mvnw test

# Run with coverage
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=AuthControllerTest
```

### Test Coverage

Tests are located in `src/test/java/com/wilson/cmpe272/`:
- `AuthControllerTest.java` - API endpoint tests
- `TokenBlacklistServiceTest.java` - Token blacklist tests
- `Cmpe272ApplicationTests.java` - Integration tests

## Building

### Build JAR

```bash
./mvnw clean package
```

Output: `target/cmpe272-0.0.1-SNAPSHOT.jar`

### Build Docker Image

```bash
docker build -t cmpe272-backend .
```

### Build for AWS (Linux AMD64)

For AWS ECS Fargate deployment:

```bash
docker build --platform linux/amd64 -t cmpe272-backend .
```

## Deployment

### AWS ECS Deployment

Key considerations:
- Build with `--platform linux/amd64` for Fargate
- Configure `ALLOWED_ORIGINS` environment variable
- Ensure RDS database exists before deployment
- Use same VPC for RDS and ECS tasks

### Environment Setup

Create `.env` file or set environment variables:

```bash
cp env.example .env
# Edit .env with your values
```

## Health Checks

The application provides a health check endpoint for monitoring and load balancers:

```bash
curl http://localhost:8080/actuator/health
```

Response:
```json
{
  "status": "UP"
}
```

## Logging

Logs are configured via `application.properties`:
- Console logging: INFO level
- Application-specific logging: INFO level
- Log pattern: `yyyy-MM-dd HH:mm:ss - %msg%n`

For production, logs can be streamed to CloudWatch or other log aggregation services.

## Troubleshooting

### Common Issues

#### Database Connection Errors

**Error:** `Communications link failure` or `Cannot resolve reference to bean 'entityManagerFactory'`

**Solutions:**
1. Verify database is running and accessible
2. Check database credentials in environment variables
3. Ensure database `cmpe272` exists (create it if needed)
4. Verify security groups/firewall allow connections
5. Check VPC configuration (if on AWS)
6. Increase connection timeout: `spring.datasource.hikari.connection-timeout=60000`

#### Port Already in Use

**Error:** `Port 8080 is already in use`

**Solution:**
```bash
# Change port
export SERVER_PORT=8081

# Or kill existing process
lsof -ti:8080 | xargs kill -9
```

#### CORS Errors

**Error:** CORS policy blocks requests from frontend

**Solution:**
- Set `ALLOWED_ORIGINS` environment variable to frontend URL
- Include protocol: `http://localhost:3000` or `https://yourdomain.com`
- Multiple origins: `https://domain1.com,https://domain2.com`

#### Email Not Sending

**Error:** Email verification codes not received

**Solutions:**
1. Use Gmail App Password (not regular password)
2. Verify `MAIL_USERNAME` and `MAIL_PASSWORD` are correct
3. Check Gmail account settings (less secure apps, if applicable)
4. Verify SMTP host and port settings

#### JWT Token Issues

**Error:** Token validation fails or unauthorized

**Solutions:**
1. Verify `JWT_SECRET` matches between deployments
2. Check token expiration time
3. Ensure token is included in `Authorization: Bearer <token>` header
4. Check if token was blacklisted (logout)

## Development Tips

### Hot Reload

Use Spring Boot DevTools for automatic restarts:

```bash
./mvnw spring-boot:run
# Edit files, application auto-restarts
```

### Database Console (Dev Only)

Access H2 Console during development:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

**Warning:** H2 Console is disabled in production profile for security.

### Debug Mode

Enable debug logging:

```bash
export LOGGING_LEVEL_ROOT=DEBUG
./mvnw spring-boot:run
```

## Performance Considerations

### Connection Pooling

HikariCP is configured with:
- Maximum pool size: 20
- Minimum idle: 5
- Connection timeout: 60s (production)
- Idle timeout: 10 minutes
- Max lifetime: 30 minutes

### JWT Performance

- Stateless design enables horizontal scaling
- Token blacklisting stored in-memory (consider Redis for distributed systems)
- Token expiration reduces security risk

## Security Best Practices

1. **JWT Secret:** Use a strong, random secret (minimum 256 bits)
2. **Database:** Use SSL connections in production
3. **Passwords:** Never log or expose passwords
4. **CORS:** Restrict allowed origins in production
5. **Environment Variables:** Never commit secrets to version control
6. **HTTPS:** Use HTTPS in production (configure reverse proxy/load balancer)
7. **Rate Limiting:** Consider implementing rate limiting for authentication endpoints

---

**Last Updated:** November 2025

