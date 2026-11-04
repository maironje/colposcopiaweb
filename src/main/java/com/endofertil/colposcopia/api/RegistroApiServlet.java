package com.endofertil.colposcopia.api;

import com.endofertil.colposcopia.modelo.Usuario;
import com.endofertil.colposcopia.servicio.AutenticacionServicio;
import com.endofertil.colposcopia.servicio.AutenticacionServicio.ResultadoRegistro;
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
 * Servicio web de registro de usuarios.
 *
 * <pre>
 * POST /api/registro
 * Content-Type: application/json
 * { "usuario": "laura.gomez", "clave": "Clave2026" }
 * </pre>
 *
 * <ul>
 *   <li>{@code 201 Created}: usuario registrado (queda inactivo hasta que un administrador lo active).</li>
 *   <li>{@code 400 Bad Request}: cuerpo no es JSON o los datos no cumplen las reglas.</li>
 *   <li>{@code 409 Conflict}: ya existe una cuenta con ese nombre de usuario.</li>
 *   <li>{@code 500 Internal Server Error}: falla de la base de datos.</li>
 * </ul>
 */
@WebServlet(name = "RegistroApiServlet", urlPatterns = {"/api/registro"})
public class RegistroApiServlet extends ApiServletBase {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(RegistroApiServlet.class.getName());

    private final transient AutenticacionServicio servicio = new AutenticacionServicio();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. El cuerpo debe ser un objeto JSON
        JsonObject cuerpo = esJson(req) ? JsonUtil.leerObjeto(req) : null;
        if (cuerpo == null) {
            JsonUtil.responder(resp, HttpServletResponse.SC_BAD_REQUEST, RespuestaApi.error("Solicitud inválida",
                    List.of("Envíe un JSON con los campos \"usuario\" y \"clave\" (Content-Type: application/json).")));
            return;
        }

        try {
            // 2. Registrar con las reglas del negocio (validación, duplicados y cifrado)
            ResultadoRegistro resultado = servicio.registrar(
                    JsonUtil.texto(cuerpo, "usuario"), JsonUtil.texto(cuerpo, "clave"));

            // 3. Traducir el resultado a un código HTTP y un mensaje
            switch (resultado.getEstado()) {
                case REGISTRADO:
                    JsonUtil.responder(resp, HttpServletResponse.SC_CREATED, RespuestaApi.ok(
                            "Usuario registrado correctamente. La cuenta quedará habilitada cuando "
                                    + "un administrador la active.",
                            datosUsuario(resultado.getUsuario())));
                    break;
                case USUARIO_EXISTENTE:
                    JsonUtil.responder(resp, HttpServletResponse.SC_CONFLICT,
                            RespuestaApi.error("Error en el registro", resultado.getErrores()));
                    break;
                default:
                    JsonUtil.responder(resp, HttpServletResponse.SC_BAD_REQUEST,
                            RespuestaApi.error("Error en el registro", resultado.getErrores()));
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Error de base de datos en /api/registro", e);
            JsonUtil.responder(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, RespuestaApi.error(
                    "Error interno", List.of("No fue posible conectarse a la base de datos.")));
        }
    }

    /** Datos públicos de la cuenta creada (nunca se devuelve la contraseña ni su hash). */
    private static Map<String, Object> datosUsuario(Usuario u) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("idUsuario", u.getIdUsuario());
        datos.put("usuario", u.getUsuario());
        datos.put("rol", u.getRol());
        datos.put("activo", u.isActivo());
        return datos;
    }
}
