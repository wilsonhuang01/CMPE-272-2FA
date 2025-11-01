-- MySQL Database Setup Script for CMPE-272 2FA Application
-- Run this script as MySQL root user: mysql -u root -p < setup_database.sql
-- Or execute in MySQL: source setup_database.sql

-- Create the database
CREATE DATABASE IF NOT EXISTS cmpe272
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Drop existing users if they exist to ensure clean setup
-- This is safe because we'll recreate them immediately
DROP USER IF EXISTS 'cmpe272_user'@'%';
DROP USER IF EXISTS 'cmpe272_user'@'localhost';

-- Create user with access from any host (%)
-- This allows connections from Docker containers, remote hosts, etc.
-- IMPORTANT: This matches the application-prod.properties default username
CREATE USER 'cmpe272_user'@'%' 
    IDENTIFIED BY 'notasecurepassword';

-- Create user with access from localhost only
-- This is more secure for local development
CREATE USER 'cmpe272_user'@'localhost' 
    IDENTIFIED BY 'notasecurepassword';

-- Grant all privileges on the database to the user
-- For production, consider granting only necessary privileges
GRANT ALL PRIVILEGES ON cmpe272.* TO 'cmpe272_user'@'%';
GRANT ALL PRIVILEGES ON cmpe272.* TO 'cmpe272_user'@'localhost';

-- Alternative: Grant specific privileges (more secure)
-- GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER ON cmpe272.* TO 'cmpe272_user'@'%';
-- GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, DROP, INDEX, ALTER ON cmpe272.* TO 'cmpe272_user'@'localhost';

-- Flush privileges to apply changes
FLUSH PRIVILEGES;

-- Verify the setup
SHOW DATABASES LIKE 'cmpe272';
SELECT user, host FROM mysql.user WHERE user = 'cmpe272_user';

-- Display success message
SELECT 'Database and user created successfully!' AS status;

