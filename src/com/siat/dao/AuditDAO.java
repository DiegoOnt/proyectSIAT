package com.siat.dao;

import com.siat.conexion;
import com.siat.models.Audit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuditDAO {
    
    public static void log(Integer userId, String action, String targetTable, String targetId, boolean success, String details) {
        String sql = "INSERT INTO audits (user_id, action, target_table, target_id, success, details, ip_address) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String ip = "127.0.0.1";
        try {
            ip = java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception ignored) {}

        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (userId != null) {
                stmt.setInt(1, userId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, action);
            stmt.setString(3, targetTable);
            stmt.setString(4, targetId);
            stmt.setBoolean(5, success);
            stmt.setString(6, details);
            stmt.setString(7, ip);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<Audit> findAll() {
        List<Audit> list = new ArrayList<>();
        String sql = "SELECT a.audit_id, a.user_id, u.username, a.action, a.target_table, a.target_id, a.success, a.details, a.event_time, a.ip_address " +
                     "FROM audits a " +
                     "LEFT JOIN users u ON a.user_id = u.user_id " +
                     "ORDER BY a.event_time DESC";
        try (Connection conn = conexion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Audit audit = new Audit();
                audit.setAuditId(rs.getInt("audit_id"));
                int uid = rs.getInt("user_id");
                audit.setUserId(rs.wasNull() ? null : uid);
                audit.setUsername(rs.getString("username"));
                audit.setAction(rs.getString("action"));
                audit.setTargetTable(rs.getString("target_table"));
                audit.setTargetId(rs.getString("target_id"));
                audit.setSuccess(rs.getBoolean("success"));
                audit.setDetails(rs.getString("details"));
                audit.setEventTime(rs.getString("event_time"));
                audit.setIpAddress(rs.getString("ip_address"));
                list.add(audit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
