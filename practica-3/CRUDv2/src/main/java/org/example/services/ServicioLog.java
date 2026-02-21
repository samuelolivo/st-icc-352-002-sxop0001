package org.example.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServicioLog {

    private static final String URL = "jdbc:h2:./practica3;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "sa";


    public static void registrarAcceso(String username) {

        String sqlCrearTabla = "CREATE TABLE IF NOT EXISTS LOG_SESIONES (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "usuario VARCHAR(100), " +
                "fecha_acceso VARCHAR(100))";

        String sqlInsertar = "INSERT INTO LOG_SESIONES (usuario, fecha_acceso) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            try (PreparedStatement createStmt = conn.prepareStatement(sqlCrearTabla)) {
                createStmt.execute();
            }


            try (PreparedStatement insertStmt = conn.prepareStatement(sqlInsertar)) {
                String fechaActual = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                insertStmt.setString(1, username);
                insertStmt.setString(2, fechaActual);

                insertStmt.executeUpdate();
                System.out.println("[JDBC LOG] Usuario '" + username + "' ha iniciado sesión correctamente.");
            }

        } catch (SQLException e) {
            System.err.println("[JDBC ERROR] No se pudo registrar el log: " + e.getMessage());
        }
    }
}