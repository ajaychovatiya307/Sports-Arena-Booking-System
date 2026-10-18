CREATE DATABASE IF NOT EXISTS auth_db;
USE auth_db;

CREATE TABLE IF NOT EXISTS user_credentials (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

INSERT IGNORE INTO user_credentials (id, user_id, full_name, email, password, role) VALUES
 (1, 'A1', 'System Administrator', 'admin@sportsarena.com', 'admin1234', 'ADMIN'),
 (2, 'U1', 'Rohan Verma',   'rohan@gmail.com',  'pwd123', 'CUSTOMER'),
 (3, 'U2', 'Sneha Iyer',    'sneha@gmail.com',  'pwd123', 'CUSTOMER'),
 (4, 'U3', 'Farhan Ahmed',  'farhan@gmail.com', 'pwd123', 'CUSTOMER'),
 (5, 'U4', 'Meera Kapoor',  'meera@gmail.com',  'pwd123', 'CUSTOMER');
