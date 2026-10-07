package com.endofertil.colposcopia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.endofertil.colposcopia.modelo.Usuario;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las reglas de validación de cuentas de usuario.
 */
class UsuarioValidadorTest {

    private Usuario usuario;

    @BeforeEach
    void crearUsuarioValido() {
        usuario = new Usuario();
        usuario.setUsuario("laura.gomez");
        usuario.setRol(Usuario.ROL_AUXILIAR);
    }

    @Test
    void usuarioNuevoCompletoNoTieneErrores() {
        assertTrue(UsuarioValidador.validar(usuario, "Clave2026", "Clave2026").isEmpty());
    }

    @Test
    void usuarioNuevoSinClaveEsRechazado() {
        List<String> errores = UsuarioValidador.validar(usuario, "", "");
        assertTrue(errores.contains("La contraseña es obligatoria."));
    }

    @Test
    void alEditarSePuedeDejarLaClaveVacia() {
        usuario.setIdUsuario(5);
        assertTrue(UsuarioValidador.validar(usuario, "", "").isEmpty());
    }

    @Test
    void claveSinNumerosEsRechazada() {
        List<String> errores = UsuarioValidador.validar(usuario, "SoloLetras", "SoloLetras");
        assertTrue(errores.contains("La contraseña debe combinar letras y números."));
    }

    @Test
    void claveCortaEsRechazada() {
        assertEquals(1, UsuarioValidador.validar(usuario, "Ab1", "Ab1").size());
    }

    @Test
    void confirmacionDistintaEsRechazada() {
        List<String> errores = UsuarioValidador.validar(usuario, "Clave2026", "Clave2027");
        assertTrue(errores.contains("La confirmación no coincide con la contraseña."));
    }

    @Test
    void nombreConEspaciosEsRechazado() {
        usuario.setUsuario("laura gomez");
        assertEquals(1, UsuarioValidador.validar(usuario, "Clave2026", "Clave2026").size());
    }

    @Test
    void rolFueraDeLaListaEsRechazado() {
        usuario.setRol("Gerente");
        assertTrue(UsuarioValidador.validar(usuario, "Clave2026", "Clave2026")
                .contains("Seleccione un rol válido."));
    }
}
