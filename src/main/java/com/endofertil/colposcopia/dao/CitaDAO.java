package com.endofertil.colposcopia.dao;

import com.endofertil.colposcopia.modelo.Cita;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos (DAO) para la tabla {@code cita}.
 * Las consultas de lectura hacen JOIN con {@code paciente} y {@code medico}
 * para mostrar los nombres en la agenda.
 */
public class CitaDAO {

    /** Código MySQL: la cita tiene una colposcopia registrada (ON DELETE RESTRICT). */
    public static final int ERROR_TIENE_DEPENDENCIAS = 1451;
    /** Código MySQL: la paciente o el médico indicados no existen. */
    public static final int ERROR_LLAVE_FORANEA = 1452;
    /** Duración de una cita; dos citas del mismo médico deben separarse al menos este tiempo. */
    public static final int DURACION_MINUTOS = 30;

    private static final String SELECT_BASE = "SELECT c.id_cita, c.fecha, c.hora, c.estado, "
            + "c.id_paciente, c.id_medico, "
            + "CONCAT(p.nombres, ' ', p.apellidos) AS nombre_paciente, p.numero_documento, "
            + "CONCAT(m.nombres, ' ', m.apellidos) AS nombre_medico "
            + "FROM cita c "
            + "JOIN paciente p ON p.id_paciente = c.id_paciente "
            + "JOIN medico m ON m.id_medico = c.id_medico ";

    private static final String SQL_BUSCAR_POR_ID = SELECT_BASE + "WHERE c.id_cita = ?";

    private static final String SQL_INSERTAR = "INSERT INTO cita (fecha, hora, estado, id_paciente, id_medico) "
            + "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_ACTUALIZAR = "UPDATE cita SET fecha = ?, hora = ?, estado = ?, "
            + "id_paciente = ?, id_medico = ? WHERE id_cita = ?";

    private static final String SQL_CAMBIAR_ESTADO = "UPDATE cita SET estado = ? WHERE id_cita = ?";

    private static final String SQL_ELIMINAR = "DELETE FROM cita WHERE id_cita = ?";

    /*
     * Cruce de horario: otra cita no cancelada del mismo médico (o de la misma paciente)
     * el mismo día, con menos de DURACION_MINUTOS de diferencia. Se excluye la propia cita al editar.
     */
    private static final String SQL_CRUCE = "SELECT COUNT(*) FROM cita "
            + "WHERE %s = ? AND fecha = ? AND estado <> 'Cancelada' AND id_cita <> ? "
            + "AND ABS(TIME_TO_SEC(TIMEDIFF(hora, ?))) < ?";

    /**
     * Consulta la agenda con filtros opcionales.
     *
     * @param fecha día exacto; si es nulo se usa {@code desde}
     * @param desde primer día a mostrar cuando no hay fecha exacta; nulo muestra todo el historial
     * @param estado estado exacto; nulo o vacío muestra todos
     * @param texto documento, nombres o apellidos de la paciente; nulo o vacío no filtra
     * @return citas ordenadas por fecha y hora
     * @throws SQLException si falla la consulta
     */
    public List<Cita> listar(LocalDate fecha, LocalDate desde, String estado, String texto) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE).append("WHERE 1 = 1 ");
        List<Object> parametros = new ArrayList<>();

        if (fecha != null) {
            sql.append("AND c.fecha = ? ");
            parametros.add(Date.valueOf(fecha));
        } else if (desde != null) {
            sql.append("AND c.fecha >= ? ");
            parametros.add(Date.valueOf(desde));
        }
        if (estado != null && !estado.isBlank()) {
            sql.append("AND c.estado = ? ");
            parametros.add(estado);
        }
        if (texto != null && !texto.isBlank()) {
            String patron = "%" + texto.trim() + "%";
            sql.append("AND (p.numero_documento LIKE ? OR p.nombres LIKE ? OR p.apellidos LIKE ?) ");
            parametros.add(patron);
            parametros.add(patron);
            parametros.add(patron);
        }
        sql.append("ORDER BY c.fecha, c.hora");

        List<Cita> citas = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    citas.add(mapear(rs));
                }
            }
        }
        return citas;
    }

    /**
     * @param id identificador de la cita
     * @return la cita con nombres de paciente y médico, o {@code null} si no existe
     * @throws SQLException si falla la consulta
     */
    public Cita buscarPorId(int id) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * @param c cita a registrar
     * @return id generado
     * @throws SQLException si falla la inserción
     */
    public int insertar(Cita c) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(ps, c);
            ps.executeUpdate();
            try (ResultSet llaves = ps.getGeneratedKeys()) {
                if (llaves.next()) {
                    c.setIdCita(llaves.getInt(1));
                }
            }
        }
        return c.getIdCita();
    }

    /**
     * @param c cita con su id
     * @return {@code true} si se actualizó
     * @throws SQLException si falla la actualización
     */
    public boolean actualizar(Cita c) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_ACTUALIZAR)) {
            asignarParametros(ps, c);
            ps.setInt(6, c.getIdCita());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * @param id identificador de la cita
     * @param estado nuevo estado (uno de {@link Cita#ESTADOS})
     * @return {@code true} si se actualizó
     * @throws SQLException si falla la actualización
     */
    public boolean cambiarEstado(int id, String estado) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_CAMBIAR_ESTADO)) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Elimina una cita. Falla con {@link #ERROR_TIENE_DEPENDENCIAS} si ya tiene colposcopia.
     *
     * @param id identificador de la cita
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
     * @param c cita a guardar
     * @return {@code true} si el médico ya tiene otra cita cercana a esa hora
     * @throws SQLException si falla la consulta
     */
    public boolean medicoOcupado(Cita c) throws SQLException {
        return hayCruce("id_medico", c.getIdMedico(), c);
    }

    /**
     * @param c cita a guardar
     * @return {@code true} si la paciente ya tiene otra cita cercana a esa hora
     * @throws SQLException si falla la consulta
     */
    public boolean pacienteOcupada(Cita c) throws SQLException {
        return hayCruce("id_paciente", c.getIdPaciente(), c);
    }

    /** El nombre de la columna es una constante interna, nunca un dato del usuario. */
    private boolean hayCruce(String columna, int id, Cita c) throws SQLException {
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(String.format(SQL_CRUCE, columna))) {
            ps.setInt(1, id);
            ps.setDate(2, Date.valueOf(c.getFecha()));
            ps.setInt(3, c.getIdCita());
            ps.setTime(4, Time.valueOf(c.getHora()));
            ps.setInt(5, DURACION_MINUTOS * 60);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void asignarParametros(PreparedStatement ps, Cita c) throws SQLException {
        ps.setDate(1, Date.valueOf(c.getFecha()));
        ps.setTime(2, Time.valueOf(c.getHora()));
        ps.setString(3, c.getEstado());
        ps.setInt(4, c.getIdPaciente());
        ps.setInt(5, c.getIdMedico());
    }

    private Cita mapear(ResultSet rs) throws SQLException {
        Cita c = new Cita();
        c.setIdCita(rs.getInt("id_cita"));
        c.setFecha(rs.getDate("fecha").toLocalDate());
        c.setHora(rs.getTime("hora").toLocalTime());
        c.setEstado(rs.getString("estado"));
        c.setIdPaciente(rs.getInt("id_paciente"));
        c.setIdMedico(rs.getInt("id_medico"));
        c.setNombrePaciente(rs.getString("nombre_paciente"));
        c.setDocumentoPaciente(rs.getString("numero_documento"));
        c.setNombreMedico(rs.getString("nombre_medico"));
        return c;
    }
}
