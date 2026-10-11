package dao;

import database.DatabaseConnection;
import model.Product;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public boolean addProduct(Product p) {
        String sql = "INSERT INTO products (product_name, category, description, price, stock_quantity, " +
                     "production_date, expiration_date, supplier_id, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getStockQuantity());
            ps.setDate(6, p.getProductionDate() != null ? Date.valueOf(p.getProductionDate()) : null);
            ps.setDate(7, p.getExpirationDate() != null ? Date.valueOf(p.getExpirationDate()) : null);
            if (p.getSupplierId() != null) ps.setInt(8, p.getSupplierId()); else ps.setNull(8, Types.INTEGER);
            ps.setString(9, p.getImagePath());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("addProduct error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateProduct(Product p) {
        String sql = "UPDATE products SET product_name=?, category=?, description=?, price=?, stock_quantity=?, " +
                     "production_date=?, expiration_date=?, supplier_id=?, image_path=? WHERE product_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getProductName());
            ps.setString(2, p.getCategory());
            ps.setString(3, p.getDescription());
            ps.setDouble(4, p.getPrice());
            ps.setInt(5, p.getStockQuantity());
            ps.setDate(6, p.getProductionDate() != null ? Date.valueOf(p.getProductionDate()) : null);
            ps.setDate(7, p.getExpirationDate() != null ? Date.valueOf(p.getExpirationDate()) : null);
            if (p.getSupplierId() != null) ps.setInt(8, p.getSupplierId()); else ps.setNull(8, Types.INTEGER);
            ps.setString(9, p.getImagePath());
            ps.setInt(10, p.getProductId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("updateProduct error: " + e.getMessage());
            return false;
        }
    }

    public boolean removeProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("removeProduct error: " + e.getMessage());
            return false;
        }
    }

    /** Adjusts stock by delta (positive to add supply, negative to deduct on sale). */
    public boolean adjustStock(int productId, int delta) {
        String sql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("adjustStock error: " + e.getMessage());
            return false;
        }
    }

    public Product findById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("findById error: " + e.getMessage());
        }
        return null;
    }

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY product_name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getAllProducts error: " + e.getMessage());
        }
        return list;
    }

    public List<Product> searchProducts(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE product_name LIKE ? OR category LIKE ? ORDER BY product_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("searchProducts error: " + e.getMessage());
        }
        return list;
    }

    public List<Product> getLowStockProducts(int threshold) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock_quantity <= ? ORDER BY stock_quantity";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("getLowStockProducts error: " + e.getMessage());
        }
        return list;
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setCategory(rs.getString("category"));
        p.setDescription(rs.getString("description"));
        p.setImagePath(rs.getString("image_path"));
        p.setPrice(rs.getDouble("price"));
        p.setStockQuantity(rs.getInt("stock_quantity"));
        Date prod = rs.getDate("production_date");
        p.setProductionDate(prod != null ? prod.toLocalDate() : null);
        Date exp = rs.getDate("expiration_date");
        p.setExpirationDate(exp != null ? exp.toLocalDate() : null);
        int supplierId = rs.getInt("supplier_id");
        p.setSupplierId(rs.wasNull() ? null : supplierId);
        Timestamp ts = rs.getTimestamp("created_at");
        p.setCreatedAt(ts != null ? ts.toLocalDateTime() : LocalDateTime.now());
        return p;
    }
}
