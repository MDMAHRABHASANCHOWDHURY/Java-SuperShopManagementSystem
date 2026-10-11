package dao;

import database.DatabaseConnection;
import model.Order;
import model.OrderItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    /** Saves an order plus its items in a single transaction and returns the generated order id, or -1 on failure. */
    public int saveOrder(Order order) {
        String orderSql = "INSERT INTO orders (customer_id, cashier_id, invoice_number, subtotal, discount, vat, " +
                          "grand_total, amount_paid, change_amount, payment_method, payment_status) " +
                          "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, total_price) " +
                         "VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int orderId;
            try (PreparedStatement ps = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
                if (order.getCustomerId() != null) ps.setInt(1, order.getCustomerId()); else ps.setNull(1, Types.INTEGER);
                if (order.getCashierId() != null) ps.setInt(2, order.getCashierId()); else ps.setNull(2, Types.INTEGER);
                ps.setString(3, order.getInvoiceNumber());
                ps.setDouble(4, order.getSubtotal());
                ps.setDouble(5, order.getDiscount());
                ps.setDouble(6, order.getVat());
                ps.setDouble(7, order.getGrandTotal());
                ps.setDouble(8, order.getAmountPaid());
                ps.setDouble(9, order.getChangeAmount());
                ps.setString(10, order.getPaymentMethod());
                ps.setString(11, order.getPaymentStatus());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) throw new SQLException("No order id generated");
                    orderId = keys.getInt(1);
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                for (OrderItem item : order.getItems()) {
                    ps.setInt(1, orderId);
                    ps.setInt(2, item.getProductId());
                    ps.setInt(3, item.getQuantity());
                    ps.setDouble(4, item.getUnitPrice());
                    ps.setDouble(5, item.getTotalPrice());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Deduct stock for each item
            String stockSql = "UPDATE products SET stock_quantity = stock_quantity - ? " +
                    "WHERE product_id = ? AND stock_quantity >= ? " +
                    "AND (expiration_date IS NULL OR expiration_date >= CURDATE())";
            try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                for (OrderItem item : order.getItems()) {
                    ps.setInt(1, item.getQuantity());
                    ps.setInt(2, item.getProductId());
                    ps.setInt(3, item.getQuantity());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException("Not enough stock or expired product: " + item.getProductId());
                    }
                }
            }

            conn.commit();
            return orderId;
        } catch (SQLException | RuntimeException e) {
            System.err.println("saveOrder error: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            return -1;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
        }
    }

    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE customer_id = ? ORDER BY order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapOrderRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("getOrdersByCustomer error: " + e.getMessage());
        }
        return list;
    }

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY order_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapOrderRow(rs));
        } catch (SQLException e) {
            System.err.println("getAllOrders error: " + e.getMessage());
        }
        return list;
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT oi.*, p.product_name FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.product_id WHERE oi.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id"));
                    item.setOrderId(rs.getInt("order_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    item.setTotalPrice(rs.getDouble("total_price"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("getOrderItems error: " + e.getMessage());
        }
        return list;
    }

    private Order mapOrderRow(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setOrderId(rs.getInt("order_id"));
        int customerId = rs.getInt("customer_id");
        o.setCustomerId(rs.wasNull() ? null : customerId);
        int cashierId = rs.getInt("cashier_id");
        o.setCashierId(rs.wasNull() ? null : cashierId);
        o.setInvoiceNumber(rs.getString("invoice_number"));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setDiscount(rs.getDouble("discount"));
        o.setVat(rs.getDouble("vat"));
        o.setGrandTotal(rs.getDouble("grand_total"));
        o.setAmountPaid(rs.getDouble("amount_paid"));
        o.setChangeAmount(rs.getDouble("change_amount"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setPaymentStatus(rs.getString("payment_status"));
        Timestamp ts = rs.getTimestamp("order_date");
        o.setOrderDate(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return o;
    }
}
