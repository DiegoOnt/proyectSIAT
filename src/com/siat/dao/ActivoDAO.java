package com.siat.dao;

import com.siat.conexion;
import com.siat.models.Activo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class ActivoDAO {
    // Leer desde la vista que agrega métricas y alertas pendientes
    private static final String FIND_ALL_SQL = "SELECT sku, name, category, location, status, lifecycle_stage, purchase_date, decommission_date, lifecycle_days, maintenance, quantity, cost, created_by, responsible_id, unresolved_alerts FROM vw_asset_summary ORDER BY name";

    public static List<Activo> findAll() {
        List<Activo> assets = new ArrayList<>();
        try (Connection connection = conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Activo activo = new Activo();
                activo.setSku(rs.getString("sku"));
                activo.setName(rs.getString("name"));
                activo.setCategory(rs.getString("category"));
                activo.setLocation(rs.getString("location"));
                activo.setStatus(rs.getString("status"));
                activo.setLifecycleStage(rs.getString("lifecycle_stage"));
                activo.setPurchaseDate(rs.getString("purchase_date"));
                activo.setDecommissionDate(rs.getString("decommission_date"));
                activo.setLifecycleDays(rs.getInt("lifecycle_days"));
                activo.setMaintenance(rs.getString("maintenance"));
                activo.setQuantity(rs.getInt("quantity"));
                activo.setCost(rs.getDouble("cost"));
                activo.setCreatedBy(rs.getInt("created_by"));
                activo.setResponsibleId(rs.getInt("responsible_id"));
                activo.setUnresolvedAlerts(rs.getInt("unresolved_alerts"));
                assets.add(activo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return assets;
    }

    public static boolean insert(Activo activo, int creatorUserId) {
        String sql = "INSERT INTO assets (sku, name, category_id, location, purchase_date, decommission_date, maintenance, quantity, warranty_expiry, cost, created_by, responsible_id, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW())";
        try (Connection connection = conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activo.getSku());
            stmt.setString(2, activo.getName());
            // category must be provided as category_id in 'category' field temporarily
            stmt.setInt(3, Integer.parseInt(activo.getCategory()));
            stmt.setString(4, activo.getLocation());
            stmt.setDate(5, activo.getPurchaseDate() != null ? Date.valueOf(activo.getPurchaseDate()) : null);
            stmt.setDate(6, activo.getDecommissionDate() != null ? Date.valueOf(activo.getDecommissionDate()) : null);
            stmt.setString(7, activo.getMaintenance() != null ? activo.getMaintenance() : "NO");
            stmt.setInt(8, activo.getQuantity());
            stmt.setDate(9, activo.getWarrantyExpiry() != null ? Date.valueOf(activo.getWarrantyExpiry()) : null);
            stmt.setDouble(10, activo.getCost());
            stmt.setInt(11, creatorUserId);
            stmt.setInt(12, creatorUserId); // responsible_id set equal to creator by default
            int rows = stmt.executeUpdate();
            return rows == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean existsBySku(String sku) {
        String sql = "SELECT 1 FROM assets WHERE sku = ? LIMIT 1";
        try (Connection connection = conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, sku);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean update(Activo activo, int userId) {
        String sql = "UPDATE assets SET name=?, category_id=?, location=?, purchase_date=?, decommission_date=?, maintenance=?, quantity=?, warranty_expiry=?, cost=?, updated_by=?, updated_at=NOW() WHERE sku=?";
        try (Connection connection = conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activo.getName());
            stmt.setInt(2, Integer.parseInt(activo.getCategory()));
            stmt.setString(3, activo.getLocation());
            stmt.setDate(4, activo.getPurchaseDate() != null ? java.sql.Date.valueOf(activo.getPurchaseDate()) : null);
            stmt.setDate(5, activo.getDecommissionDate() != null ? java.sql.Date.valueOf(activo.getDecommissionDate()) : null);
            stmt.setString(6, activo.getMaintenance() != null ? activo.getMaintenance() : "NO");
            stmt.setInt(7, activo.getQuantity());
            stmt.setDate(8, activo.getWarrantyExpiry() != null ? java.sql.Date.valueOf(activo.getWarrantyExpiry()) : null);
            stmt.setDouble(9, activo.getCost());
            stmt.setInt(10, userId);
            stmt.setString(11, activo.getSku());
            int rows = stmt.executeUpdate();
            return rows == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean delete(String sku) {
        String deleteAlerts = "DELETE FROM alerts WHERE asset_id = (SELECT asset_id FROM assets WHERE sku = ?)";
        String deleteHistory = "DELETE FROM asset_history WHERE asset_id = (SELECT asset_id FROM assets WHERE sku = ?)";
        String deleteAsset = "DELETE FROM assets WHERE sku = ?";
        
        try (Connection connection = conexion.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement stmtAlerts = connection.prepareStatement(deleteAlerts);
                 PreparedStatement stmtHistory = connection.prepareStatement(deleteHistory);
                 PreparedStatement stmtAsset = connection.prepareStatement(deleteAsset)) {
                
                stmtAlerts.setString(1, sku);
                stmtAlerts.executeUpdate();
                
                stmtHistory.setString(1, sku);
                stmtHistory.executeUpdate();
                
                stmtAsset.setString(1, sku);
                int rows = stmtAsset.executeUpdate();
                
                connection.commit();
                return rows == 1;
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
