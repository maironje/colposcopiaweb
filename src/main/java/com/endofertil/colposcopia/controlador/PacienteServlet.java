package com.endofertil.colposcopia.controlador;

import com.endofertil.colposcopia.dao.PacienteDAO;
import com.endofertil.colposcopia.modelo.Paciente;
import com.endofertil.colposcopia.util.PacienteValidador;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador del módulo Pacientes.
 *
 * <p>Peticiones GET (consultas, no modifican datos):</p>
 * <ul>
 *   <li>{@code /pacientes} o {@code ?accion=listar&q=...} lista y busca pacientes</li>
 *   <li>{@code ?accion=nuevo} muestra el formulario vacío</li>
 *   <li>{@code ?accion=editar&id=N} muestra el formulario con los datos de la paciente</li>
 * </ul>
 * <p>Peticiones POST (modifican datos):</p>
 * <ul>
 *   <li>{@code accion=guardar} registra o actualiza una paciente</li>
 *   <li>{@code accion=eliminar} elimina una paciente</li>
 * </ul>
 * <p>Tras cada POST exitoso se redirige al listado (patrón Post/Redirect/Get)
 * para que recargar la página no duplique el registro.</p>
 */
@WebServlet(name = "PacienteServlet", urlPatterns = {"/pacientes"})
public class PacienteServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(PacienteServlet.class.getName());

    private static final String VISTA_LISTADO = "/WEB-INF/vistas/pacientes/listado.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/pacientes/formulario.jsp";
    private static final String VISTA_ERROR = "/WEB-INF/vistas/error.jsp";

    /** Tipos de documento: código guardado en BD -> texto mostrado en pantalla. */
    private static final Map<String, String> TIPOS_DOCUMENTO;

    static {
        Map<String, String> tipos = new LinkedHashMap<>();
        tipos.put("CC", "Cédula de ciudadanía");
        tipos.put("TI", "Tarjeta de identidad");
        tipos.put("CE", "Cédula de extranjería");
        tipos.put("PA", "Pasaporte");
        tipos.put("PPT", "Permiso por protección temporal");
        TIPOS_DOCUMENTO = Collections.unmodifiableMap(tipos);
    }

    private static final List<String> LISTA_EPS = List.of(
            "Compensar", "Coosalud", "Famisanar", "Nueva EPS", "Salud Total", "Sanitas", "Sura", "Otra", "Mutual Ser");

    private final PacienteDAO pacienteDAO = new PacienteDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = valorODefecto(req.getParameter("accion"), "listar");
        try {
            switch (accion) {
                case "nuevo":
                    mostrarFormulario(req, resp, new Paciente());
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
        // Debe ir antes de leer cualquier parámetro para conservar tildes y eñes
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String accion = valorODefecto(req.getParameter("accion"), "");
        try {
            switch (accion) {
                case "guardar":
                    guardar(req, resp);
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

    /** GET: lista las pacientes aplicando el filtro de búsqueda opcional "q". */
    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        String filtro = req.getParameter("q");
        req.setAttribute("pacientes", pacienteDAO.listar(filtro));
        req.setAttribute("filtro", filtro);
        req.setAttribute("tiposDocumento", TIPOS_DOCUMENTO);
        req.getRequestDispatcher(VISTA_LISTADO).forward(req, resp);
    }

    /** GET: carga la paciente indicada y muestra el formulario de edición. */
    private void editar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Integer id = convertirEntero(req.getParameter("id"));
        Paciente paciente = (id == null) ? null : pacienteDAO.buscarPorId(id);
        if (paciente == null) {
            enviarMensaje(req, "error", "La paciente solicitada no existe.");
            resp.sendRedirect(req.getContextPath() + "/pacientes");
            return;
        }
        mostrarFormulario(req, resp, paciente);
    }

    /** POST: valida y registra o actualiza según exista o no el id. */
    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Paciente paciente = leerFormulario(req);
        List<String> errores = PacienteValidador.validar(paciente);
        if (!errores.isEmpty()) {
            req.setAttribute("errores", errores);
            mostrarFormulario(req, resp, paciente);
            return;
        }

        try {
            if (paciente.isNueva()) {
                pacienteDAO.insertar(paciente);
                enviarMensaje(req, "exito", "Paciente " + paciente.getNombreCompleto() + " registrada.");
            } else {
                pacienteDAO.actualizar(paciente);
                enviarMensaje(req, "exito", "Datos de " + paciente.getNombreCompleto() + " actualizados.");
            }
            resp.sendRedirect(req.getContextPath() + "/pacientes");
        } catch (SQLException e) {
            if (e.getErrorCode() != PacienteDAO.ERROR_DUPLICADO) {
                throw e;
            }
            req.setAttribute("errores", List.of(
                    "Ya existe una paciente con el documento " + paciente.getNumeroDocumento() + "."));
            mostrarFormulario(req, resp, paciente);
        }
    }

    /** POST: elimina la paciente si no tiene citas asociadas. */
    private void eliminar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Integer id = convertirEntero(req.getParameter("id"));
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Identificador no válido");
            return;
        }
        try {
            if (pacienteDAO.eliminar(id)) {
                enviarMensaje(req, "exito", "Paciente eliminada.");
            } else {
                enviarMensaje(req, "error", "La paciente ya no existe.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() != PacienteDAO.ERROR_TIENE_DEPENDENCIAS) {
                throw e;
            }
            enviarMensaje(req, "error",
                    "No se puede eliminar: la paciente tiene citas registradas.");
        }
        resp.sendRedirect(req.getContextPath() + "/pacientes");
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, Paciente paciente)
            throws ServletException, IOException {
        req.setAttribute("paciente", paciente);
        req.setAttribute("tiposDocumento", TIPOS_DOCUMENTO);
        req.setAttribute("listaEps", LISTA_EPS);
        req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
    }

    /** Construye un objeto Paciente con los campos enviados por el formulario HTML. */
    private Paciente leerFormulario(HttpServletRequest req) {
        Paciente p = new Paciente();
        Integer id = convertirEntero(req.getParameter("idPaciente"));
        p.setIdPaciente(id == null ? 0 : id);
        p.setTipoDocumento(limpiar(req.getParameter("tipoDocumento")));
        p.setNumeroDocumento(limpiar(req.getParameter("numeroDocumento")));
        p.setNombres(limpiar(req.getParameter("nombres")));
        p.setApellidos(limpiar(req.getParameter("apellidos")));
        p.setFechaNacimiento(convertirFecha(req.getParameter("fechaNacimiento")));
        p.setTelefono(limpiar(req.getParameter("telefono")));
        p.setCorreo(limpiar(req.getParameter("correo")));
        p.setDireccion(limpiar(req.getParameter("direccion")));
        p.setEps(limpiar(req.getParameter("eps")));
        return p;
    }

    /** Guarda un mensaje en sesión para mostrarlo después de la redirección. */
    private void enviarMensaje(HttpServletRequest req, String tipo, String texto) {
        req.getSession().setAttribute("tipoMensaje", tipo);
        req.getSession().setAttribute("mensaje", texto);
    }

    private void manejarErrorBaseDatos(SQLException e, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.log(Level.SEVERE, "Error de base de datos en el módulo Pacientes", e);
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

    private static LocalDate convertirFecha(String valor) {
        try {
            return (valor == null || valor.isBlank()) ? null : LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
