package com.siat.dao;

import com.siat.conexion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {
    private static final String LIST_SQL = "SELECT category_id, name FROM categories ORDER BY name";

    public static List<String[]> list() {
        List<String[]> result = new ArrayList<>();
        try (Connection connection = conexion.getConnection();
             PreparedStatement stmt = connection.prepareStatement(LIST_SQL);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                result.add(new String[]{String.valueOf(rs.getInt("category_id")), rs.getString("name")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
