-- PHASE 1: DATABASE UPGRADES - Canteen Management System
-- Add new tables and columns for enhanced features

USE canteen_db;
GO

-- ============================================
-- 1. ADD COLUMNS TO EXISTING TABLES
-- ============================================

-- Add rating/review related columns to customers if not exists
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='customers' AND COLUMN_NAME='total_spent')
BEGIN
    ALTER TABLE customers ADD total_spent DECIMAL(12,2) DEFAULT 0.00;
END;

-- Add notes and options columns to order_details
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='order_details' AND COLUMN_NAME='special_notes')
BEGIN
    ALTER TABLE order_details ADD special_notes VARCHAR(500);
END;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='order_details' AND COLUMN_NAME='options_json')
BEGIN
    ALTER TABLE order_details ADD options_json VARCHAR(1000);
END;

-- Add more info to menu_items
IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='menu_items' AND COLUMN_NAME='rating')
BEGIN
    ALTER TABLE menu_items ADD rating DECIMAL(3,2) DEFAULT 5.00;
END;

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='menu_items' AND COLUMN_NAME='review_count')
BEGIN
    ALTER TABLE menu_items ADD review_count INT DEFAULT 0;
END;

GO

-- ============================================
-- 2. CREATE REVIEW TABLE
-- ============================================

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='reviews')
BEGIN
    CREATE TABLE reviews (
        review_id INT PRIMARY KEY IDENTITY(1, 1),
        item_id INT NOT NULL,
        customer_id INT NOT NULL,
        order_id INT NOT NULL,
        rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
        comment TEXT,
        verified_purchase BIT DEFAULT 1,
        helpful_count INT DEFAULT 0,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (item_id) REFERENCES menu_items(item_id),
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id),
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE
    );
    
    CREATE INDEX idx_review_item ON reviews(item_id);
    CREATE INDEX idx_review_customer ON reviews(customer_id);
    CREATE INDEX idx_review_rating ON reviews(rating);
END;

GO

-- ============================================
-- 3. CREATE COUPONS TABLE
-- ============================================

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='coupons')
BEGIN
    CREATE TABLE coupons (
        coupon_id INT PRIMARY KEY IDENTITY(1, 1),
        coupon_code VARCHAR(50) UNIQUE NOT NULL,
        description VARCHAR(200),
        discount_type VARCHAR(20) CHECK (discount_type IN ('PERCENT', 'FIXED')), -- Phần trăm hoặc tiền cố định
        discount_value DECIMAL(10,2) NOT NULL,
        min_order_amount DECIMAL(10,2) DEFAULT 0,
        max_discount DECIMAL(10,2),
        max_uses INT DEFAULT 0, -- 0 = unlimited
        used_count INT DEFAULT 0,
        valid_from DATETIME,
        valid_to DATETIME,
        status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'EXPIRED', 'DISABLED')),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );
    
    CREATE INDEX idx_coupon_code ON coupons(coupon_code);
    CREATE INDEX idx_coupon_status ON coupons(status);
    CREATE INDEX idx_coupon_valid ON coupons(valid_from, valid_to);
END;

GO

-- ============================================
-- 4. CREATE ORDER_COUPONS TABLE
-- ============================================

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='order_coupons')
BEGIN
    CREATE TABLE order_coupons (
        order_coupon_id INT PRIMARY KEY IDENTITY(1, 1),
        order_id INT NOT NULL,
        coupon_id INT NOT NULL,
        discount_amount DECIMAL(10,2) NOT NULL,
        applied_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
        FOREIGN KEY (coupon_id) REFERENCES coupons(coupon_id)
    );
    
    CREATE INDEX idx_order_coupon_order ON order_coupons(order_id);
    CREATE INDEX idx_order_coupon_coupon ON order_coupons(coupon_id);
END;

GO

