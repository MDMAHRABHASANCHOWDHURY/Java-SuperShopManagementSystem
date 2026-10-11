-- Amar Dokan database
-- MySQL Database Schema

CREATE DATABASE IF NOT EXISTS supershop;
USE supershop;

-- ============================
-- USERS
-- ============================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    address VARCHAR(255),
    profile_image_path VARCHAR(500),
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN','CUSTOMER','CASHIER','MANAGER','SUPPLIER') NOT NULL,
    status ENUM('PENDING','ACTIVE','REJECTED','DISABLED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================
-- PRODUCTS
-- ============================
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(150) NOT NULL,
    category VARCHAR(80),
    description VARCHAR(500),
    image_path VARCHAR(500),
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    production_date DATE,
    expiration_date DATE,
    supplier_id INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ============================
-- CART
-- ============================
CREATE TABLE cart (
    cart_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE cart_items (
    cart_item_id INT AUTO_INCREMENT PRIMARY KEY,
    cart_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES cart(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- ============================
-- ORDERS (also used for cashier bills)
-- ============================
CREATE TABLE orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    cashier_id INT,
    invoice_number VARCHAR(50) UNIQUE,
    subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount DECIMAL(10,2) NOT NULL DEFAULT 0,
    vat DECIMAL(10,2) NOT NULL DEFAULT 0,
    grand_total DECIMAL(10,2) NOT NULL DEFAULT 0,
    amount_paid DECIMAL(10,2) NOT NULL DEFAULT 0,
    change_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'RECORDED',
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (cashier_id) REFERENCES users(user_id) ON DELETE SET NULL
);

CREATE TABLE order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- ============================
-- SUPPLIES
-- ============================
CREATE TABLE supplies (
    supply_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    supply_date DATE NOT NULL,
    FOREIGN KEY (supplier_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
);

-- ============================
-- LOGIN ACTIVITY
-- ============================
CREATE TABLE login_activity (
    activity_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    login_time DATETIME,
    logout_time DATETIME,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- ============================
-- USER REQUESTS (registration approval workflow)
-- ============================
CREATE TABLE user_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    requested_role VARCHAR(20) NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    requested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    approved_by INT,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ============================
-- SEED DATA: default admin account (username: admin / password: admin123)
-- ============================
INSERT INTO users (name, username, email, phone, address, password, role, status)
VALUES ('System Admin', 'admin', 'admin@amardokan.com', '0000000000', 'Head Office', 'admin123', 'ADMIN', 'ACTIVE');

-- Bangladesh supershop DEMO catalogue. All prices in BDT; illustrative stock and shelf-life only.

-- Re-running inserts only missing demo accounts/products; it does not reset prices or stock.

START TRANSACTION;

INSERT INTO users (name, username, email, phone, address, password, role, status)
SELECT 'Store Manager (Demo)', 'manager', 'manager@example.com', '01700000001', 'Dhaka store office', 'manager123', 'MANAGER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'manager' OR email = 'manager@example.com');

INSERT INTO users (name, username, email, phone, address, password, role, status)
SELECT 'Store Cashier (Demo)', 'cashier', 'cashier@example.com', '01700000002', 'Dhaka store counter', 'cashier123', 'CASHIER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'cashier' OR email = 'cashier@example.com');

INSERT INTO users (name, username, email, phone, address, password, role, status)
SELECT 'Demo Customer', 'customer', 'customer@example.com', '01700000003', 'Mirpur, Dhaka', 'customer123', 'CUSTOMER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer' OR email = 'customer@example.com');

INSERT INTO users (name, username, email, phone, address, password, role, status)
SELECT 'Dhaka Pantry Distribution (Demo)', 'pantry_supplier', 'pantry@example.com', '01700000004', 'Tejgaon, Dhaka', 'supplier123', 'SUPPLIER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'pantry_supplier' OR email = 'pantry@example.com');

INSERT INTO users (name, username, email, phone, address, password, role, status)
SELECT 'Fresh Foods Distribution (Demo)', 'fresh_supplier', 'fresh@example.com', '01700000005', 'Kawran Bazar, Dhaka', 'supplier123', 'SUPPLIER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'fresh_supplier' OR email = 'fresh@example.com');

INSERT INTO users (name, username, email, phone, address, password, role, status)
SELECT 'Home Care Distribution (Demo)', 'home_supplier', 'home@example.com', '01700000006', 'Uttara, Dhaka', 'supplier123', 'SUPPLIER', 'ACTIVE'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'home_supplier' OR email = 'home@example.com');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Miniket Rice - 5 kg', 'Rice & Grains', 'Clean long-grain rice for everyday meals; sealed 5 kg pack.', 425, 80, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 330 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/rice.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Miniket Rice - 5 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Masoor Dal - 1 kg', 'Rice & Grains', 'Red split lentils for dal and khichuri; 1 kg pack.', 130, 95, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 240 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/lentils.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Masoor Dal - 1 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Whole Wheat Atta - 2 kg', 'Rice & Grains', 'Whole wheat flour for roti and baking; 2 kg pack.', 145, 70, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 180 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/flour.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Whole Wheat Atta - 2 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Soybean Oil - 1 litre', 'Cooking Essentials', 'Refined soybean cooking oil; sealed 1 litre bottle.', 200, 120, DATE_SUB(CURDATE(), INTERVAL 25 DAY), DATE_ADD(CURDATE(), INTERVAL 365 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/oil.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Soybean Oil - 1 litre');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Iodized Salt - 1 kg', 'Cooking Essentials', 'Iodized table salt for daily cooking; 1 kg pack.', 42, 150, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 600 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/salt.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Iodized Salt - 1 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'White Sugar - 1 kg', 'Cooking Essentials', 'Refined white granulated sugar; 1 kg pack.', 125, 110, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 540 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/sugar.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'White Sugar - 1 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Full Cream Milk - 1 litre', 'Dairy & Eggs', 'Full cream UHT milk; 1 litre carton. Refrigerate after opening.', 100, 45, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 90 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/milk.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Full Cream Milk - 1 litre');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Brown Eggs - 12 pcs', 'Dairy & Eggs', 'Farm chicken eggs in a protective 12-piece carton.', 150, 60, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 12 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/eggs.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Brown Eggs - 12 pcs');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Plain Yogurt - 500 g', 'Dairy & Eggs', 'Unsweetened plain yogurt; 500 g cup. Keep refrigerated.', 130, 8, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/yogurt.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Plain Yogurt - 500 g');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Butter - 200 g', 'Dairy & Eggs', 'Creamy dairy butter for bread and baking; 200 g pack. Keep chilled.', 220, 10, DATE_SUB(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 90 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/butter.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Butter - 200 g');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Potatoes - 1 kg', 'Vegetables', 'Fresh potatoes; 1 kg selling unit. Store in a cool dry place.', 55, 180, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/potatoes.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Potatoes - 1 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Red Onions - 1 kg', 'Vegetables', 'Fresh red onions; 1 kg selling unit.', 90, 160, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 21 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/onions.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Red Onions - 1 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Tomatoes - 1 kg', 'Vegetables', 'Fresh ripe tomatoes; 1 kg selling unit. Keep chilled.', 80, 55, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 5 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/tomatoes.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Tomatoes - 1 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Bananas - 6 pcs', 'Fruits', 'Ripe bananas; bunch of 6. Keep at room temperature.', 60, 75, DATE_SUB(CURDATE(), INTERVAL 1 DAY), DATE_ADD(CURDATE(), INTERVAL 4 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/bananas.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Bananas - 6 pcs');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Red Apples - 1 kg', 'Fruits', 'Fresh red apples; 1 kg selling unit. Keep chilled.', 280, 40, DATE_SUB(CURDATE(), INTERVAL 2 DAY), DATE_ADD(CURDATE(), INTERVAL 14 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/apples.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Red Apples - 1 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Whole Chicken - 1 kg', 'Meat & Fish', 'Dressed chicken; 1 kg selling unit. Keep refrigerated and cook thoroughly.', 230, 35, DATE_SUB(CURDATE(), INTERVAL 0 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/chicken.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Whole Chicken - 1 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Rohu Fish - 1 kg', 'Meat & Fish', 'Clean fresh rohu fish; 1 kg selling unit. Keep refrigerated.', 360, 25, DATE_SUB(CURDATE(), INTERVAL 0 DAY), DATE_ADD(CURDATE(), INTERVAL 2 DAY), (SELECT user_id FROM users WHERE username = 'fresh_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/fish.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Rohu Fish - 1 kg');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Digestive Biscuits - 200 g', 'Snacks', 'Crunchy digestive biscuits; sealed 200 g pack.', 70, 90, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 180 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/biscuits.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Digestive Biscuits - 200 g');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Instant Noodles - 80 g', 'Snacks', 'Single-serving instant noodles; 80 g pack.', 25, 200, DATE_SUB(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 240 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/noodles.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Instant Noodles - 80 g');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Black Tea - 200 g', 'Beverages', 'Loose black tea for a rich daily brew; 200 g pack.', 120, 65, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 365 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/tea.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Black Tea - 200 g');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Orange Juice - 1 litre', 'Beverages', 'Orange juice; 1 litre carton. Refrigerate after opening.', 150, 50, DATE_SUB(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 150 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/juice.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Orange Juice - 1 litre');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Drinking Water - 1.5 litres', 'Beverages', 'Sealed purified drinking water; 1.5 litre bottle.', 35, 240, DATE_SUB(CURDATE(), INTERVAL 20 DAY), DATE_ADD(CURDATE(), INTERVAL 365 DAY), (SELECT user_id FROM users WHERE username = 'pantry_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/water.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Drinking Water - 1.5 litres');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Bath Soap - 100 g', 'Personal & Home Care', 'Mild bath soap; individually wrapped 100 g bar.', 55, 100, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 730 DAY), (SELECT user_id FROM users WHERE username = 'home_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/soap.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Bath Soap - 100 g');

INSERT INTO products (product_name, category, description, price, stock_quantity, production_date, expiration_date, supplier_id, image_path)
SELECT 'Laundry Detergent - 1 kg', 'Personal & Home Care', 'Laundry detergent powder; sealed 1 kg pouch.', 155, 85, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_ADD(CURDATE(), INTERVAL 730 DAY), (SELECT user_id FROM users WHERE username = 'home_supplier' AND role = 'SUPPLIER' LIMIT 1), 'images/products/detergent.png'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE product_name = 'Laundry Detergent - 1 kg');

COMMIT;
