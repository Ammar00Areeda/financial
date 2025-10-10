-- Create users table
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    is_account_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    is_account_non_locked BOOLEAN NOT NULL DEFAULT TRUE,
    is_credentials_non_expired BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert a default admin user (password: admin123)
-- Password is BCrypt encoded
INSERT INTO users (username, email, password, first_name, last_name, role, is_enabled, is_account_non_expired, is_account_non_locked, is_credentials_non_expired)
VALUES ('admin', 'admin@financial.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1R0VhqjO7JgJkEuTzPjUqNdG7LpJzAW', 'Admin', 'User', 'ADMIN', TRUE, TRUE, TRUE, TRUE);

-- Insert a default regular user (password: user123)
INSERT INTO users (username, email, password, first_name, last_name, role, is_enabled, is_account_non_expired, is_account_non_locked, is_credentials_non_expired)
VALUES ('user', 'user@financial.com', '$2a$10$ZJCvP3QfYJ7a0c0F4MX5QeJ1LnFBWKKKBz6Uu1zF1xW0C7dH6MQZi', 'Regular', 'User', 'USER', TRUE, TRUE, TRUE, TRUE);

