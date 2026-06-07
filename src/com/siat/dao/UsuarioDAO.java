package com.siat.dao;

import com.siat.conexion;
import com.siat.models.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    private static final String AUTH_SQL = "SELECT u.user_id, u.username, u.full_name, r.name AS role_name, r.level " +
            "FROM users u JOIN roles r ON u.role_id = r.role_id " +
            "WHERE u.username = ? AND u.password_hash = ? AND u.status = 'ACTIVE'";

    public static Usuario authenticate(String username, String password) {
        try (Connection connection = conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(AUTH_SQL)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario user = new Usuario();
                    user.setUserId(rs.getInt("user_id"));
                    user.setUsername(rs.getString("username"));
                    user.setFullName(rs.getString("full_name"));
                    user.setRoleName(rs.getString("role_name"));
                    user.setRoleLevel(rs.getInt("level"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
