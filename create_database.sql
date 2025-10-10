-- Create database for financial application
CREATE DATABASE IF NOT EXISTS financial_db 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Create a user for the application (optional, you can use root)
CREATE USER IF NOT EXISTS 'financial_user'@'localhost' IDENTIFIED BY 'financial_password';

-- Grant privileges to the user
GRANT ALL PRIVILEGES ON financial_db.* TO 'financial_user'@'localhost';

-- Refresh privileges
FLUSH PRIVILEGES;

-- Show the created database
SHOW DATABASES LIKE 'financial_db';

