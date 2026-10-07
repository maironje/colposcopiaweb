package com.endofertil.colposcopia.dao;

import com.endofertil.colposcopia.modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para la tabla {@code usuario}.
 * Todas las consultas usan PreparedStatement para evitar inyección SQL.
 */
public class UsuarioDAO {

    /** Código MySQL: valor duplicado en un índice UNIQUE (nombre de usuario). */
    public static final int ERROR_DUPLICADO = 1062;

    private static final String COLUMNAS = "id_usuario, usuario, password_hash, rol, estado";

    private static final String SQL_LISTAR = "SELECT " + COLUMNAS + " FROM usuario "
            + "WHERE usuario LIKE ? ORDER BY estado DESC, usuario";

    private static final String SQL_BUSCAR_POR_ID = "SELECT " + COLUMNAS
            + " FROM usuario WHERE id_usuario = ?";

    private static final String SQL_BUSCAR_POR_USUARIO = "SELECT " + COLUMNAS
            + " FROM usuario WHERE usuario = ?";

    private static final String SQL_INSERTAR = "INSERT INTO usuario (usuario, password_hash, rol, estado) "
            + "VALUES (?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR = "UPDATE usuario SET usuario = ?, rol = ?, estado = ? "
            + "WHERE id_usuario = ?";

    private static final String SQL_CAMBIAR_CLAVE = "UPDATE usuario SET password_hash = ? WHERE id_usuario = ?";

    private static final String SQL_CAMBIAR_ESTADO = "UPDATE usuario SET estado = ? WHERE id_usuario = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM usuario WHERE id_usuario = ?";

    private static final String SQL_CONTAR_ADMIN_ACTIVOS = "SELECT COUNT(*) FROM usuario "
            + "WHERE rol = 'Administrador' AND estado = 1";

    /**
     * Lista las cuentas cuyo nombre de usuario contenga el filtro.
     *
     * @param filtro texto a buscar; nulo o vacío devuelve todas
     * @return cuentas activas primero, luego por nombre
     * @throws SQLException si falla la consulta
     */
    public List<Usuario> listar(String filtro) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR)) {
            ps.setString(1, "%" + (filtro == null ? "" : filtro.trim()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapear(rs));
                }
            }
        }
        return usuarios;
    }

    /**
     * @param id identificador de la cuenta
     * @return la cuenta o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Usuario buscarPorId(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Busca la cuenta con la que se intenta iniciar sesión.
     *
     * @param usuario nombre de usuario exacto
     * @return la cuenta (incluido el hash) o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Usuario buscarPorUsuario(String usuario) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR_POR_USUARIO)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Registra una cuenta nueva. El hash de la contraseña debe venir ya calculado.
     *
     * @param u cuenta con {@code passwordHash} asignado
     * @return id generado
     * @throws SQLException si falla la inserción (p. ej. usuario duplicado)
     */
    public int insertar(Usuario u) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsuario());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getRol());
            ps.setBoolean(4, u.isActivo());
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    u.setIdUsuario(llaves.getInt(1));
                }
            }
        }
        return u.getIdUsuario();
    }

    /**
     * Actualiza nombre de usuario, rol y estado. La contraseña se cambia con {@link #cambiarClave}.
     *
     * @param u cuenta con su id
     * @return {@code true} si se actualizó
     * @throws SQLException si falla la actualización
     */
    public boolean actualizar(Usuario u) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            ps.setString(1, u.getUsuario());
            ps.setString(2, u.getRol());
            ps.setBoolean(3, u.isActivo());
            ps.setInt(4, u.getIdUsuario());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @param id identificador de la cuenta
     * @param hash nuevo hash generado con {@code SeguridadClave}
     * @return {@code true} si se actualizó
     * @throws SQLException si falla la actualización
     */
    public boolean cambiarClave(int id, String hash) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_CAMBIAR_CLAVE)) {
            ps.setString(1, hash);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Activa o inactiva una cuenta. Una cuenta inactiva no puede iniciar sesión.
     *
     * @param id identificador de la cuenta
     * @param activo nuevo estado
     * @return {@code true} si se actualizó
     * @throws SQLException si falla la actualización
     */
    public boolean cambiarEstado(int id, boolean activo) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            ps.setBoolean(1, activo);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @param id identificador de la cuenta
     * @return {@code true} si se eliminó
     * @throws SQLException si falla la eliminación
     */
    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cuenta los administradores activos, para no dejar el sistema sin ninguno.
     *
     * @return número de administradores activos
     * @throws SQLException si falla la consulta
     */
    public int contarAdministradoresActivos() throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_CONTAR_ADMIN_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setUsuario(rs.getString("usuario"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRol(rs.getString("rol"));
        u.setActivo(rs.getBoolean("estado"));
        return u;
    }
}
