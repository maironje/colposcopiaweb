package com.endofertil.colposcopia.api;

import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Comportamiento común de los servicios web que solo aceptan POST con cuerpo JSON.
 *
 * <p>Cualquier otro método (GET, PUT, DELETE) responde {@code 405 Method Not Allowed}
 * en formato JSON, para que el cliente sepa qué hacer en lugar de recibir una página HTML.</p>
 */
public abstract class ApiServletBase extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /** Código HTTP 405; la API de Servlet 4 no trae constante con este nombre en español. */
    protected static final int METODO_NO_PERMITIDO = HttpServletResponse.SC_METHOD_NOT_ALLOWED;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        responderMetodoNoPermitido(resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        responderMetodoNoPermitido(resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        responderMetodoNoPermitido(resp);
    }

    private void responderMetodoNoPermitido(HttpServletResponse resp) throws IOException {
        resp.setHeader("Allow", "POST");
        JsonUtil.responder(resp, METODO_NO_PERMITIDO, RespuestaApi.error("Método no permitido",
                List.of("Este servicio solo acepta peticiones POST con un cuerpo JSON.")));
    }

    /**
     * Verifica que la petición declare un cuerpo JSON.
     *
     * @param req petición HTTP
     * @return {@code true} si el Content-Type es application/json
     */
    protected static boolean esJson(HttpServletRequest req) {
        String tipo = req.getContentType();
        return tipo != null && tipo.toLowerCase().startsWith("application/json");
    }
}
