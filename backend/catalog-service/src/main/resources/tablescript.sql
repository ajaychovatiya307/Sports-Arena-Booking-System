CREATE DATABASE IF NOT EXISTS catalog_db;
USE catalog_db;

CREATE TABLE IF NOT EXISTS arena_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(150) UNIQUE NOT NULL,
    description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS arena_package (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    price_per_hour DECIMAL(10,2) NOT NULL,
    duration_hours INT NOT NULL,
    category_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_ap_cat FOREIGN KEY (category_id) REFERENCES arena_category(id)
);

CREATE TABLE IF NOT EXISTS slot_availability (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    package_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    time_slot VARCHAR(50) NOT NULL,
    blocked BOOLEAN DEFAULT FALSE,
    reason VARCHAR(255),
    UNIQUE KEY uk_slot (package_id, slot_date, time_slot),
    CONSTRAINT fk_slot_pkg FOREIGN KEY (package_id) REFERENCES arena_package(id)
);

INSERT IGNORE INTO arena_category (id, category_code, name, description) VALUES
 (1,'CAT001','Outdoor Turf',   'Open-air football and cricket turfs'),
 (2,'CAT002','Indoor Court',   'Indoor badminton, tennis and basketball courts'),
 (3,'CAT003','Aquatic Center', 'Swimming pools and aquatic training centers');

INSERT IGNORE INTO arena_package (id, package_code, name, description, price_per_hour, duration_hours, category_id, status) VALUES
 (1,'PKG001','5-a-Side Football Turf',       'Premium 5-a-side football turf with floodlights', 1200.00, 1, 1, 'ACTIVE'),
 (2,'PKG002','Box Cricket Net',              'Netted box cricket pitch with equipment',          900.00, 1, 1, 'ACTIVE'),
 (3,'PKG003','Badminton Court (Hourly)',     'Wooden indoor badminton court',                     500.00, 1, 2, 'ACTIVE'),
 (4,'PKG004','Tennis Court (Hourly)',        'Synthetic tennis court, indoor',                    800.00, 1, 2, 'ACTIVE'),
 (5,'PKG005','Basketball Court (Hourly)',    'Full-size indoor basketball court',                1000.00, 1, 2, 'ACTIVE'),
 (6,'PKG006','Swimming Pool Lane (Hourly)',  'Single 25m lane in the aquatic center',             400.00, 1, 3, 'ACTIVE');
