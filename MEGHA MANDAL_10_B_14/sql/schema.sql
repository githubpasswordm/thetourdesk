-- Create Table for Tour Packages
CREATE TABLE packages (
    pkg_id INT PRIMARY KEY AUTO_INCREMENT,
    destination VARCHAR(100),
    depart_date DATE,
    total_seats INT,
    booked_seats INT DEFAULT 0,
    price DECIMAL(10,2)
);

-- Create Table for Customer Bookings
CREATE TABLE bookings (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    pkg_id INT,
    customer VARCHAR(100),
    seats INT,
    booked_on DATETIME DEFAULT NOW(),
    cancelled BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (pkg_id) REFERENCES packages(pkg_id)
);