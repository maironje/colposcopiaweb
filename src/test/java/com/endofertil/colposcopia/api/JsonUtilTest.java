package com.endofertil.colposcopia.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la lectura de peticiones JSON y del formato de las respuestas de la API.
 */
class JsonUtilTest {

    @Test
    void leeLosCamposDeUnObjetoJson() {
        JsonObject o = JsonUtil.convertirObjeto("{\"usuario\":\"mairon\",\"clave\":\"Clave2026\"}");
        assertEquals("mairon", JsonUtil.texto(o, "usuario"));
        assertEquals("Clave2026", JsonUtil.texto(o, "clave"));
    }

    @Test
    void textoQueNoEsJsonDevuelveNulo() {
        assertNull(JsonUtil.convertirObjeto("usuario=mairon&clave=123"));
        assertNull(JsonUtil.convertirObjeto(""));
        assertNull(JsonUtil.convertirObjeto("[1, 2, 3]"), "un arreglo no es un objeto");
    }

    @Test
    void campoAusenteONuloDevuelveNulo() {
        JsonObject o = JsonUtil.convertirObjeto("{\"usuario\":null}");
        assertNull(JsonUtil.texto(o, "usuario"));
        assertNull(JsonUtil.texto(o, "clave"));
    }

    @Test
    void respuestaExitosaTieneElMensajeDelCaso() {
        String json = JsonUtil.aJson(RespuestaApi.ok("Autenticación satisfactoria", null));
        assertEquals("{\"exito\":true,\"mensaje\":\"Autenticación satisfactoria\"}", json);
    }

    @Test
    void respuestaDeErrorIncluyeElDetalle() {
        String json = JsonUtil.aJson(RespuestaApi.error("Error en la autenticación",
                List.of("Usuario o contraseña incorrectos.")));
        assertTrue(json.contains("\"exito\":false"));
        assertTrue(json.contains("\"errores\":[\"Usuario o contraseña incorrectos.\"]"));
        assertFalse(json.contains("datos"), "los campos nulos no se envían");
    }
}
