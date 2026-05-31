CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    role ENUM('ADMIN', 'CUSTOMER') NOT NULL DEFAULT 'CUSTOMER'
);

CREATE TABLE IF NOT EXISTS concert_locations (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    city VARCHAR(120),
    venue_name VARCHAR(180),
    country VARCHAR(120),
    address VARCHAR(255),
    concert_date DATETIME,
    description TEXT,
    image_url VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS seat_sections (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    location_id BIGINT NOT NULL,
    section_code VARCHAR(20) NOT NULL,
    section_name VARCHAR(120) NOT NULL,
    ticket_type VARCHAR(80) NOT NULL,
    price DECIMAL(10, 2),
    seat_location VARCHAR(160),
    grid_row INT NOT NULL DEFAULT 1,
    grid_column INT NOT NULL DEFAULT 1,
    row_span INT NOT NULL DEFAULT 1,
    column_span INT NOT NULL DEFAULT 1,
    display_order INT NOT NULL DEFAULT 0,
    capacity INT,
    UNIQUE KEY uk_seat_sections_location_code (location_id, section_code),
    INDEX idx_seat_sections_location_order (location_id, display_order)
);

CREATE TABLE IF NOT EXISTS seats (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    location_id BIGINT,
    section_id BIGINT,
    user_id BIGINT,
    row_label VARCHAR(20),
    seat_number VARCHAR(20),
    status ENUM('AVAILABLE', 'HELD', 'BOOKED', 'UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
    INDEX idx_seats_location_status (location_id, status),
    INDEX idx_seats_section_status (section_id, status),
    INDEX idx_seats_user_status (user_id, status)
);

INSERT IGNORE INTO seat_sections (
    location_id,
    section_code,
    section_name,
    ticket_type,
    price,
    seat_location,
    grid_row,
    grid_column,
    row_span,
    column_span,
    display_order,
    capacity
)
SELECT
    concert_locations.id,
    layout_template.section_code,
    CONCAT(layout_template.section_code, ' ', layout_template.ticket_type),
    layout_template.ticket_type,
    layout_template.price,
    layout_template.seat_location,
    layout_template.grid_row,
    layout_template.grid_column,
    layout_template.row_span,
    layout_template.column_span,
    layout_template.display_order,
    layout_template.capacity
FROM concert_locations
CROSS JOIN (
    SELECT 'A1' AS section_code, 'V.I.P' AS ticket_type, 220.00 AS price, 'Front Left' AS seat_location, 1 AS grid_row, 1 AS grid_column, 1 AS row_span, 1 AS column_span, 1 AS display_order, 80 AS capacity
    UNION ALL SELECT 'A2', 'V.I.P', 260.00, 'Front Center', 1, 2, 1, 1, 2, 100
    UNION ALL SELECT 'A3', 'V.I.P', 220.00, 'Front Right', 1, 3, 1, 1, 3, 80
    UNION ALL SELECT 'B1', 'PLUS', 150.00, 'Middle Left', 2, 1, 1, 1, 4, 120
    UNION ALL SELECT 'B2', 'STANDING', 180.00, 'Center Floor', 2, 2, 1, 1, 5, 220
    UNION ALL SELECT 'B3', 'PLUS', 150.00, 'Middle Right', 2, 3, 1, 1, 6, 120
    UNION ALL SELECT 'C1', 'ECO', 90.00, 'Rear Left', 3, 1, 1, 1, 7, 160
    UNION ALL SELECT 'C2', 'ECO', 110.00, 'Rear Center', 3, 2, 1, 1, 8, 180
    UNION ALL SELECT 'C3', 'ECO', 90.00, 'Rear Right', 3, 3, 1, 1, 9, 160
) AS layout_template;

UPDATE seat_sections
JOIN (SELECT MIN(id) AS location_id FROM concert_locations) AS standing_location
    ON seat_sections.location_id = standing_location.location_id
SET
    seat_sections.ticket_type = 'STANDING',
    seat_sections.section_name = 'A2 STANDING',
    seat_sections.price = 180.00,
    seat_sections.seat_location = 'A2 + B2 Center Standing',
    seat_sections.grid_row = 1,
    seat_sections.grid_column = 2,
    seat_sections.row_span = 2,
    seat_sections.column_span = 1,
    seat_sections.display_order = 2,
    seat_sections.capacity = 320
WHERE seat_sections.section_code = 'A2';

DELETE seat_sections
FROM seat_sections
JOIN (SELECT MIN(id) AS location_id FROM concert_locations) AS standing_location
    ON seat_sections.location_id = standing_location.location_id
WHERE seat_sections.section_code = 'B2';
