package com.endofertil.colposcopia.dao;

import com.endofertil.colposcopia.modelo.Paciente;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para la tabla {@code paciente}.
 * Todas las consultas usan PreparedStatement para evitar inyección SQL.
 */
public class PacienteDAO {

    /** Código MySQL: valor duplicado en un índice UNIQUE (numero_documento). */
    public static final int ERROR_DUPLICADO = 1062;

    /** Código MySQL: la fila tiene registros hijos (citas) con ON DELETE RESTRICT. */
    public static final int ERROR_TIENE_DEPENDENCIAS = 1451;

    private static final String COLUMNAS = "id_paciente, tipo_documento, numero_documento, nombres, "
            + "apellidos, fecha_nacimiento, telefono, correo, direccion, eps";

    private static final String SQL_LISTAR = "SELECT " + COLUMNAS + " FROM paciente "
            + "WHERE numero_documento LIKE ? OR nombres LIKE ? OR apellidos LIKE ? "
            + "ORDER BY apellidos, nombres";

    private static final String SQL_BUSCAR_POR_ID = "SELECT " + COLUMNAS
            + " FROM paciente WHERE id_paciente = ?";

    private static final String SQL_INSERTAR = "INSERT INTO paciente (tipo_documento, numero_documento, "
            + "nombres, apellidos, fecha_nacimiento, telefono, correo, direccion, eps) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR = "UPDATE paciente SET tipo_documento = ?, "
            + "numero_documento = ?, nombres = ?, apellidos = ?, fecha_nacimiento = ?, telefono = ?, "
            + "correo = ?, direccion = ?, eps = ? WHERE id_paciente = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM paciente WHERE id_paciente = ?";

    /**
     * Lista las pacientes cuyo documento, nombres o apellidos contengan el filtro.
     *
     * @param filtro texto a buscar; nulo o vacío devuelve todas
     * @return lista ordenada por apellidos y nombres
     * @throws SQLException si falla la consulta
     */
    public List<Paciente> listar(String filtro) throws SQLException {
        String patron = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        List<Paciente> pacientes = new ArrayList<>();

        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR)) {
            ps.setString(1, patron);
            ps.setString(2, patron);
            ps.setString(3, patron);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pacientes.add(mapear(rs));
                }
            }
        }
        return pacientes;
    }

    /**
     * Busca una paciente por su identificador.
     *
     * @param id identificador de la paciente
     * @return la paciente encontrada o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Paciente buscarPorId(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Registra una nueva paciente y asigna el id generado al objeto.
     *
     * @param paciente datos a guardar
     * @return id generado por la base de datos
     * @throws SQLException si falla la inserción (p. ej. documento duplicado)
     */
    public int insertar(Paciente paciente) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(ps, paciente);
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    paciente.setIdPaciente(llaves.getInt(1));
                }
            }
        }
        return paciente.getIdPaciente();
    }

    /**
     * Actualiza los datos de una paciente existente.
     *
     * @param paciente datos con el id de la paciente a modificar
     * @return {@code true} si se actualizó algún registro
     * @throws SQLException si falla la actualización
     */
    public boolean actualizar(Paciente paciente) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            asignarParametros(ps, paciente);
            ps.setInt(10, paciente.getIdPaciente());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina una paciente. Falla con {@link #ERROR_TIENE_DEPENDENCIAS}
     * si la paciente tiene citas asociadas.
     *
     * @param id identificador de la paciente
     * @return {@code true} si se eliminó el registro
     * @throws SQLException si falla la eliminación
     */
    public boolean eliminar(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_ELIMINAR)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    /** Asigna los 9 parámetros comunes de INSERT y UPDATE en el mismo orden. */
    private void asignarParametros(PreparedStatement ps, Paciente p) throws SQLException {
        ps.setString(1, p.getTipoDocumento());
        ps.setString(2, p.getNumeroDocumento());
        ps.setString(3, p.getNombres());
        ps.setString(4, p.getApellidos());
        ps.setDate(5, Date.valueOf(p.getFechaNacimiento()));
        ps.setString(6, vacioANulo(p.getTelefono()));
        ps.setString(7, vacioANulo(p.getCorreo()));
        ps.setString(8, vacioANulo(p.getDireccion()));
        ps.setString(9, vacioANulo(p.getEps()));
    }

    /** Convierte una fila del ResultSet en un objeto Paciente. */
    private Paciente mapear(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setIdPaciente(rs.getInt("id_paciente"));
        p.setTipoDocumento(rs.getString("tipo_documento"));
        p.setNumeroDocumento(rs.getString("numero_documento"));
        p.setNombres(rs.getString("nombres"));
        p.setApellidos(rs.getString("apellidos"));
        Date fecha = rs.getDate("fecha_nacimiento");
        p.setFechaNacimiento(fecha != null ? fecha.toLocalDate() : null);
        p.setTelefono(rs.getString("telefono"));
        p.setCorreo(rs.getString("correo"));
        p.setDireccion(rs.getString("direccion"));
        p.setEps(rs.getString("eps"));
        return p;
    }

    /** Los campos opcionales vacíos se guardan como NULL. */
    private String vacioANulo(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor;
    }
}
