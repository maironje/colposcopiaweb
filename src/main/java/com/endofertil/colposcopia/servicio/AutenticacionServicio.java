package com.endofertil.colposcopia.servicio;

import com.endofertil.colposcopia.dao.UsuarioDAO;
import com.endofertil.colposcopia.modelo.Usuario;
import com.endofertil.colposcopia.util.SeguridadClave;
import com.endofertil.colposcopia.util.UsuarioValidador;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Lógica de negocio del registro y el inicio de sesión.
 *
 * <p>Se separa de los servlets para que la misma regla la usen la pantalla de login
 * ({@code LoginServlet}) y el servicio web ({@code LoginApiServlet}, {@code RegistroApiServlet}).
 * Así no hay dos versiones de "cómo se autentica un usuario" en el sistema.</p>
 *
 * <p>Recibe el {@link UsuarioDAO} por el constructor; en las pruebas unitarias se le entrega
 * un DAO en memoria y no se necesita MySQL.</p>
 */
public class AutenticacionServicio {

    private static final Logger LOG = Logger.getLogger(AutenticacionServicio.class.getName());

    /** Rol que recibe toda cuenta creada desde el servicio de registro. */
    public static final String ROL_REGISTRO = Usuario.ROL_AUXILIAR;

    /** Posibles resultados de un intento de inicio de sesión. */
    public enum EstadoLogin {
        /** Usuario y contraseña correctos y cuenta activa. */
        EXITOSO,
        /** El usuario no existe o la contraseña no coincide (no se distingue a propósito). */
        CREDENCIALES_INVALIDAS,
        /** Usuario y contraseña correctos, pero la cuenta está inactiva. */
        CUENTA_INACTIVA
    }

    /** Posibles resultados de un registro. */
    public enum EstadoRegistro {
        REGISTRADO,
        DATOS_INVALIDOS,
        USUARIO_EXISTENTE
    }

    /** Resultado de {@link #autenticar}: el estado y, si fue exitoso, el usuario sin su hash. */
    public static final class ResultadoLogin {
        private final EstadoLogin estado;
        private final Usuario usuario;

        private ResultadoLogin(EstadoLogin estado, Usuario usuario) {
            this.estado = estado;
            this.usuario = usuario;
        }

        public EstadoLogin getEstado() {
            return estado;
        }

        /** @return usuario autenticado (sin hash) o {@code null} si no fue exitoso */
        public Usuario getUsuario() {
            return usuario;
        }

        public boolean isExitoso() {
            return estado == EstadoLogin.EXITOSO;
        }
    }

    /** Resultado de {@link #registrar}: el estado, los errores de validación y la cuenta creada. */
    public static final class ResultadoRegistro {
        private final EstadoRegistro estado;
        private final List<String> errores;
        private final Usuario usuario;

        private ResultadoRegistro(EstadoRegistro estado, List<String> errores, Usuario usuario) {
            this.estado = estado;
            this.errores = errores;
            this.usuario = usuario;
        }

        public EstadoRegistro getEstado() {
            return estado;
        }

        /** @return mensajes de validación; lista vacía si no hubo errores */
        public List<String> getErrores() {
            return errores;
        }

        /** @return cuenta creada (sin hash) o {@code null} si no se registró */
        public Usuario getUsuario() {
            return usuario;
        }
    }

    private final UsuarioDAO usuarioDAO;

    /** Constructor usado por la aplicación: trabaja contra la base de datos real. */
    public AutenticacionServicio() {
        this(new UsuarioDAO());
    }

    /**
     * Constructor que permite inyectar el DAO (usado en las pruebas unitarias).
     *
     * @param usuarioDAO acceso a la tabla usuario
     */
    public AutenticacionServicio(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Verifica un usuario y una contraseña.
     *
     * <p>Si la contraseña guardada aún estaba en texto plano (cuentas antiguas),
     * se reemplaza por su hash PBKDF2 en este mismo momento.</p>
     *
     * @param nombreUsuario nombre de usuario digitado
     * @param clave contraseña digitada
     * @return resultado del intento
     * @throws SQLException si falla el acceso a la base de datos
     */
    public ResultadoLogin autenticar(String nombreUsuario, String clave) throws SQLException {
        if (estaVacio(nombreUsuario) || estaVacio(clave)) {
            return new ResultadoLogin(EstadoLogin.CREDENCIALES_INVALIDAS, null);
        }

        Usuario usuario = usuarioDAO.buscarPorUsuario(nombreUsuario.trim());
        if (usuario == null || !SeguridadClave.verificar(clave, usuario.getPasswordHash())) {
            return new ResultadoLogin(EstadoLogin.CREDENCIALES_INVALIDAS, null);
        }
        if (!usuario.isActivo()) {
            return new ResultadoLogin(EstadoLogin.CUENTA_INACTIVA, null);
        }

        // Migración: la contraseña antigua en texto plano se guarda cifrada desde ya
        if (!SeguridadClave.esHash(usuario.getPasswordHash())) {
            usuarioDAO.cambiarClave(usuario.getIdUsuario(), SeguridadClave.generarHash(clave));
            LOG.info("Contraseña del usuario " + usuario.getUsuario() + " migrada a PBKDF2.");
        }
        return new ResultadoLogin(EstadoLogin.EXITOSO, usuario.sinClave());
    }

    /**
     * Registra una cuenta nueva con rol {@link #ROL_REGISTRO} e <b>inactiva</b>.
     *
     * <p>El sistema maneja datos clínicos, por eso una cuenta creada desde el servicio
     * público no puede entrar hasta que un administrador la active en el módulo Usuarios.</p>
     *
     * @param nombreUsuario nombre de usuario deseado
     * @param clave contraseña (mínimo 8 caracteres con letras y números)
     * @return resultado del registro
     * @throws SQLException si falla el acceso a la base de datos
     */
    public ResultadoRegistro registrar(String nombreUsuario, String clave) throws SQLException {
        Usuario nuevo = new Usuario();
        nuevo.setUsuario(nombreUsuario == null ? null : nombreUsuario.trim());
        nuevo.setRol(ROL_REGISTRO);
        nuevo.setActivo(false);

        // Se reutilizan las mismas reglas del formulario web; la confirmación es la misma clave
        List<String> errores = UsuarioValidador.validar(nuevo, clave, clave);
        if (!errores.isEmpty()) {
            return new ResultadoRegistro(EstadoRegistro.DATOS_INVALIDOS, errores, null);
        }
        if (usuarioDAO.buscarPorUsuario(nuevo.getUsuario()) != null) {
            return new ResultadoRegistro(EstadoRegistro.USUARIO_EXISTENTE,
                    List.of("Ya existe una cuenta con el usuario " + nuevo.getUsuario() + "."), null);
        }

        nuevo.setPasswordHash(SeguridadClave.generarHash(clave));
        try {
            usuarioDAO.insertar(nuevo);
        } catch (SQLException e) {
            // Dos registros simultáneos con el mismo nombre: lo detecta el índice UNIQUE
            if (e.getErrorCode() == UsuarioDAO.ERROR_DUPLICADO) {
                return new ResultadoRegistro(EstadoRegistro.USUARIO_EXISTENTE,
                        List.of("Ya existe una cuenta con el usuario " + nuevo.getUsuario() + "."), null);
            }
            throw e;
        }
        return new ResultadoRegistro(EstadoRegistro.REGISTRADO, Collections.emptyList(), nuevo.sinClave());
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
