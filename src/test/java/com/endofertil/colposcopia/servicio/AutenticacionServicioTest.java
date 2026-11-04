package com.endofertil.colposcopia.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.endofertil.colposcopia.modelo.Usuario;
import com.endofertil.colposcopia.servicio.AutenticacionServicio.EstadoLogin;
import com.endofertil.colposcopia.servicio.AutenticacionServicio.EstadoRegistro;
import com.endofertil.colposcopia.servicio.AutenticacionServicio.ResultadoLogin;
import com.endofertil.colposcopia.servicio.AutenticacionServicio.ResultadoRegistro;
import com.endofertil.colposcopia.util.SeguridadClave;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de la lógica del servicio web de registro e inicio de sesión (AA5-EV01).
 */
class AutenticacionServicioTest {

    private UsuarioDAOEnMemoria dao;
    private AutenticacionServicio servicio;

    @BeforeEach
    void preparar() {
        dao = new UsuarioDAOEnMemoria();
        servicio = new AutenticacionServicio(dao);
        dao.agregar("mairon", SeguridadClave.generarHash("Endofertil2026"), Usuario.ROL_ADMINISTRADOR, true);
    }

    // ---------- Inicio de sesión ----------

    @Test
    void credencialesCorrectasDanAutenticacionSatisfactoria() throws SQLException {
        ResultadoLogin r = servicio.autenticar("mairon", "Endofertil2026");
        assertTrue(r.isExitoso());
        assertEquals("mairon", r.getUsuario().getUsuario());
        assertNull(r.getUsuario().getPasswordHash(), "el hash nunca sale del servicio");
    }

    @Test
    void claveIncorrectaDaErrorDeAutenticacion() throws SQLException {
        assertEquals(EstadoLogin.CREDENCIALES_INVALIDAS, servicio.autenticar("mairon", "otraClave1").getEstado());
    }

    @Test
    void usuarioInexistenteDaElMismoErrorQueClaveIncorrecta() throws SQLException {
        assertEquals(EstadoLogin.CREDENCIALES_INVALIDAS, servicio.autenticar("nadie", "Endofertil2026").getEstado());
    }

    @Test
    void camposVaciosDanErrorDeAutenticacion() throws SQLException {
        assertFalse(servicio.autenticar("", "").isExitoso());
        assertFalse(servicio.autenticar(null, null).isExitoso());
    }

    @Test
    void cuentaInactivaNoSeAutentica() throws SQLException {
        dao.agregar("inactivo", SeguridadClave.generarHash("Clave2026"), Usuario.ROL_AUXILIAR, false);
        assertEquals(EstadoLogin.CUENTA_INACTIVA, servicio.autenticar("inactivo", "Clave2026").getEstado());
    }

    @Test
    void claveAntiguaEnTextoPlanoSeMigraAHash() throws SQLException {
        Usuario antiguo = dao.agregar("antiguo", "123456", Usuario.ROL_ADMINISTRADOR, true);
        assertTrue(servicio.autenticar("antiguo", "123456").isExitoso());
        assertTrue(SeguridadClave.esHash(antiguo.getPasswordHash()));
        assertTrue(servicio.autenticar("antiguo", "123456").isExitoso(), "sigue entrando después de migrar");
    }

    // ---------- Registro ----------

    @Test
    void registroValidoCreaCuentaInactivaConRolAuxiliar() throws SQLException {
        ResultadoRegistro r = servicio.registrar("laura.gomez", "Clave2026");
        assertEquals(EstadoRegistro.REGISTRADO, r.getEstado());
        Usuario guardado = dao.buscarPorUsuario("laura.gomez");
        assertEquals(Usuario.ROL_AUXILIAR, guardado.getRol());
        assertFalse(guardado.isActivo());
        assertTrue(SeguridadClave.esHash(guardado.getPasswordHash()), "la clave se guarda cifrada");
    }

    @Test
    void cuentaRegistradaPuedeEntrarCuandoSeActiva() throws SQLException {
        servicio.registrar("laura.gomez", "Clave2026");
        assertEquals(EstadoLogin.CUENTA_INACTIVA, servicio.autenticar("laura.gomez", "Clave2026").getEstado());
        dao.buscarPorUsuario("laura.gomez").setActivo(true); // lo que hace el administrador
        assertTrue(servicio.autenticar("laura.gomez", "Clave2026").isExitoso());
    }

    @Test
    void registroConUsuarioExistenteEsRechazado() throws SQLException {
        assertEquals(EstadoRegistro.USUARIO_EXISTENTE, servicio.registrar("mairon", "Clave2026").getEstado());
    }

    @Test
    void registroConClaveDebilEsRechazado() throws SQLException {
        ResultadoRegistro r = servicio.registrar("laura.gomez", "123");
        assertEquals(EstadoRegistro.DATOS_INVALIDOS, r.getEstado());
        assertFalse(r.getErrores().isEmpty());
        assertNull(dao.buscarPorUsuario("laura.gomez"));
    }

    @Test
    void registroSinDatosReportaErrores() throws SQLException {
        ResultadoRegistro r = servicio.registrar(null, null);
        assertEquals(EstadoRegistro.DATOS_INVALIDOS, r.getEstado());
        assertEquals(2, r.getErrores().size()); // usuario y contraseña
    }
}
