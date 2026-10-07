package com.endofertil.colposcopia.controlador;

import com.endofertil.colposcopia.dao.UsuarioDAO;
import com.endofertil.colposcopia.modelo.Usuario;
import com.endofertil.colposcopia.util.SeguridadClave;
import com.endofertil.colposcopia.util.Sesion;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Inicio de sesión.
 * GET muestra el formulario; POST valida usuario y contraseña contra la tabla {@code usuario}.
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String VISTA = "/WEB-INF/vistas/login.jsp";
    /** Mismo mensaje para usuario inexistente y clave errada: no revela qué cuentas existen. */
    private static final String ERROR_CREDENCIALES = "Usuario o contraseña incorrectos.";

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (Sesion.usuario(req) != null) {
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
            return;
        }
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String nombre = req.getParameter("usuario") == null ? "" : req.getParameter("usuario").trim();
        String clave = req.getParameter("clave");

        if (nombre.isEmpty() || clave == null || clave.isEmpty()) {
            mostrarError(req, resp, nombre, "Escriba su usuario y su contraseña.");
            return;
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorUsuario(nombre);
            if (usuario == null || !SeguridadClave.verificar(clave, usuario.getPasswordHash())) {
                mostrarError(req, resp, nombre, ERROR_CREDENCIALES);
                return;
            }
            if (!usuario.isActivo()) {
                mostrarError(req, resp, nombre, "Su cuenta está inactiva. Comuníquese con el administrador.");
                return;
            }

            // Migración: si la contraseña estaba en texto plano, se guarda cifrada desde ya
            if (!SeguridadClave.esHash(usuario.getPasswordHash())) {
                usuarioDAO.cambiarClave(usuario.getIdUsuario(), SeguridadClave.generarHash(clave));
                LOG.info("Contraseña del usuario " + usuario.getUsuario() + " migrada a PBKDF2.");
            }

            // Se descarta la sesión anterior y se crea una nueva (evita fijación de sesión)
            HttpSession anterior = req.getSession(false);
            if (anterior != null) {
                anterior.invalidate();
            }
            req.getSession(true).setAttribute(Sesion.ATRIBUTO_USUARIO, usuario.sinClave());
            resp.sendRedirect(req.getContextPath() + "/index.jsp");
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos al iniciar sesión", e);
            mostrarError(req, resp, nombre,
                    "No fue posible conectarse a la base de datos. Verifique que MySQL esté activo.");
        }
    }

    private void mostrarError(HttpServletRequest req, HttpServletResponse resp, String nombre, String error)
            throws ServletException, IOException {
        req.setAttribute("error", error);
        req.setAttribute("usuarioDigitado", nombre);
        req.getRequestDispatcher(VISTA).forward(req, resp);
    }
}
