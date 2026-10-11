package dao;

import database.DatabaseConnection;
import model.Cart;
import model.CartItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CartDAO {

    /** Gets the customer's open cart, creating one if it doesn't exist. */
    public Cart getOrCreateCart(int customerId) {
        String find = "SELECT * FROM cart WHERE customer_id = ? ORDER BY cart_id DESC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(find)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cart cart = new Cart();
                    cart.setCartId(rs.getInt("cart_id"));
                    cart.setCustomerId(rs.getInt("customer_id"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    cart.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
                    cart.setItems(getCartItems(cart.getCartId()));
                    return cart;
                }
            }
        } catch (SQLException e) {
            System.err.println("getOrCreateCart find error: " + e.getMessage());
        }

        String insert = "INSERT INTO cart (customer_id) VALUES (?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    Cart cart = new Cart();
                    cart.setCartId(keys.getInt(1));
                    cart.setCustomerId(customerId);
                    cart.setCreatedAt(LocalDateTime.now());
                    return cart;
                }
            }
        } catch (SQLException e) {
            System.err.println("getOrCreateCart insert error: " + e.getMessage());
        }
        return null;
    }

    public List<CartItem> getCartItems(int cartId) {
        List<CartItem> list = new ArrayList<>();
        String sql = "SELECT ci.*, p.product_name FROM cart_items ci " +
                     "JOIN products p ON ci.product_id = p.product_id WHERE ci.cart_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CartItem item = new CartItem();
                    item.setCartItemId(rs.getInt("cart_item_id"));
                    item.setCartId(rs.getInt("cart_id"));
                    item.setProductId(rs.getInt("product_id"));
                    item.setProductName(rs.getString("product_name"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unit_price"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("getCartItems error: " + e.getMessage());
        }
        return list;
    }

    public boolean addItem(int cartId, int productId, int quantity, double unitPrice) {
        // If item already in cart, increase quantity instead of duplicating the row
        String check = "SELECT cart_item_id, quantity FROM cart_items WHERE cart_id = ? AND product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(check)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int newQty = rs.getInt("quantity") + quantity;
                    return updateItemQuantity(rs.getInt("cart_item_id"), newQty);
                }
            }
        } catch (SQLException e) {
            System.err.println("addItem check error: " + e.getMessage());
        }

        String insert = "INSERT INTO cart_items (cart_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insert)) {
            ps.setInt(1, cartId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setDouble(4, unitPrice);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("addItem insert error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateItemQuantity(int cartItemId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, cartItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateItemQuantity error: " + e.getMessage());
            return false;
        }
    }

    public boolean removeItem(int cartItemId) {
        String sql = "DELETE FROM cart_items WHERE cart_item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartItemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("removeItem error: " + e.getMessage());
            return false;
        }
    }

    public boolean clearCart(int cartId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, cartId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("clearCart error: " + e.getMessage());
            return false;
        }
    }
}
