package com.endofertil.colposcopia.modelo;

import java.io.Serializable;
import java.util.List;

/**
 * Entidad que representa una cuenta de acceso al sistema.
 * Corresponde a la tabla {@code usuario} de la base de datos {@code bd_colposcopia}.
 * Es serializable porque se guarda en la sesión HTTP.
 *
 * @author Mairon Medez
 */
public class Usuario implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Rol con acceso total, incluida la gestión de usuarios. */
    public static final String ROL_ADMINISTRADOR = "Administrador";
    /** Rol del personal médico: consulta citas y registra su atención. */
    public static final String ROL_MEDICO = "Medico";
    /** Rol del personal administrativo: agenda citas y registra pacientes. */
    public static final String ROL_AUXILIAR = "Auxiliar";

    /** Roles permitidos por la restricción CHECK {@code chk_usuario_rol}. */
    public static final List<String> ROLES = List.of(ROL_ADMINISTRADOR, ROL_MEDICO, ROL_AUXILIAR);

    private int idUsuario;
    private String usuario;
    private String passwordHash;
    private String rol;
    private boolean activo = true;

    /** Constructor vacío requerido para usar la clase como JavaBean en las JSP. */
    public Usuario() {
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    /**
     * Indica si la cuenta aún no existe en la base de datos.
     * En EL se consulta como {@code ${usuario.nuevo}}.
     *
     * @return {@code true} si el identificador es 0
     */
    public boolean isNuevo() {
        return idUsuario == 0;
    }

    /**
     * Indica si la cuenta tiene el rol de administrador.
     * En EL se consulta como {@code ${usuarioSesion.administrador}}.
     *
     * @return {@code true} si el rol es Administrador
     */
    public boolean isAdministrador() {
        return ROL_ADMINISTRADOR.equals(rol);
    }

    /**
     * Indica si el usuario puede programar, editar y eliminar citas.
     *
     * @return {@code true} para Administrador y Auxiliar
     */
    public boolean isPuedeAgendar() {
        return ROL_ADMINISTRADOR.equals(rol) || ROL_AUXILIAR.equals(rol);
    }

    /**
     * Texto del rol para mostrar en pantalla (con tilde).
     *
     * @return nombre legible del rol
     */
    public String getRolTexto() {
        return ROL_MEDICO.equals(rol) ? "Médico" : rol;
    }

    /**
     * Copia de la cuenta sin el hash de la contraseña, para guardarla en la sesión HTTP.
     *
     * @return usuario sin datos sensibles
     */
    public Usuario sinClave() {
        Usuario copia = new Usuario();
        copia.setIdUsuario(idUsuario);
        copia.setUsuario(usuario);
        copia.setRol(rol);
        copia.setActivo(activo);
        return copia;
    }
}
