package com.endofertil.colposcopia.filtro;

import com.endofertil.colposcopia.modelo.Usuario;
import com.endofertil.colposcopia.util.Sesion;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Protege todas las páginas del sistema.
 *
 * <ul>
 *   <li>Sin sesión iniciada solo se permite el inicio de sesión y los estilos.</li>
 *   <li>El módulo de usuarios ({@code /usuarios}) es exclusivo del rol Administrador.</li>
 *   <li>Las páginas protegidas no se guardan en caché, así el botón "Atrás"
 *       no muestra datos después de cerrar sesión.</li>
 * </ul>
 */
@WebFilter(filterName = "AutenticacionFiltro", urlPatterns = {"/*"})
public class AutenticacionFiltro implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String ruta = req.getRequestURI().substring(req.getContextPath().length());

        if (esPublica(ruta)) {
            chain.doFilter(request, response);
            return;
        }

        Usuario usuario = Sesion.usuario(req);
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (ruta.startsWith("/usuarios") && !usuario.isAdministrador()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        resp.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        chain.doFilter(request, response);
    }

    /** Recursos que se pueden ver sin iniciar sesión. */
    private boolean esPublica(String ruta) {
        return ruta.equals("/login") || ruta.startsWith("/css/") || ruta.equals("/favicon.ico");
    }
}
