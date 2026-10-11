-- Run ONCE on an existing Amar Dokan / supershop database.
-- Do not run this after importing the latest supershop.sql (already includes these columns).
USE supershop;
ALTER TABLE orders
    ADD COLUMN payment_method VARCHAR(30) NOT NULL DEFAULT 'CASH',
    ADD COLUMN payment_status VARCHAR(20) NOT NULL DEFAULT 'RECORDED';
