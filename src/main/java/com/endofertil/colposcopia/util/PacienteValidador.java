package com.endofertil.colposcopia.util;

import com.endofertil.colposcopia.modelo.Paciente;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reglas de validación del lado del servidor para el registro de pacientes.
 * Se separa del servlet para poder probarla con JUnit sin servidor.
 */
public final class PacienteValidador {

    private static final Pattern DOCUMENTO = Pattern.compile("[A-Za-z0-9]{5,20}");
    private static final Pattern TELEFONO = Pattern.compile("\\d{7,15}");
    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final int MAX_NOMBRE = 60;
    private static final int MAX_CORREO = 100;
    private static final int MAX_DIRECCION = 150;

    private PacienteValidador() {
        // Clase utilitaria: no se instancia
    }

    /**
     * Valida los datos de una paciente antes de guardarlos.
     *
     * @param p paciente a validar
     * @return lista de mensajes de error; vacía si los datos son válidos
     */
    public static List<String> validar(Paciente p) {
        List<String> errores = new ArrayList<>();

        if (estaVacio(p.getTipoDocumento())) {
            errores.add("Seleccione el tipo de documento.");
        }
        if (estaVacio(p.getNumeroDocumento())) {
            errores.add("El número de documento es obligatorio.");
        } else if (!DOCUMENTO.matcher(p.getNumeroDocumento()).matches()) {
            errores.add("El número de documento debe tener entre 5 y 20 letras o dígitos, sin espacios.");
        }
        validarNombre(p.getNombres(), "Los nombres", errores);
        validarNombre(p.getApellidos(), "Los apellidos", errores);

        if (p.getFechaNacimiento() == null) {
            errores.add("La fecha de nacimiento es obligatoria.");
        } else if (p.getFechaNacimiento().isAfter(LocalDate.now())) {
            errores.add("La fecha de nacimiento no puede ser posterior a hoy.");
        }
        if (!estaVacio(p.getTelefono()) && !TELEFONO.matcher(p.getTelefono()).matches()) {
            errores.add("El teléfono debe contener entre 7 y 15 dígitos.");
        }
        if (!estaVacio(p.getCorreo())
                && (p.getCorreo().length() > MAX_CORREO || !CORREO.matcher(p.getCorreo()).matches())) {
            errores.add("El correo electrónico no tiene un formato válido.");
        }
        if (!estaVacio(p.getDireccion()) && p.getDireccion().length() > MAX_DIRECCION) {
            errores.add("La dirección no puede superar " + MAX_DIRECCION + " caracteres.");
        }
        return errores;
    }

    private static void validarNombre(String valor, String campo, List<String> errores) {
        if (estaVacio(valor)) {
            errores.add(campo + " son obligatorios.");
        } else if (valor.length() > MAX_NOMBRE) {
            errores.add(campo + " no pueden superar " + MAX_NOMBRE + " caracteres.");
        }
    }

    private static boolean estaVacio(String valor) {
        return valor == null || valor.isBlank();
    }
}
