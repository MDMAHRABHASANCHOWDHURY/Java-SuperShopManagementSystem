package dao;

import database.DatabaseConnection;
import model.Supply;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplyDAO {

    /** Records a supply entry and increases the product's stock in one transaction. */
    public boolean addSupply(Supply supply) {
        String insertSql = "INSERT INTO supplies (supplier_id, product_id, quantity, supply_date) VALUES (?, ?, ?, ?)";
        String stockSql = "UPDATE products SET stock_quantity = stock_quantity + ? WHERE product_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setInt(1, supply.getSupplierId());
                ps.setInt(2, supply.getProductId());
                ps.setInt(3, supply.getQuantity());
                ps.setDate(4, Date.valueOf(supply.getSupplyDate()));
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(stockSql)) {
                ps.setInt(1, supply.getQuantity());
                ps.setInt(2, supply.getProductId());
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("addSupply error: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            return false;
        } finally {
            try { if (conn != null) conn.setAutoCommit(true); } catch (SQLException ex) { /* ignore */ }
        }
    }

    public List<Supply> getSupplyHistory(int supplierId) {
        List<Supply> list = new ArrayList<>();
        String sql = "SELECT s.*, p.product_name FROM supplies s " +
                     "JOIN products p ON s.product_id = p.product_id " +
                     "WHERE s.supplier_id = ? ORDER BY s.supply_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("getSupplyHistory error: " + e.getMessage());
        }
        return list;
    }

    public List<Supply> getAllSupplies() {
        List<Supply> list = new ArrayList<>();
        String sql = "SELECT s.*, p.product_name FROM supplies s " +
                     "JOIN products p ON s.product_id = p.product_id ORDER BY s.supply_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("getAllSupplies error: " + e.getMessage());
        }
        return list;
    }

    private Supply mapRow(ResultSet rs) throws SQLException {
        Supply s = new Supply();
        s.setSupplyId(rs.getInt("supply_id"));
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setProductId(rs.getInt("product_id"));
        s.setProductName(rs.getString("product_name"));
        s.setQuantity(rs.getInt("quantity"));
        s.setSupplyDate(rs.getDate("supply_date").toLocalDate());
        return s;
    }
}
