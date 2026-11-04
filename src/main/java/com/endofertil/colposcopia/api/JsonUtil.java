package com.endofertil.colposcopia.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utilidades para leer peticiones JSON y escribir respuestas JSON desde los servlets de la API.
 * Usa la librería Gson (dependencia {@code com.google.code.gson:gson} en el pom.xml).
 */
public final class JsonUtil {

    /** Instancia compartida: Gson es seguro para usarse desde varios hilos. */
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    /** Tamaño máximo aceptado para el cuerpo de una petición (evita cuerpos gigantes). */
    private static final int MAX_CARACTERES = 10_000;

    private JsonUtil() {
        // Clase utilitaria: no se instancia
    }

    /**
     * Lee el cuerpo de la petición y lo interpreta como un objeto JSON.
     *
     * @param req petición HTTP
     * @return el objeto JSON, o {@code null} si el cuerpo está vacío, no es JSON o no es un objeto
     * @throws IOException si falla la lectura
     */
    public static JsonObject leerObjeto(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        StringBuilder cuerpo = new StringBuilder();
        try (BufferedReader lector = req.getReader()) {
            char[] buffer = new char[1024];
            int leidos;
            while ((leidos = lector.read(buffer)) != -1) {
                cuerpo.append(buffer, 0, leidos);
                if (cuerpo.length() > MAX_CARACTERES) {
                    return null;
                }
            }
        }
        return convertirObjeto(cuerpo.toString());
    }

    /**
     * Convierte un texto en objeto JSON (separado para poder probarlo sin servidor).
     *
     * @param texto contenido recibido
     * @return el objeto JSON o {@code null} si el texto no es un objeto JSON válido
     */
    public static JsonObject convertirObjeto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            JsonElement elemento = JsonParser.parseString(texto);
            return elemento.isJsonObject() ? elemento.getAsJsonObject() : null;
        } catch (JsonParseException e) {
            return null;
        }
    }

    /**
     * Obtiene un campo de texto de un objeto JSON.
     *
     * @param objeto objeto JSON recibido
     * @param campo nombre del campo
     * @return el valor como texto, o {@code null} si no existe, es nulo o no es texto/número
     */
    public static String texto(JsonObject objeto, String campo) {
        if (objeto == null || !objeto.has(campo) || objeto.get(campo).isJsonNull()) {
            return null;
        }
        JsonElement valor = objeto.get(campo);
        return valor.isJsonPrimitive() ? valor.getAsString() : null;
    }

    /**
     * Convierte cualquier objeto a texto JSON.
     *
     * @param objeto objeto a convertir
     * @return representación JSON
     */
    public static String aJson(Object objeto) {
        return GSON.toJson(objeto);
    }

    /**
     * Escribe la respuesta JSON con su código de estado HTTP.
     *
     * @param resp respuesta HTTP
     * @param estado código HTTP (200, 201, 400, 401, 409, 500...)
     * @param cuerpo objeto a enviar como JSON
     * @throws IOException si falla la escritura
     */
    public static void responder(HttpServletResponse resp, int estado, Object cuerpo) throws IOException {
        resp.setStatus(estado);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        // Las respuestas de la API nunca se guardan en caché
        resp.setHeader("Cache-Control", "no-store");
        try (PrintWriter salida = resp.getWriter()) {
            salida.write(aJson(cuerpo));
        }
    }
}
