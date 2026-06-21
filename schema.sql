-- Canteen Management System Database Schema - SQL Server Version
-- Database: canteen_db

-- Create Database
CREATE DATABASE canteen_db;
GO
USE canteen_db;
GO

-- Table: Admin Users
CREATE TABLE admin_users (
    admin_id INT PRIMARY KEY IDENTITY(1, 1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    role VARCHAR(50) DEFAULT 'ADMIN',
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Table: Customers (Students)
CREATE TABLE customers (
    customer_id INT PRIMARY KEY IDENTITY(1, 1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    student_code VARCHAR(50) NULL,
    date_of_birth DATE,
    address VARCHAR(255),
    class VARCHAR(50),
    balance DECIMAL(10, 2) DEFAULT 0.00,
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Table: Staff (Nhân viên)
CREATE TABLE staff_members (
    staff_id INT PRIMARY KEY IDENTITY(1, 1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    staff_code VARCHAR(50) UNIQUE NOT NULL,
    position VARCHAR(100),
    salary DECIMAL(12, 2),
    department VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Table: User Sessions (for tracking login history)
CREATE TABLE user_sessions (
    session_id INT PRIMARY KEY IDENTITY(1, 1),
    user_id INT NOT NULL,
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('ADMIN', 'CUSTOMER', 'STAFF')),
    ip_address VARCHAR(45),
    user_agent VARCHAR(MAX),
    login_time DATETIME DEFAULT GETDATE(),
    logout_time DATETIME NULL,
    duration_minutes INT,
    status VARCHAR(50)
);

-- Table: Registration Requests (pending registrations)
CREATE TABLE registration_requests (
    reg_id INT PRIMARY KEY IDENTITY(1, 1),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    user_type VARCHAR(20) NOT NULL CHECK (user_type IN ('CUSTOMER', 'STAFF')),
    phone VARCHAR(15),
    additional_info VARCHAR(MAX),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at DATETIME DEFAULT GETDATE(),
    reviewed_by INT,
    reviewed_at DATETIME NULL,
    notes VARCHAR(255)
);

-- Create indexes for better performance
CREATE INDEX idx_admin_username ON admin_users(username);
CREATE INDEX idx_admin_email ON admin_users(email);
CREATE INDEX idx_customer_username ON customers(username);
CREATE INDEX idx_customer_email ON customers(email);
-- Create a filtered unique index so only non-null student codes must be unique
CREATE UNIQUE INDEX ux_customers_student_code_filtered ON customers(student_code) WHERE student_code IS NOT NULL;
CREATE INDEX idx_staff_username ON staff_members(username);
CREATE INDEX idx_staff_email ON staff_members(email);
CREATE INDEX idx_staff_code ON staff_members(staff_code);
CREATE INDEX idx_registration_status ON registration_requests(status);
CREATE INDEX idx_registration_username ON registration_requests(username);
CREATE INDEX idx_user_sessions_user ON user_sessions(user_id, user_type);
GO

-- Insert default admin account (Password: admin123 hashed with MD5)
INSERT INTO admin_users (username, password, email, full_name, phone, role, status)
VALUES ('admin', '0192023a7bbd73250516f069df18b500', 'admin@canteen.edu.vn', 'Admin System', '0123456789', 'ADMIN', 'ACTIVE');

-- Insert sample customers (Password: password123 hashed with MD5)
INSERT INTO customers (username, password, email, full_name, phone, student_code, date_of_birth, class, balance, status)
VALUES 
('SV001', '482c811da5d5b4bc6d497ffa98491e38', 'sv001@student.edu.vn', 'Nguyễn Văn A', '0912345678', 'SV001', '2005-01-15', 'C1', 500000.00, 'ACTIVE'),
('SV002', '482c811da5d5b4bc6d497ffa98491e38', 'sv002@student.edu.vn', 'Trần Thị B', '0912345679', 'SV002', '2005-03-20', 'C2', 750000.00, 'ACTIVE');

-- Insert sample staff (Password: password123 hashed with MD5)
INSERT INTO staff_members (username, password, email, full_name, phone, staff_code, position, salary, department, status)
VALUES 
('STAFF001', '482c811da5d5b4bc6d497ffa98491e38', 'staff001@canteen.edu.vn', 'Lê Văn C', '0912345680', 'NV001', 'Quản lý bán hàng', 12000000.00, 'Kinh doanh', 'ACTIVE'),
('STAFF002', '482c811da5d5b4bc6d497ffa98491e38', 'staff002@canteen.edu.vn', 'Phạm Thị D', '0912345681', 'NV002', 'Thu ngân', 9000000.00, 'Tài chính', 'ACTIVE');

-- Create triggers for updated_at field in admin_users
CREATE TRIGGER trg_admin_users_update
ON admin_users
AFTER UPDATE
AS
BEGIN
    UPDATE admin_users SET updated_at = GETDATE() WHERE admin_id IN (SELECT admin_id FROM inserted)
END;
GO

-- Create triggers for updated_at field in customers
CREATE TRIGGER trg_customers_update
ON customers
AFTER UPDATE
AS
BEGIN
    UPDATE customers SET updated_at = GETDATE() WHERE customer_id IN (SELECT customer_id FROM inserted)
END;
GO

-- Create triggers for updated_at field in staff_members
CREATE TRIGGER trg_staff_members_update
ON staff_members
AFTER UPDATE
AS
BEGIN
    UPDATE staff_members SET updated_at = GETDATE() WHERE staff_id IN (SELECT staff_id FROM inserted)
END;
GO

-- Table: Menu Items (Thực đơn)
CREATE TABLE menu_items (
    item_id INT PRIMARY KEY IDENTITY(1, 1),
    item_name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255),
    available INT DEFAULT 1 CHECK (available IN (0, 1)),
    created_by INT,
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE()
);

-- Table: Orders (Đơn hàng)
CREATE TABLE orders (
    order_id INT PRIMARY KEY IDENTITY(1, 1),
    customer_id INT NOT NULL,
    staff_id INT,
    order_date DATETIME DEFAULT GETDATE(),
    delivery_date DATETIME,
    total_amount DECIMAL(12, 2),
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'CANCELLED')),
    notes VARCHAR(500),
    created_at DATETIME DEFAULT GETDATE(),
    updated_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
    FOREIGN KEY (staff_id) REFERENCES staff_members(staff_id)
);

-- Table: Order Details (Chi tiết đơn hàng)
CREATE TABLE order_details (
    detail_id INT PRIMARY KEY IDENTITY(1, 1),
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(12, 2),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES menu_items(item_id)
);

-- Table: Payments (Thanh toán)
CREATE TABLE payments (
    payment_id INT PRIMARY KEY IDENTITY(1, 1),
    order_id INT NOT NULL,
    customer_id INT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL CHECK (payment_method IN ('CASH', 'CARD', 'TRANSFER', 'WALLET')),
    status VARCHAR(50) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    transaction_code VARCHAR(100),
    payment_date DATETIME,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
);

-- Table: Revenue Report (Thống kê doanh thu)
CREATE TABLE revenue_reports (
    report_id INT PRIMARY KEY IDENTITY(1, 1),
    report_date DATE NOT NULL,
    total_orders INT,
    total_revenue DECIMAL(12, 2),
    completed_orders INT,
    cancelled_orders INT,
    created_at DATETIME DEFAULT GETDATE()
);

-- Create indexes for better performance
CREATE INDEX idx_menu_category ON menu_items(category);
CREATE INDEX idx_menu_available ON menu_items(available);
CREATE INDEX idx_order_customer ON orders(customer_id);
CREATE INDEX idx_order_staff ON orders(staff_id);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_date ON orders(order_date);
CREATE INDEX idx_order_detail_order ON order_details(order_id);
CREATE INDEX idx_order_detail_item ON order_details(item_id);
CREATE INDEX idx_payment_order ON payments(order_id);
CREATE INDEX idx_payment_customer ON payments(customer_id);
CREATE INDEX idx_payment_status ON payments(status);
CREATE INDEX idx_revenue_date ON revenue_reports(report_date);
GO

-- Insert sample menu items
INSERT INTO menu_items (item_name, description, category, price, available)
VALUES 
('Cơm tấm sườn nướng', 'Cơm tấm kèm sườn nướng, trứng ốp la', 'Cơm', 35000, 1),
('Phở bò', 'Phở bò nước dùng 24h', 'Cơm canh', 45000, 1),
('Bánh mì thịt', 'Bánh mì kèm thịt nướng, pâté', 'Bánh mì', 25000, 1),
('Nước cam', 'Nước cam tươi vắt', 'Thức uống', 15000, 1),
('Cà phê đen', 'Cà phê đen đậm đà', 'Thức uống', 12000, 1),
('Gà rán', 'Gà rán giòn kèm khoai tây chiên', 'Gà rán', 55000, 1),
('Salad trái cây', 'Salad hoa quả tươi ngon', 'Salad', 30000, 1),
('Tiramisu', 'Tiramisu ngon tuyệt vời', 'Tráng miệng', 35000, 1);

-- Insert sample orders
INSERT INTO orders (customer_id, staff_id, order_date, status, total_amount)
VALUES 
(1, 1, '2026-06-02 10:30:00', 'COMPLETED', 100000),
(2, 1, '2026-06-02 11:00:00', 'PENDING', 85000);

-- Insert sample order details
INSERT INTO order_details (order_id, item_id, quantity, unit_price, subtotal)
VALUES 
(1, 1, 2, 35000, 70000),
(1, 4, 1, 15000, 15000),
(1, 5, 1, 12000, 12000),
(2, 3, 2, 25000, 50000),
(2, 5, 1, 12000, 12000);

-- Insert sample payments
INSERT INTO payments (order_id, customer_id, amount, payment_method, status, payment_date)
VALUES 
(1, 1, 100000, 'CASH', 'COMPLETED', '2026-06-02 10:45:00'),
(2, 2, 85000, 'CARD', 'PENDING', NULL);

-- Verify tables
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'dbo' ORDER BY TABLE_NAME;
GO
