package com.endofertil.colposcopia.util;

import com.endofertil.colposcopia.modelo.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Reglas de validación del lado del servidor para la programación de citas.
 * Los cruces de horario se revisan aparte en {@code CitaDAO}, porque requieren
 * consultar la base de datos.
 */
public final class CitaValidador {

    /** Hora de inicio de la atención en la I.P.S. */
    public static final LocalTime HORA_APERTURA = LocalTime.of(7, 0);
    /** Última hora en la que se puede programar una cita. */
    public static final LocalTime HORA_ULTIMA_CITA = LocalTime.of(17, 30);

    private CitaValidador() {
        // Clase utilitaria: no se instancia
    }

    /**
     * Valida una cita antes de guardarla.
     *
     * @param c cita a validar
     * @param hoy fecha actual (se recibe como parámetro para poder probar con fechas fijas)
     * @return lista de mensajes de error; vacía si los datos son válidos
     */
    public static List<String> validar(Cita c, LocalDate hoy) {
        List<String> errores = new ArrayList<>();

        if (c.getIdPaciente() <= 0) {
            errores.add("Seleccione la paciente.");
        }
        if (c.getIdMedico() <= 0) {
            errores.add("Seleccione el médico.");
        }
        if (c.getFecha() == null) {
            errores.add("La fecha es obligatoria.");
        } else if (c.isNueva() && c.getFecha().isBefore(hoy)) {
            errores.add("No se puede programar una cita en una fecha pasada.");
        }
        if (c.getHora() == null) {
            errores.add("La hora es obligatoria.");
        } else if (c.getHora().isBefore(HORA_APERTURA) || c.getHora().isAfter(HORA_ULTIMA_CITA)) {
            errores.add("La hora debe estar entre las 7:00 a. m. y las 5:30 p. m.");
        }
        errores.addAll(validarEstado(c.getEstado(), c.getFecha(), hoy));
        return errores;
    }

    /**
     * Valida un cambio de estado.
     *
     * @param estado estado nuevo
     * @param fecha fecha de la cita
     * @param hoy fecha actual
     * @return lista de errores; vacía si el cambio es válido
     */
    public static List<String> validarEstado(String estado, LocalDate fecha, LocalDate hoy) {
        List<String> errores = new ArrayList<>();
        if (estado == null || !Cita.ESTADOS.contains(estado)) {
            errores.add("Seleccione un estado válido.");
        } else if (fecha != null && fecha.isAfter(hoy)
                && (Cita.ATENDIDA.equals(estado) || Cita.NO_ASISTIO.equals(estado))) {
            errores.add("Una cita futura no puede marcarse como atendida ni como inasistencia.");
        }
        return errores;
    }
}
