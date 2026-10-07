package com.endofertil.colposcopia.util;

import com.endofertil.colposcopia.modelo.Usuario;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reglas de validación del lado del servidor para las cuentas de usuario.
 * Se separa del servlet para poder probarla con JUnit sin servidor.
 */
public final class UsuarioValidador {

    private static final Pattern NOMBRE_USUARIO = Pattern.compile("[A-Za-z0-9._-]{4,50}");
    private static final Pattern TIENE_LETRA = Pattern.compile(".*[A-Za-zÁÉÍÓÚáéíóúÑñ].*");
    private static final Pattern TIENE_DIGITO = Pattern.compile(".*\\d.*");
    /** Longitud mínima de la contraseña. */
    public static final int MIN_CLAVE = 8;
    private static final int MAX_CLAVE = 64;

    private UsuarioValidador() {
        // Clase utilitaria: no se instancia
    }

    /**
     * Valida los datos de una cuenta antes de guardarla.
     *
     * @param u cuenta a validar (nombre de usuario y rol)
     * @param clave contraseña digitada; puede ir vacía al editar para conservar la actual
     * @param confirmacion repetición de la contraseña
     * @return lista de mensajes de error; vacía si los datos son válidos
     */
    public static List<String> validar(Usuario u, String clave, String confirmacion) {
        List<String> errores = new ArrayList<>();

        if (estaVacio(u.getUsuario())) {
            errores.add("El nombre de usuario es obligatorio.");
        } else if (!NOMBRE_USUARIO.matcher(u.getUsuario()).matches()) {
            errores.add("El nombre de usuario debe tener entre 4 y 50 caracteres: letras, números, punto, guion o guion bajo, sin espacios.");
        }
        if (estaVacio(u.getRol()) || !Usuario.ROLES.contains(u.getRol())) {
            errores.add("Seleccione un rol válido.");
        }

        // Al crear la cuenta la contraseña es obligatoria; al editar solo se valida si se escribió
        if (u.isNuevo() || !estaVacio(clave)) {
            errores.addAll(validarClave(clave, confirmacion));
        }
        return errores;
    }

    /**
     * Reglas de una contraseña nueva.
     *
     * @param clave contraseña digitada
     * @param confirmacion repetición de la contraseña
     * @return lista de errores; vacía si la contraseña es aceptable
     */
    public static List<String> validarClave(String clave, String confirmacion) {
        List<String> errores = new ArrayList<>();
        if (estaVacio(clave)) {
            errores.add("La contraseña es obligatoria.");
            return errores;
        }
        if (clave.length() < MIN_CLAVE || clave.length() > MAX_CLAVE) {
            errores.add("La contraseña debe tener entre " + MIN_CLAVE + " y " + MAX_CLAVE + " caracteres.");
        }
        if (!TIENE_LETRA.matcher(clave).matches() || !TIENE_DIGITO.matcher(clave).matches()) {
            errores.add("La contraseña debe combinar letras y números.");
        }
        if (!clave.equals(confirmacion)) {
            errores.add("La confirmación no coincide con la contraseña.");
        }
        return errores;
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
