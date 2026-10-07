package com.endofertil.colposcopia.controlador;

import com.endofertil.colposcopia.dao.UsuarioDAO;
import com.endofertil.colposcopia.modelo.Usuario;
import com.endofertil.colposcopia.util.SeguridadClave;
import com.endofertil.colposcopia.util.Sesion;
import com.endofertil.colposcopia.util.UsuarioValidador;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador del módulo Usuarios (solo Administrador; lo garantiza {@code AutenticacionFiltro}).
 *
 * <p>GET: {@code listar} (con filtro "q"), {@code nuevo}, {@code editar&id=N}.</p>
 * <p>POST: {@code guardar}, {@code cambiarEstado}, {@code eliminar}.</p>
 *
 * <p>Reglas de protección: un administrador no puede inactivarse, eliminarse ni quitarse
 * el rol a sí mismo, y el sistema nunca se queda sin un administrador activo.</p>
 */
@WebServlet(name = "UsuarioServlet", urlPatterns = {"/usuarios"})
public class UsuarioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(UsuarioServlet.class.getName());

    private static final String VISTA_LISTADO = "/WEB-INF/vistas/usuarios/listado.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/usuarios/formulario.jsp";
    private static final String VISTA_ERROR = "/WEB-INF/vistas/error.jsp";

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = valorODefecto(req.getParameter("accion"), "listar");
        try {
            switch (accion) {
                case "nuevo":
                    mostrarFormulario(req, resp, new Usuario());
                    break;
                case "editar":
                    editar(req, resp);
                    break;
                default:
                    listar(req, resp);
            }
        } catch (SQLException e) {
            manejarErrorBaseDatos(e, req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String accion = valorODefecto(req.getParameter("accion"), "");
        try {
            switch (accion) {
                case "guardar":
                    guardar(req, resp);
                    break;
                case "cambiarEstado":
                    cambiarEstado(req, resp);
                    break;
                case "eliminar":
                    eliminar(req, resp);
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
            }
        } catch (SQLException e) {
            manejarErrorBaseDatos(e, req, resp);
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        String filtro = req.getParameter("q");
        req.setAttribute("usuarios", usuarioDAO.listar(filtro));
        req.setAttribute("filtro", filtro);
        req.getRequestDispatcher(VISTA_LISTADO).forward(req, resp);
    }

    private void editar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Integer id = convertirEntero(req.getParameter("id"));
        Usuario usuario = (id == null) ? null : usuarioDAO.buscarPorId(id);
        if (usuario == null) {
            Sesion.mensaje(req, "error", "El usuario solicitado no existe.");
            resp.sendRedirect(req.getContextPath() + "/usuarios");
            return;
        }
        mostrarFormulario(req, resp, usuario);
    }

    /** POST: crea la cuenta o actualiza usuario, rol, estado y (opcionalmente) contraseña. */
    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Usuario usuario = leerFormulario(req);
        String clave = req.getParameter("clave");
        String confirmacion = req.getParameter("confirmacion");

        List<String> errores = UsuarioValidador.validar(usuario, clave, confirmacion);
        if (errores.isEmpty() && !usuario.isNuevo()) {
            String problema = revisarCambioDeAdministrador(req, usuario);
            if (problema != null) {
                errores.add(problema);
            }
        }
        if (!errores.isEmpty()) {
            req.setAttribute("errores", errores);
            mostrarFormulario(req, resp, usuario);
            return;
        }

        try {
            if (usuario.isNuevo()) {
                usuario.setPasswordHash(SeguridadClave.generarHash(clave));
                usuarioDAO.insertar(usuario);
                Sesion.mensaje(req, "exito", "Usuario " + usuario.getUsuario() + " creado.");
            } else {
                usuarioDAO.actualizar(usuario);
                if (clave != null && !clave.isBlank()) {
                    usuarioDAO.cambiarClave(usuario.getIdUsuario(), SeguridadClave.generarHash(clave));
                }
                actualizarSesionSiEsPropio(req, usuario);
                Sesion.mensaje(req, "exito", "Usuario " + usuario.getUsuario() + " actualizado.");
            }
            resp.sendRedirect(req.getContextPath() + "/usuarios");
        } catch (SQLException e) {
            if (e.getErrorCode() != UsuarioDAO.ERROR_DUPLICADO) {
                throw e;
            }
            req.setAttribute("errores", List.of("Ya existe una cuenta con el usuario " + usuario.getUsuario() + "."));
            mostrarFormulario(req, resp, usuario);
        }
    }

    /** POST: activa o inactiva una cuenta. */
    private void cambiarEstado(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Usuario usuario = buscarDesdeFormulario(req);
        if (usuario == null) {
            Sesion.mensaje(req, "error", "El usuario ya no existe.");
        } else {
            usuario.setActivo(!usuario.isActivo());
            String problema = revisarCambioDeAdministrador(req, usuario);
            if (problema != null) {
                Sesion.mensaje(req, "error", problema);
            } else {
                usuarioDAO.cambiarEstado(usuario.getIdUsuario(), usuario.isActivo());
                Sesion.mensaje(req, "exito", "Usuario " + usuario.getUsuario()
                        + (usuario.isActivo() ? " activado." : " inactivado. Ya no podrá iniciar sesión."));
            }
        }
        resp.sendRedirect(req.getContextPath() + "/usuarios");
    }

    /** POST: elimina una cuenta (con las mismas protecciones de administrador). */
    private void eliminar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Usuario usuario = buscarDesdeFormulario(req);
        Usuario actual = Sesion.usuario(req);
        if (usuario == null) {
            Sesion.mensaje(req, "error", "El usuario ya no existe.");
        } else if (usuario.getIdUsuario() == actual.getIdUsuario()) {
            Sesion.mensaje(req, "error", "No puede eliminar su propia cuenta.");
        } else if (usuario.isAdministrador() && usuario.isActivo()
                && usuarioDAO.contarAdministradoresActivos() <= 1) {
            Sesion.mensaje(req, "error", "No se puede eliminar el único administrador activo.");
        } else {
            usuarioDAO.eliminar(usuario.getIdUsuario());
            Sesion.mensaje(req, "exito", "Usuario " + usuario.getUsuario() + " eliminado.");
        }
        resp.sendRedirect(req.getContextPath() + "/usuarios");
    }

    /**
     * Revisa que un cambio no deje al usuario actual sin acceso ni al sistema sin administrador.
     *
     * @return mensaje del problema, o {@code null} si el cambio es permitido
     */
    private String revisarCambioDeAdministrador(HttpServletRequest req, Usuario cambiado) throws SQLException {
        Usuario actual = Sesion.usuario(req);
        boolean esPropio = cambiado.getIdUsuario() == actual.getIdUsuario();
        boolean dejaDeSerAdminActivo = !cambiado.isAdministrador() || !cambiado.isActivo();

        if (esPropio && dejaDeSerAdminActivo) {
            return "No puede quitarse el rol de administrador ni inactivar su propia cuenta.";
        }
        if (dejaDeSerAdminActivo) {
            Usuario antes = usuarioDAO.buscarPorId(cambiado.getIdUsuario());
            boolean eraAdminActivo = antes != null && antes.isAdministrador() && antes.isActivo();
            if (eraAdminActivo && usuarioDAO.contarAdministradoresActivos() <= 1) {
                return "El sistema debe conservar al menos un administrador activo.";
            }
        }
        return null;
    }

    /** Si el administrador editó su propia cuenta, refresca los datos guardados en la sesión. */
    private void actualizarSesionSiEsPropio(HttpServletRequest req, Usuario usuario) {
        if (usuario.getIdUsuario() == Sesion.usuario(req).getIdUsuario()) {
            req.getSession().setAttribute(Sesion.ATRIBUTO_USUARIO, usuario.sinClave());
        }
    }

    private Usuario buscarDesdeFormulario(HttpServletRequest req) throws SQLException {
        Integer id = convertirEntero(req.getParameter("id"));
        return id == null ? null : usuarioDAO.buscarPorId(id);
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, Usuario usuario)
            throws ServletException, IOException {
        usuario.setPasswordHash(null); // el hash nunca viaja a la vista
        req.setAttribute("usuario", usuario);
        req.setAttribute("roles", Usuario.ROLES);
        req.setAttribute("minClave", UsuarioValidador.MIN_CLAVE);
        req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
    }

    private Usuario leerFormulario(HttpServletRequest req) {
        Usuario u = new Usuario();
        Integer id = convertirEntero(req.getParameter("idUsuario"));
        u.setIdUsuario(id == null ? 0 : id);
        u.setUsuario(limpiar(req.getParameter("usuario")));
        u.setRol(limpiar(req.getParameter("rol")));
        // Un checkbox sin marcar no se envía; por eso "ausente" significa inactivo
        u.setActivo(req.getParameter("activo") != null);
        return u;
    }

    private void manejarErrorBaseDatos(SQLException e, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.log(Level.SEVERE, "Error de base de datos en el módulo Usuarios", e);
        req.setAttribute("detalleError",
                "No fue posible completar la operación con la base de datos. Verifique que MySQL esté activo.");
        req.getRequestDispatcher(VISTA_ERROR).forward(req, resp);
    }

    private static String limpiar(String valor) {
        return valor == null ? null : valor.trim();
    }

    private static String valorODefecto(String valor, String defecto) {
        return (valor == null || valor.isBlank()) ? defecto : valor;
    }

    private static Integer convertirEntero(String valor) {
        try {
            return valor == null ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