-- ============================================
-- 5. CREATE MENU_OPTIONS TABLE (Size, Topping, etc)
-- ============================================

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='menu_options')
BEGIN
    CREATE TABLE menu_options (
        option_id INT PRIMARY KEY IDENTITY(1, 1),
        item_id INT NOT NULL,
        option_type VARCHAR(50) NOT NULL, -- 'SIZE', 'TOPPING', 'EXTRA', etc
        option_name VARCHAR(100) NOT NULL, -- 'Small', 'Medium', 'Large', 'Cheese', etc
        price_adjustment DECIMAL(10,2) DEFAULT 0,
        available BIT DEFAULT 1,
        created_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (item_id) REFERENCES menu_items(item_id) ON DELETE CASCADE
    );
    
    CREATE INDEX idx_option_item ON menu_options(item_id);
    CREATE INDEX idx_option_type ON menu_options(option_type);
END;

GO

-- ============================================
-- 6. CREATE CART_ITEMS TABLE (for persistent cart)
-- ============================================

IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='cart_items')
BEGIN
    CREATE TABLE cart_items (
        cart_id INT PRIMARY KEY IDENTITY(1, 1),
        customer_id INT NOT NULL,
        item_id INT NOT NULL,
        quantity INT NOT NULL DEFAULT 1,
        special_notes VARCHAR(500),
        options_json VARCHAR(1000),
        added_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE,
        FOREIGN KEY (item_id) REFERENCES menu_items(item_id) ON DELETE CASCADE
    );
    
    CREATE INDEX idx_cart_customer ON cart_items(customer_id);
    CREATE INDEX idx_cart_item ON cart_items(item_id);
END;

GO

-- ============================================
-- 7. INSERT SAMPLE DATA
-- ============================================

-- Sample Reviews
INSERT INTO reviews (item_id, customer_id, order_id, rating, comment, verified_purchase)
VALUES
(1, 1, 1, 5, 'Cơm tấm rất ngon, sườn nướng thơm phức!', 1),
(1, 2, 2, 4, 'Ngon nhưng hơi ít nước mắm', 1),
(2, 1, 1, 5, 'Phở bò chuẩn, nước dùng đậm đà', 1);

-- Update menu_items with average ratings
UPDATE menu_items SET rating = 4.67, review_count = 3 WHERE item_id = 1;
UPDATE menu_items SET rating = 5.00, review_count = 1 WHERE item_id = 2;

-- Sample Coupons
INSERT INTO coupons (coupon_code, description, discount_type, discount_value, min_order_amount, valid_from, valid_to, status)
VALUES
('WELCOME10', 'Giảm 10% cho khách hàng mới', 'PERCENT', 10, 50000, GETDATE(), DATEADD(DAY, 30, GETDATE()), 'ACTIVE'),
('SUMMER20K', 'Giảm 20,000đ cho đơn từ 100,000đ', 'FIXED', 20000, 100000, GETDATE(), DATEADD(DAY, 30, GETDATE()), 'ACTIVE'),
('LUNCH15', 'Giảm 15% vào giờ trưa (10-14h)', 'PERCENT', 15, 30000, GETDATE(), DATEADD(DAY, 60, GETDATE()), 'ACTIVE');

-- Sample Menu Options (Size)
INSERT INTO menu_options (item_id, option_type, option_name, price_adjustment)
VALUES
(1, 'SIZE', 'Nhỏ', 0),
(1, 'SIZE', 'Vừa', 5000),
(1, 'SIZE', 'Lớn', 10000);

-- Sample Menu Options (Topping for drink)
INSERT INTO menu_options (item_id, option_type, option_name, price_adjustment)
VALUES
(4, 'TOPPING', 'Bình thường', 0),
(4, 'TOPPING', 'Nhiều đường', 2000),
(4, 'TOPPING', 'Ít đường', 0);

GO

-- ============================================
-- 8. VERIFY COMPLETION
-- ============================================

PRINT '========== PHASE 1 UPGRADE COMPLETED =========';
PRINT 'Tables created:';
SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('reviews', 'coupons', 'order_coupons', 'menu_options', 'cart_items');
PRINT '';
PRINT 'New columns added to existing tables';
GO
