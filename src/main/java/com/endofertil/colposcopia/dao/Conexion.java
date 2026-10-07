package com.endofertil.colposcopia.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexiones JDBC hacia la base de datos {@code bd_colposcopia}.
 *
 * <p>Los datos de conexión se leen de {@code src/main/resources/db.properties},
 * que está excluido de Git para no publicar la contraseña. Use
 * {@code db.properties.ejemplo} como plantilla.</p>
 */
public final class Conexion {

    private static final String ARCHIVO = "/db.properties";
    private static final String URL;
    private static final String USUARIO;
    private static final String CLAVE;

    static {
        // En aplicaciones web el driver debe registrarse explícitamente
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError("No se encontró el driver de MySQL: " + e.getMessage());
        }

        Properties datos = new Properties();
        try (InputStream entrada = Conexion.class.getResourceAsStream(ARCHIVO)) {
            if (entrada == null) {
                throw new ExceptionInInitializerError("Falta el archivo db.properties en src/main/resources. "
                        + "Cópielo desde db.properties.ejemplo y escriba su contraseña de MySQL.");
            }
            datos.load(entrada);
        } catch (IOException e) {
            throw new ExceptionInInitializerError("No se pudo leer db.properties: " + e.getMessage());
        }
        URL = datos.getProperty("db.url");
        USUARIO = datos.getProperty("db.usuario");
        CLAVE = datos.getProperty("db.clave", "");
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
