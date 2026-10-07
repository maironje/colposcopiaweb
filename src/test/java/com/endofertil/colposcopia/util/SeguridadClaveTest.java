package com.endofertil.colposcopia.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del cifrado de contraseñas con PBKDF2.
 */
class SeguridadClaveTest {

    @Test
    void laClaveCorrectaSeVerifica() {
        String hash = SeguridadClave.generarHash("Endofertil2026");
        assertTrue(SeguridadClave.verificar("Endofertil2026", hash));
    }

    @Test
    void unaClaveIncorrectaSeRechaza() {
        String hash = SeguridadClave.generarHash("Endofertil2026");
        assertFalse(SeguridadClave.verificar("endofertil2026", hash));
    }

    @Test
    void laMismaClaveProduceHashesDistintos() {
        // Cada hash lleva una sal aleatoria propia
        assertNotEquals(SeguridadClave.generarHash("Clave1234"), SeguridadClave.generarHash("Clave1234"));
    }

    @Test
    void elHashNoContieneLaClave() {
        assertFalse(SeguridadClave.generarHash("Clave1234").contains("Clave1234"));
    }

    @Test
    void reconoceClavesAntiguasEnTextoPlano() {
        assertFalse(SeguridadClave.esHash("123456"));
        assertTrue(SeguridadClave.verificar("123456", "123456"));
        assertTrue(SeguridadClave.esHash(SeguridadClave.generarHash("123456")));
    }

    @Test
    void valoresNulosNoSeAceptan() {
        assertFalse(SeguridadClave.verificar(null, "123456"));
        assertFalse(SeguridadClave.verificar("123456", null));
    }
}
