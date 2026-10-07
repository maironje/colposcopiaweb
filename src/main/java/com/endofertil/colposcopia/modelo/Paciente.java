package com.endofertil.colposcopia.modelo;

import java.time.LocalDate;
import java.time.Period;

/**
 * Entidad que representa a una paciente de la I.P.S. Endofertil.
 * Corresponde a la tabla {@code paciente} de la base de datos {@code bd_colposcopia}.
 *
 * @author Mairon Medez
 */
public class Paciente {

    private int idPaciente;
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String correo;
    private String direccion;
    private String eps;

    /** Constructor vacío requerido para usar la clase como JavaBean en las JSP. */
    public Paciente() {
    }

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEps() {
        return eps;
    }

    public void setEps(String eps) {
        this.eps = eps;
    }

    /**
     * Nombre completo de la paciente, usado en listados y mensajes.
     *
     * @return nombres y apellidos separados por un espacio
     */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    /**
     * Calcula la edad actual a partir de la fecha de nacimiento.
     *
     * @return edad en años, o {@code null} si no hay fecha registrada
     */
    public Integer getEdad() {
        if (fechaNacimiento == null) {
            return null;
        }
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    /**
     * Indica si el registro aún no existe en la base de datos.
     * En EL se consulta como {@code ${paciente.nueva}}.
     *
     * @return {@code true} si el identificador es 0
     */
    public boolean isNueva() {
        return idPaciente == 0;
    }
}
