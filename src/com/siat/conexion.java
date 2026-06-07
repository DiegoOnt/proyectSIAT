package com.siat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexion {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    private static final String DATABASE = "siat_inventario";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static String getConfig(String propName, String envName, String defaultValue) {
        String value = System.getProperty(propName);
        if (value != null && !value.isBlank()) {
            return value;
        }
        value = System.getenv(envName);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    public static Connection getConnection() throws SQLException {
        String host = getConfig("db.host", "DB_HOST", HOST);
        String port = getConfig("db.port", "DB_PORT", PORT);
        String database = getConfig("db.name", "DB_NAME", DATABASE);
        String user = getConfig("db.user", "DB_USER", USER);
        String password = getConfig("db.password", "DB_PASSWORD", PASSWORD);

        String url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8",
            host, port, database
        );
        return DriverManager.getConnection(url, user, password);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Conectado a MySQL OK");
        } catch (SQLException e) {
            System.err.println("Error de conexion: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
