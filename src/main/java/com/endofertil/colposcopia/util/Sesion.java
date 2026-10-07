package com.endofertil.colposcopia.util;

import com.endofertil.colposcopia.modelo.Usuario;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Utilidades para leer el usuario que inició sesión y enviar mensajes
 * entre una redirección y la página siguiente.
 */
public final class Sesion {

    /** Nombre del atributo de sesión donde se guarda el usuario autenticado. */
    public static final String ATRIBUTO_USUARIO = "usuarioSesion";

    private Sesion() {
        // Clase utilitaria: no se instancia
    }

    /**
     * @param req petición actual
     * @return el usuario autenticado o {@code null} si no hay sesión
     */
    public static Usuario usuario(HttpServletRequest req) {
        HttpSession sesion = req.getSession(false);
        return sesion == null ? null : (Usuario) sesion.getAttribute(ATRIBUTO_USUARIO);
    }

    /**
     * Guarda un mensaje en sesión; {@code mensajes.jspf} lo muestra y lo borra.
     *
     * @param req petición actual
     * @param tipo "exito" o "error"
     * @param texto mensaje para el usuario
     */
    public static void mensaje(HttpServletRequest req, String tipo, String texto) {
        req.getSession().setAttribute("tipoMensaje", tipo);
        req.getSession().setAttribute("mensaje", texto);
    }
}
