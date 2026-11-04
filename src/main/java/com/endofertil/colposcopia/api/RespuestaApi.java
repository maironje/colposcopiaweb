package com.endofertil.colposcopia.api;

import java.util.List;

/**
 * Cuerpo JSON que devuelven todos los servicios web del sistema.
 *
 * <pre>
 * {
 *   "exito": true,
 *   "mensaje": "Autenticación satisfactoria",
 *   "datos":   { ... },            // opcional
 *   "errores": [ "..." ]           // opcional
 * }
 * </pre>
 *
 * <p>Los campos nulos no se incluyen en el JSON (comportamiento por defecto de Gson).</p>
 */
public final class RespuestaApi {

    private final boolean exito;
    private final String mensaje;
    private final Object datos;
    private final List<String> errores;

    private RespuestaApi(boolean exito, String mensaje, Object datos, List<String> errores) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.datos = datos;
        this.errores = (errores == null || errores.isEmpty()) ? null : errores;
    }

    /**
     * @param mensaje mensaje para el cliente
     * @param datos información adicional (puede ser {@code null})
     * @return respuesta de operación exitosa
     */
    public static RespuestaApi ok(String mensaje, Object datos) {
        return new RespuestaApi(true, mensaje, datos, null);
    }

    /**
     * @param mensaje mensaje general del error
     * @param errores detalle de cada problema (puede ser {@code null})
     * @return respuesta de operación fallida
     */
    public static RespuestaApi error(String mensaje, List<String> errores) {
        return new RespuestaApi(false, mensaje, null, errores);
    }

    public boolean isExito() {
        return exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public Object getDatos() {
        return datos;
    }

    public List<String> getErrores() {
        return errores;
    }
}
