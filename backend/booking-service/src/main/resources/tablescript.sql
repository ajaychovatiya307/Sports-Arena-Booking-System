CREATE DATABASE IF NOT EXISTS booking_db;
USE booking_db;

CREATE TABLE IF NOT EXISTS booking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_number VARCHAR(50) UNIQUE NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    customer_name VARCHAR(255),
    team_id BIGINT NOT NULL,
    team_name VARCHAR(200),
    package_id BIGINT NOT NULL,
    package_name VARCHAR(200),
    booking_date DATE NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    special_request VARCHAR(1000),
    status VARCHAR(30) NOT NULL,
    total_amount DECIMAL(10,2),
    created_at DATETIME
);

CREATE TABLE IF NOT EXISTS rating (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT UNIQUE NOT NULL,
    reference_number VARCHAR(50) NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    package_id BIGINT NOT NULL,
    stars INT NOT NULL,
    review VARCHAR(1000),
    created_at DATETIME,
    CONSTRAINT fk_rating_booking FOREIGN KEY (booking_id) REFERENCES booking(id)
);

INSERT IGNORE INTO booking
(id, reference_number, user_id, customer_name, team_id, team_name, package_id, package_name, booking_date, 
time_slot, special_request, status, total_amount, created_at)
 VALUES
 (1,'ARN-10001','U1','Rohan Verma', 1,'Rohan Rangers',   1,'5-a-Side Football Turf',       
 DATE_ADD(CURDATE(), INTERVAL 2 DAY), '18:00-19:00','Need extra bibs',   'PENDING',   1200.00, NOW()),
 (2,'ARN-10002','U1','Rohan Verma', 3,'Rohan Rebels',    2,'Box Cricket Net',              
 DATE_ADD(CURDATE(), INTERVAL 5 DAY), '20:00-21:00',NULL,               'CONFIRMED', 900.00,  NOW()),
 (3,'ARN-10003','U1','Rohan Verma', 2,'Verma Volleys',   3,'Badminton Court (Hourly)',     
 DATE_SUB(CURDATE(), INTERVAL 3 DAY), '07:00-08:00',NULL,               'COMPLETED', 500.00,  NOW()),
 (4,'ARN-20001','U2','Sneha Iyer',  4,'Iyer Aces',       4,'Tennis Court (Hourly)',        
 DATE_ADD(CURDATE(), INTERVAL 1 DAY), '17:00-18:00',NULL,               'PENDING',   800.00,  NOW()),
 (5,'ARN-20002','U2','Sneha Iyer',  5,'Sneha Smashers',  3,'Badminton Court (Hourly)',     
 DATE_ADD(CURDATE(), INTERVAL 3 DAY), '18:00-19:00',NULL,               'CONFIRMED', 500.00,  NOW()),
 (6,'ARN-20003','U2','Sneha Iyer',  6,'Chennai Chargers',5,'Basketball Court (Hourly)',    
 DATE_ADD(CURDATE(), INTERVAL 7 DAY), '19:00-20:00',NULL,               'CHECKED_IN',1000.00, NOW()),
 (7,'ARN-20004','U2','Sneha Iyer',  4,'Iyer Aces',       6,'Swimming Pool Lane (Hourly)',  
 DATE_SUB(CURDATE(), INTERVAL 1 DAY), '06:00-07:00',NULL,               'COMPLETED', 400.00,  NOW()),
 (8,'ARN-30001','U3','Farhan Ahmed',7,'Ahmed Arrows',    1,'5-a-Side Football Turf',       
 DATE_ADD(CURDATE(), INTERVAL 4 DAY), '19:00-20:00',NULL,               'PENDING',   1200.00, NOW()),
 (9,'ARN-30002','U3','Farhan Ahmed',8,'Farhan Falcons',  2,'Box Cricket Net',             
  DATE_ADD(CURDATE(), INTERVAL 6 DAY), '20:00-21:00',NULL,               'CANCELLED', 900.00,  NOW()),
 (10,'ARN-30003','U3','Farhan Ahmed',9,'Hyd Hornets',    5,'Basketball Court (Hourly)',    
 DATE_SUB(CURDATE(), INTERVAL 5 DAY), '18:00-19:00','Great session',    'COMPLETED', 1000.00, NOW());
