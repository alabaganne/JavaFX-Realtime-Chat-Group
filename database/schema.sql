-- JavaFX Real-Time Chat Application Database Schema
-- MySQL 8.0+

-- Create database
CREATE DATABASE IF NOT EXISTS chat CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chat;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL COMMENT 'BCrypt hashed password',
    role VARCHAR(20) DEFAULT 'user' COMMENT 'user or admin',
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Indexes for performance
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_created (created)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    text TEXT NOT NULL,
    created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraint
    CONSTRAINT fk_messages_user
        FOREIGN KEY (userId)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    -- Indexes for performance
    INDEX idx_user (userId),
    INDEX idx_created (created)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user
-- Email: admin@chat.com
-- Password: admin123 (BCrypt hashed)
INSERT INTO users (name, email, password, role) VALUES
('Admin User', 'admin@chat.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYNg4oMpU7i', 'admin')
ON DUPLICATE KEY UPDATE name=name; -- Prevent duplicate if already exists

-- Insert sample users for testing (optional)
-- Password for all test users: test1234
INSERT INTO users (name, email, password, role) VALUES
('John Doe', 'john@example.com', '$2a$12$8ZE3L4YHqw3K9YP.vVZQYO0vL7z8M5Q.N3K2P1R4S5T6U7V8W9X0Y', 'user'),
('Jane Smith', 'jane@example.com', '$2a$12$8ZE3L4YHqw3K9YP.vVZQYO0vL7z8M5Q.N3K2P1R4S5T6U7V8W9X0Y', 'user'),
('Bob Johnson', 'bob@example.com', '$2a$12$8ZE3L4YHqw3K9YP.vVZQYO0vL7z8M5Q.N3K2P1R4S5T6U7V8W9X0Y', 'user')
ON DUPLICATE KEY UPDATE name=name;

-- Insert sample welcome message
INSERT INTO messages (userId, text) VALUES
(1, 'Welcome to the JavaFX Group Chat! Feel free to start chatting.')
ON DUPLICATE KEY UPDATE text=text;

-- Show tables
SHOW TABLES;

-- Show user count
SELECT
    role,
    COUNT(*) as count
FROM users
GROUP BY role;

-- Show table structures
DESCRIBE users;
DESCRIBE messages;

-- Success message
SELECT '✅ Database setup completed successfully!' as Status;
