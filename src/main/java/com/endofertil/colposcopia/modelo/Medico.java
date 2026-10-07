package com.endofertil.colposcopia.modelo;

/**
 * Entidad que representa a un médico de la I.P.S.
 * Corresponde a la tabla {@code medico}. Por ahora se usa solo para
 * elegir el médico responsable al programar una cita.
 *
 * @author Mairon Medez
 */
public class Medico {

    private int idMedico;
    private String nombres;
    private String apellidos;
    private String registroMedico;
    private String especialidad;

    public Medico() {
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
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

    public String getRegistroMedico() {
        return registroMedico;
    }

    public void setRegistroMedico(String registroMedico) {
        this.registroMedico = registroMedico;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    /**
     * Nombre completo del médico, usado en listas desplegables y en la agenda.
     *
     * @return nombres y apellidos separados por un espacio
     */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}
