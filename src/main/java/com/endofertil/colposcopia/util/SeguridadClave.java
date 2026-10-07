package com.endofertil.colposcopia.util;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Cifrado irreversible de contraseñas con PBKDF2 (HMAC-SHA256), incluido en el JDK.
 *
 * <p>El valor guardado en {@code usuario.password_hash} tiene el formato
 * {@code pbkdf2$iteraciones$sal$hash} (sal y hash en Base64). Cada contraseña
 * usa una sal aleatoria distinta, así dos usuarios con la misma clave tienen
 * hashes diferentes.</p>
 */
public final class SeguridadClave {

    private static final String PREFIJO = "pbkdf2";
    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 120_000;
    private static final int BYTES_SAL = 16;
    private static final int BITS_HASH = 256;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private SeguridadClave() {
        // Clase utilitaria: no se instancia
    }

    /**
     * Genera el hash de una contraseña para guardarlo en la base de datos.
     *
     * @param clave contraseña en texto plano
     * @return cadena con el formato {@code pbkdf2$iteraciones$sal$hash}
     */
    public static String generarHash(String clave) {
        byte[] sal = new byte[BYTES_SAL];
        ALEATORIO.nextBytes(sal);
        byte[] hash = derivar(clave, sal, ITERACIONES);
        Base64.Encoder b64 = Base64.getEncoder();
        return PREFIJO + "$" + ITERACIONES + "$" + b64.encodeToString(sal) + "$" + b64.encodeToString(hash);
    }

    /**
     * Comprueba una contraseña contra el valor guardado.
     * Si el valor guardado no es un hash (clave antigua en texto plano),
     * compara directamente para permitir la migración en el primer inicio de sesión.
     *
     * @param clave contraseña digitada
     * @param guardado valor de la columna {@code password_hash}
     * @return {@code true} si la contraseña es correcta
     */
    public static boolean verificar(String clave, String guardado) {
        if (clave == null || guardado == null) {
            return false;
        }
        if (!esHash(guardado)) {
            return MessageDigest.isEqual(
                    clave.getBytes(StandardCharsets.UTF_8), guardado.getBytes(StandardCharsets.UTF_8));
        }
        String[] partes = guardado.split("\\$");
        try {
            int iteraciones = Integer.parseInt(partes[1]);
            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            // Comparación en tiempo constante para no revelar información por tiempos de respuesta
            return MessageDigest.isEqual(esperado, derivar(clave, sal, iteraciones));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Indica si un valor guardado ya está cifrado con este esquema.
     *
     * @param guardado valor de la columna {@code password_hash}
     * @return {@code false} si es una contraseña antigua en texto plano
     */
    public static boolean esHash(String guardado) {
        return guardado != null && guardado.startsWith(PREFIJO + "$") && guardado.split("\\$").length == 4;
    }

    private static byte[] derivar(String clave, byte[] sal, int iteraciones) {
        PBEKeySpec spec = new PBEKeySpec(clave.toCharArray(), sal, iteraciones, BITS_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("El JDK no soporta " + ALGORITMO, e);
        } finally {
            spec.clearPassword();
        }
    }
}
