package com.endofertil.colposcopia.modelo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Entidad que representa una cita de atención entre una paciente y un médico.
 * Corresponde a la tabla {@code cita}. Además de las columnas propias, guarda
 * los nombres de la paciente y el médico obtenidos con JOIN para la agenda.
 *
 * @author Mairon Medez
 */
public class Cita {

    public static final String PROGRAMADA = "Programada";
    public static final String ATENDIDA = "Atendida";
    public static final String CANCELADA = "Cancelada";
    public static final String NO_ASISTIO = "No asistio";

    /** Estados permitidos por la restricción CHECK {@code chk_cita_estado}. */
    public static final List<String> ESTADOS = List.of(PROGRAMADA, ATENDIDA, CANCELADA, NO_ASISTIO);

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("EEE d MMM yyyy", Locale.forLanguageTag("es-CO"));
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("h:mm a", Locale.US);

    private int idCita;
    private LocalDate fecha;
    private LocalTime hora;
    private String estado = PROGRAMADA;
    private int idPaciente;
    private int idMedico;

    // Datos de solo lectura que llegan por JOIN (no se guardan en la tabla cita)
    private String nombrePaciente;
    private String documentoPaciente;
    private String nombreMedico;

    public Cita() {
    }

    public int getIdCita() {
        return idCita;
    }

    public void setIdCita(int idCita) {
        this.idCita = idCita;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public String getDocumentoPaciente() {
        return documentoPaciente;
    }

    public void setDocumentoPaciente(String documentoPaciente) {
        this.documentoPaciente = documentoPaciente;
    }

    public String getNombreMedico() {
        return nombreMedico;
    }

    public void setNombreMedico(String nombreMedico) {
        this.nombreMedico = nombreMedico;
    }

    /** @return {@code true} si la cita aún no existe en la base de datos */
    public boolean isNueva() {
        return idCita == 0;
    }

    /** @return {@code true} si la cita está pendiente de atención */
    public boolean isProgramada() {
        return PROGRAMADA.equals(estado);
    }

    /** @return {@code true} si la cita es posterior a hoy (aún no se puede marcar como atendida) */
    public boolean isFutura() {
        return fecha != null && fecha.isAfter(LocalDate.now());
    }

    /** @return fecha legible, por ejemplo "mar 20 oct 2026" */
    public String getFechaTexto() {
        return fecha == null ? "" : fecha.format(FORMATO_FECHA);
    }

    /** @return hora en formato de 12 horas, por ejemplo "8:00 AM" */
    public String getHoraTexto() {
        return hora == null ? "" : hora.format(FORMATO_HORA);
    }

    /** @return hora en formato HH:mm, el que espera un {@code <input type="time">} */
    public String getHoraCampo() {
        return hora == null ? "" : String.format("%02d:%02d", hora.getHour(), hora.getMinute());
    }

    /** @return estado con tilde para mostrar en pantalla */
    public String getEstadoTexto() {
        return NO_ASISTIO.equals(estado) ? "No asistió" : estado;
    }

    /** @return sufijo de la clase CSS de la etiqueta de estado, por ejemplo "no-asistio" */
    public String getEstadoClase() {
        return estado == null ? "" : estado.toLowerCase(Locale.ROOT).replace(' ', '-');
    }
}
