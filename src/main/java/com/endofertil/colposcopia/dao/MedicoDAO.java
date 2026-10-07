package com.endofertil.colposcopia.dao;

import com.endofertil.colposcopia.modelo.Medico;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de acceso a datos para la tabla {@code medico}.
 * Por ahora solo lista los médicos para el formulario de citas.
 */
public class MedicoDAO {

    private static final String SQL_LISTAR = "SELECT id_medico, nombres, apellidos, registro_medico, "
            + "especialidad FROM medico ORDER BY apellidos, nombres";

    /**
     * @return todos los médicos ordenados por apellidos
     * @throws SQLException si falla la consulta
     */
    public List<Medico> listar() throws SQLException {
        List<Medico> medicos = new ArrayList<>();
        try (Connection cn = Conexion.obtener();
             PreparedStatement ps = cn.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Medico m = new Medico();
                m.setIdMedico(rs.getInt("id_medico"));
                m.setNombres(rs.getString("nombres"));
                m.setApellidos(rs.getString("apellidos"));
                m.setRegistroMedico(rs.getString("registro_medico"));
                m.setEspecialidad(rs.getString("especialidad"));
                medicos.add(m);
            }
        }
        return medicos;
    }
}
