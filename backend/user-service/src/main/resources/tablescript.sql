CREATE DATABASE IF NOT EXISTS user_db;
USE user_db;

CREATE TABLE IF NOT EXISTS customer_profile (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    contact_number VARCHAR(20),
    address VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    team_name VARCHAR(150) NOT NULL,
    sport_type VARCHAR(30) NOT NULL,
    number_of_players INT NOT NULL,
    coach_name VARCHAR(150),
    founded_year INT NOT NULL
);

INSERT IGNORE INTO customer_profile (id, user_id, full_name, email, contact_number, address) VALUES
 (1, 'U1', 'Rohan Verma',  'rohan@gmail.com',  '9000000001', 'Bengaluru, KA'),
 (2, 'U2', 'Sneha Iyer',   'sneha@gmail.com',  '9000000002', 'Chennai, TN'),
 (3, 'U3', 'Farhan Ahmed', 'farhan@gmail.com', '9000000003', 'Hyderabad, TS'),
 (4, 'U4', 'Meera Kapoor', 'meera@gmail.com',  '9000000004', 'Pune, MH');

INSERT IGNORE INTO team (id, user_id, team_name, sport_type, number_of_players, coach_name, founded_year) VALUES
 (1,'U1','Rohan Rangers',   'FOOTBALL', 11, 'Coach Ali',    2019),
 (2,'U1','Verma Volleys',   'BADMINTON', 4, 'Self',         2021),
 (3,'U1','Rohan Rebels',    'CRICKET',  11, 'Coach Suresh', 2018),
 (4,'U2','Iyer Aces',       'TENNIS',    2, 'Coach Priya',  2020),
 (5,'U2','Sneha Smashers',  'BADMINTON', 4, 'Self',         2022),
 (6,'U2','Chennai Chargers','BASKETBALL',5, 'Coach Raj',    2017),
 (7,'U3','Ahmed Arrows',    'FOOTBALL', 11, 'Coach Zia',    2016),
 (8,'U3','Farhan Falcons',  'CRICKET',  11, 'Coach Imran',  2019),
 (9,'U3','Hyd Hornets',     'BASKETBALL',5, 'Coach Ravi',   2023),
 (10,'U4','Kapoor Kings',   'CRICKET',  11, 'Coach Meena',  2020),
 (11,'U4','Pune Panthers',  'FOOTBALL', 11, 'Coach Aditi',  2021);
