package com.endofertil.colposcopia.controlador;

import com.endofertil.colposcopia.dao.CitaDAO;
import com.endofertil.colposcopia.dao.MedicoDAO;
import com.endofertil.colposcopia.dao.PacienteDAO;
import com.endofertil.colposcopia.modelo.Cita;
import com.endofertil.colposcopia.modelo.Usuario;
import com.endofertil.colposcopia.util.CitaValidador;
import com.endofertil.colposcopia.util.Sesion;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador del módulo Citas (agenda).
 *
 * <p>GET (consultas):</p>
 * <ul>
 *   <li>{@code /citas} agenda desde hoy; filtros {@code fecha}, {@code estado}, {@code q}
 *       y {@code historico=1} para incluir citas pasadas</li>
 *   <li>{@code ?accion=nuevo} formulario vacío</li>
 *   <li>{@code ?accion=editar&id=N} formulario con la cita</li>
 * </ul>
 * <p>POST (modifican datos): {@code guardar}, {@code cambiarEstado}, {@code eliminar}.</p>
 *
 * <p>Permisos: Administrador y Auxiliar programan, editan y eliminan citas.
 * El Médico consulta la agenda y registra si la cita fue atendida o no.</p>
 */
@WebServlet(name = "CitaServlet", urlPatterns = {"/citas"})
public class CitaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(CitaServlet.class.getName());

    private static final String VISTA_LISTADO = "/WEB-INF/vistas/citas/listado.jsp";
    private static final String VISTA_FORMULARIO = "/WEB-INF/vistas/citas/formulario.jsp";
    private static final String VISTA_ERROR = "/WEB-INF/vistas/error.jsp";

    private final CitaDAO citaDAO = new CitaDAO();
    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final MedicoDAO medicoDAO = new MedicoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String accion = valorODefecto(req.getParameter("accion"), "listar");
        try {
            switch (accion) {
                case "nuevo":
                    if (puedeAgendar(req, resp)) {
                        nuevo(req, resp);
                    }
                    break;
                case "editar":
                    if (puedeAgendar(req, resp)) {
                        editar(req, resp);
                    }
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
                    if (puedeAgendar(req, resp)) {
                        guardar(req, resp);
                    }
                    break;
                case "cambiarEstado":
                    cambiarEstado(req, resp);
                    break;
                case "eliminar":
                    if (puedeAgendar(req, resp)) {
                        eliminar(req, resp);
                    }
                    break;
                default:
                    resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Acción no válida");
            }
        } catch (SQLException e) {
            manejarErrorBaseDatos(e, req, resp);
        }
    }

    /** GET: agenda con filtros. Sin fecha ni histórico muestra las citas desde hoy. */
    private void listar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        LocalDate fecha = convertirFecha(req.getParameter("fecha"));
        boolean historico = "1".equals(req.getParameter("historico"));
        String estado = req.getParameter("estado");
        String texto = req.getParameter("q");
        LocalDate desde = historico ? null : LocalDate.now();

        req.setAttribute("citas", citaDAO.listar(fecha, desde, estado, texto));
        req.setAttribute("fecha", fecha);
        req.setAttribute("historico", historico);
        req.setAttribute("estadoFiltro", estado);
        req.setAttribute("filtro", texto);
        req.setAttribute("estados", Cita.ESTADOS);
        req.setAttribute("hoy", LocalDate.now());
        // Se conserva la consulta para volver a la misma vista después de una acción POST
        req.setAttribute("consulta", req.getQueryString() == null ? "" : req.getQueryString());
        req.getRequestDispatcher(VISTA_LISTADO).forward(req, resp);
    }

    /** GET: formulario vacío; acepta {@code idPaciente} y {@code fecha} para llegar prellenado. */
    private void nuevo(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Cita cita = new Cita();
        Integer idPaciente = convertirEntero(req.getParameter("idPaciente"));
        cita.setIdPaciente(idPaciente == null ? 0 : idPaciente);
        LocalDate fecha = convertirFecha(req.getParameter("fecha"));
        cita.setFecha(fecha == null ? LocalDate.now() : fecha);
        mostrarFormulario(req, resp, cita);
    }

    private void editar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Integer id = convertirEntero(req.getParameter("id"));
        Cita cita = (id == null) ? null : citaDAO.buscarPorId(id);
        if (cita == null) {
            Sesion.mensaje(req, "error", "La cita solicitada no existe.");
            resp.sendRedirect(req.getContextPath() + "/citas");
            return;
        }
        mostrarFormulario(req, resp, cita);
    }

    /** POST: valida, revisa cruces de horario y registra o actualiza la cita. */
    private void guardar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, ServletException, IOException {
        Cita cita = leerFormulario(req);
        if (cita.isNueva()) {
            cita.setEstado(Cita.PROGRAMADA); // toda cita nueva nace programada
        }

        List<String> errores = CitaValidador.validar(cita, LocalDate.now());
        if (errores.isEmpty() && !Cita.CANCELADA.equals(cita.getEstado())) {
            if (citaDAO.medicoOcupado(cita)) {
                errores.add("El médico ya tiene una cita a menos de " + CitaDAO.DURACION_MINUTOS
                        + " minutos de esa hora. Elija otra hora.");
            }
            if (citaDAO.pacienteOcupada(cita)) {
                errores.add("La paciente ya tiene una cita a menos de " + CitaDAO.DURACION_MINUTOS
                        + " minutos de esa hora.");
            }
        }
        if (!errores.isEmpty()) {
            req.setAttribute("errores", errores);
            mostrarFormulario(req, resp, cita);
            return;
        }

        try {
            if (cita.isNueva()) {
                citaDAO.insertar(cita);
                Sesion.mensaje(req, "exito", "Cita programada.");
            } else {
                citaDAO.actualizar(cita);
                Sesion.mensaje(req, "exito", "Cita actualizada.");
            }
            // Después de guardar se muestra la agenda del día de la cita
            resp.sendRedirect(req.getContextPath() + "/citas?fecha=" + cita.getFecha());
        } catch (SQLException e) {
            if (e.getErrorCode() != CitaDAO.ERROR_LLAVE_FORANEA) {
                throw e;
            }
            req.setAttribute("errores", List.of("La paciente o el médico seleccionados ya no existen."));
            mostrarFormulario(req, resp, cita);
        }
    }

    /** POST: marca la cita como atendida, cancelada, inasistencia o la reprograma. */
    private void cambiarEstado(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Integer id = convertirEntero(req.getParameter("id"));
        String estado = req.getParameter("estado");
        Cita cita = (id == null) ? null : citaDAO.buscarPorId(id);

        if (cita == null) {
            Sesion.mensaje(req, "error", "La cita ya no existe.");
        } else {
            List<String> errores = CitaValidador.validarEstado(estado, cita.getFecha(), LocalDate.now());
            if (!errores.isEmpty()) {
                Sesion.mensaje(req, "error", errores.get(0));
            } else {
                citaDAO.cambiarEstado(cita.getIdCita(), estado);
                cita.setEstado(estado);
                Sesion.mensaje(req, "exito", "Cita de " + cita.getNombrePaciente()
                        + " marcada como " + cita.getEstadoTexto().toLowerCase() + ".");
            }
        }
        volverAlListado(req, resp);
    }

    /** POST: elimina la cita si no tiene colposcopia registrada. */
    private void eliminar(HttpServletRequest req, HttpServletResponse resp)
            throws SQLException, IOException {
        Integer id = convertirEntero(req.getParameter("id"));
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Identificador no válido");
            return;
        }
        try {
            if (citaDAO.eliminar(id)) {
                Sesion.mensaje(req, "exito", "Cita eliminada.");
            } else {
                Sesion.mensaje(req, "error", "La cita ya no existe.");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() != CitaDAO.ERROR_TIENE_DEPENDENCIAS) {
                throw e;
            }
            Sesion.mensaje(req, "error",
                    "No se puede eliminar: la cita ya tiene una colposcopia registrada. Puede cancelarla en su lugar.");
        }
        volverAlListado(req, resp);
    }

    /** Devuelve 403 si el rol actual no puede programar citas. */
    private boolean puedeAgendar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Usuario usuario = Sesion.usuario(req);
        if (usuario == null || !usuario.isPuedeAgendar()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        return true;
    }

    /** Redirige a la agenda conservando los filtros que el usuario tenía (campo oculto "retorno"). */
    private void volverAlListado(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String retorno = req.getParameter("retorno");
        String destino = req.getContextPath() + "/citas";
        // Solo se acepta una cadena de consulta simple; nunca una URL completa
        if (retorno != null && retorno.matches("[\\w=&%+.\\-]*") && !retorno.isEmpty()) {
            destino += "?" + retorno;
        }
        resp.sendRedirect(destino);
    }

    private void mostrarFormulario(HttpServletRequest req, HttpServletResponse resp, Cita cita)
            throws SQLException, ServletException, IOException {
        req.setAttribute("cita", cita);
        req.setAttribute("pacientes", pacienteDAO.listar(null));
        req.setAttribute("medicos", medicoDAO.listar());
        req.setAttribute("estados", Cita.ESTADOS);
        req.setAttribute("hoy", LocalDate.now());
        req.setAttribute("horaMin", CitaValidador.HORA_APERTURA);
        req.setAttribute("horaMax", CitaValidador.HORA_ULTIMA_CITA);
        req.getRequestDispatcher(VISTA_FORMULARIO).forward(req, resp);
    }

    private Cita leerFormulario(HttpServletRequest req) {
        Cita c = new Cita();
        Integer id = convertirEntero(req.getParameter("idCita"));
        c.setIdCita(id == null ? 0 : id);
        Integer idPaciente = convertirEntero(req.getParameter("idPaciente"));
        c.setIdPaciente(idPaciente == null ? 0 : idPaciente);
        Integer idMedico = convertirEntero(req.getParameter("idMedico"));
        c.setIdMedico(idMedico == null ? 0 : idMedico);
        c.setFecha(convertirFecha(req.getParameter("fecha")));
        c.setHora(convertirHora(req.getParameter("hora")));
        String estado = req.getParameter("estado");
        c.setEstado(estado == null ? Cita.PROGRAMADA : estado.trim());
        return c;
    }

    private void manejarErrorBaseDatos(SQLException e, HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        LOG.log(Level.SEVERE, "Error de base de datos en el módulo Citas", e);
        req.setAttribute("detalleError",
                "No fue posible completar la operación con la base de datos. Verifique que MySQL esté activo.");
        req.getRequestDispatcher(VISTA_ERROR).forward(req, resp);
    }

    private static String valorODefecto(String valor, String defecto) {
        return (valor == null || valor.isBlank()) ? defecto : valor;
    }

    private static Integer convertirEntero(String valor) {
        try {
            return (valor == null || valor.isBlank()) ? null : Integer.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate convertirFecha(String valor) {
        try {
            return (valor == null || valor.isBlank()) ? null : LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalTime convertirHora(String valor) {
        try {
            return (valor == null || valor.isBlank()) ? null : LocalTime.parse(valor.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
