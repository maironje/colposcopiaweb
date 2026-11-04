package com.endofertil.colposcopia.api;

import com.endofertil.colposcopia.servicio.AutenticacionServicio;
import com.endofertil.colposcopia.servicio.AutenticacionServicio.ResultadoLogin;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servicio web de inicio de sesión.
 *
 * <pre>
 * POST /api/login
 * Content-Type: application/json
 * { "usuario": "mairon", "clave": "123456" }
 * </pre>
 *
 * <ul>
 *   <li>{@code 200 OK}: "Autenticación satisfactoria".</li>
 *   <li>{@code 401 Unauthorized}: "Error en la autenticación" (usuario o contraseña incorrectos,
 *       o cuenta inactiva).</li>
 *   <li>{@code 400 Bad Request}: el cuerpo no es JSON o faltan campos.</li>
 *   <li>{@code 500 Internal Server Error}: falla de la base de datos.</li>
 * </ul>
 */
@WebServlet(name = "LoginApiServlet", urlPatterns = {"/api/login"})
public class LoginApiServlet extends ApiServletBase {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LoginApiServlet.class.getName());

    /** Mensaje que exige el caso de la evidencia cuando la autenticación es correcta. */
    public static final String MENSAJE_EXITO = "Autenticación satisfactoria";
    /** Mensaje que exige el caso de la evidencia cuando la autenticación falla. */
    public static final String MENSAJE_ERROR = "Error en la autenticación";

    private final transient AutenticacionServicio servicio = new AutenticacionServicio();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. Leer y validar el cuerpo JSON
        JsonObject cuerpo = esJson(req) ? JsonUtil.leerObjeto(req) : null;
        String usuario = JsonUtil.texto(cuerpo, "usuario");
        String clave = JsonUtil.texto(cuerpo, "clave");
        if (cuerpo == null || usuario == null || usuario.isBlank() || clave == null || clave.isEmpty()) {
            JsonUtil.responder(resp, HttpServletResponse.SC_BAD_REQUEST, RespuestaApi.error("Solicitud inválida",
                    List.of("Envíe un JSON con los campos \"usuario\" y \"clave\" (Content-Type: application/json).")));
            return;
        }

        try {
            // 2. Autenticar con la misma regla que usa la pantalla de login
            ResultadoLogin resultado = servicio.autenticar(usuario, clave);

            // 3. Responder según el resultado
            switch (resultado.getEstado()) {
                case EXITOSO:
                    Map<String, Object> datos = new LinkedHashMap<>();
                    datos.put("usuario", resultado.getUsuario().getUsuario());
                    datos.put("rol", resultado.getUsuario().getRol());
                    JsonUtil.responder(resp, HttpServletResponse.SC_OK, RespuestaApi.ok(MENSAJE_EXITO, datos));
                    break;
                case CUENTA_INACTIVA:
                    JsonUtil.responder(resp, HttpServletResponse.SC_UNAUTHORIZED, RespuestaApi.error(MENSAJE_ERROR,
                            List.of("La cuenta está inactiva. Un administrador debe activarla.")));
                    break;
                default:
                    // No se dice si falló el usuario o la contraseña: no revela qué cuentas existen
                    JsonUtil.responder(resp, HttpServletResponse.SC_UNAUTHORIZED, RespuestaApi.error(MENSAJE_ERROR,
                            List.of("Usuario o contraseña incorrectos.")));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos en /api/login", e);
            JsonUtil.responder(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RespuestaApi.error(
                    "Error interno", List.of("No fue posible conectarse a la base de datos.")));
        }
    }
}
