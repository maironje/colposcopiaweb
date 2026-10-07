package com.endofertil.colposcopia.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fábrica de conexiones JDBC hacia la base de datos {@code bd_colposcopia}.
 * Ajuste USUARIO y CLAVE según su instalación local de MySQL.
 */
public final class Conexion {

    private static final String URL = "jdbc:mysql://localhost:3306/bd_colposcopia"
            + "?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String CLAVE = "sistemas";

    static {
        // En aplicaciones web el driver debe registrarse explícitamente
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontró el driver de MySQL: " + e.getMessage());
        }
    }

    private Conexion() {
        // Clase utilitaria: no se instancia
    }

    /**
     * Abre una nueva conexión. Quien la usa debe cerrarla (try-with-resources).
     *
     * @return conexión activa a la base de datos
     * @throws SQLException si no es posible conectarse
     */
    public static Connection obtener() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
