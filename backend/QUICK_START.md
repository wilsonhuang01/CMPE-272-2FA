# Quick Start Guide

This guide provides quick commands to run the application in different modes.

## Development Mode (H2 Database)

Development mode uses an in-memory H2 database that resets on each startup.

```bash
# Run with Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or using default profile (already set to dev)
mvn spring-boot:run

# Build and run JAR
mvn clean package
java -jar -Dspring.profiles.active=dev target/cmpe272-0.0.1-SNAPSHOT.jar
```

Access H2 console at: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## Production Mode (MySQL Database)

Production mode uses MySQL database with persistent storage.

### Prerequisites

1. MySQL 8.0+ installed and running
2. Create database: `CREATE DATABASE cmpe272;`

### Using Maven

```bash
# Set environment variables
export DB_HOST=localhost
export DB_NAME=cmpe272
export DB_USERNAME=your_username
export DB_PASSWORD=your_password
export JWT_SECRET=your_secure_secret

# Run application
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Using JAR

```bash
# Build
mvn clean package

# Run
java -jar -Dspring.profiles.active=prod \
  -DDB_HOST=localhost \
  -DDB_NAME=cmpe272 \
  -DDB_USERNAME=your_username \
  -DDB_PASSWORD=your_password \
  -DJWT_SECRET=your_secure_secret \
  target/cmpe272-0.0.1-SNAPSHOT.jar
```

### Using Docker Compose (Recommended)

**Note:** The `docker-compose.yml` file is located in the project root directory.

```bash
# Navigate to project root (from backend directory)
cd ..

# Copy environment template (optional)
cp backend/env.example .env

# Edit .env with your values (optional)
nano .env

# Start all services (MySQL, Backend, Frontend)
docker-compose up --build

# Or run in detached mode
docker-compose up -d --build

# View logs
docker-compose logs -f backend
# Or view all logs
docker-compose logs -f

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

### Using Docker Only

```bash
# Start MySQL first
docker run -d \
  --name cmpe272-mysql \
  -e MYSQL_ROOT_PASSWORD=rootpass \
  -e MYSQL_DATABASE=cmpe272 \
  -e MYSQL_USER=cmpe272_user \
  -e MYSQL_PASSWORD=userpass \
  -p 3306:3306 \
  mysql:8.0

# Wait for MySQL to be ready (10-15 seconds)
sleep 15

# Build application
mvn clean package

# Run application
docker run -d \
  --name cmpe272-app \
  --link cmpe272-mysql:mysql \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_HOST=mysql \
  -e DB_NAME=cmpe272 \
  -e DB_USERNAME=cmpe272_user \
  -e DB_PASSWORD=userpass \
  -e JWT_SECRET=your_secret \
  -v $(pwd)/target/cmpe272-0.0.1-SNAPSHOT.jar:/app.jar \
  eclipse-temurin:21-jre-alpine \
  java -jar /app.jar
```

## Testing

```bash
# Run all tests
mvn test

# Run with specific profile
SPRING_PROFILES_ACTIVE=prod mvn test

# Run single test class
mvn test -Dtest=AuthControllerTest
```

## Useful Commands

```bash
# Check if MySQL is running
mysql -u root -p -e "SELECT VERSION();"

# List databases
mysql -u root -p -e "SHOW DATABASES;"

# Connect to database
mysql -u cmpe272_user -p cmpe272

# View application logs
tail -f logs/spring.log

# Check application health
curl http://localhost:8080/actuator/health

# Build without tests (faster)
mvn clean package -DskipTests
```

## Common Issues

### Connection Refused

**Error**: `Connection refused` when connecting to MySQL

**Solution**: 
- Ensure MySQL is running: `sudo systemctl status mysql`
- Check connection string: `DB_HOST`, `DB_PORT`
- Verify firewall rules allow connections

### Access Denied

**Error**: `Access denied for user`

**Solution**:
- Verify username and password
- Check if user has permissions: `SHOW GRANTS FOR 'username'@'localhost';`
- Grant permissions: `GRANT ALL PRIVILEGES ON cmpe272.* TO 'username'@'localhost';`

### Table/Column Not Found

**Error**: `Table 'cmpe272.users' doesn't exist`

**Solution**:
- Set `DB_DDL_AUTO=update` (already default in prod profile)
- Check application logs for schema creation errors
- Manually run migrations if using Flyway/Liquibase

### Port Already in Use

**Error**: `Port 8080 is already in use`

**Solution**:
- Change port: `SERVER_PORT=8081 mvn spring-boot:run`
- Or kill existing process: `lsof -ti:8080 | xargs kill -9`

## Switching Profiles

To switch between profiles, modify `application.properties`:

```properties
# For development
spring.profiles.active=dev

# For production  
spring.profiles.active=prod
```

Or use environment variable:
```bash
export SPRING_PROFILES_ACTIVE=prod
```