package com.endofertil.colposcopia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.endofertil.colposcopia.modelo.Paciente;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de las reglas de validación del registro de pacientes.
 * Ejecutar en NetBeans con clic derecho sobre el proyecto > Test (Alt+F6).
 */
class PacienteValidadorTest {

    private Paciente paciente;

    @BeforeEach
    void crearPacienteValida() {
        paciente = new Paciente();
        paciente.setTipoDocumento("CC");
        paciente.setNumeroDocumento("1023456789");
        paciente.setNombres("Ana María");
        paciente.setApellidos("Pérez Sánchez");
        paciente.setFechaNacimiento(LocalDate.of(1990, 5, 14));
        paciente.setTelefono("3001234567");
        paciente.setCorreo("ana.perez@correo.com");
    }

    @Test
    void pacienteCompletaNoTieneErrores() {
        assertTrue(PacienteValidador.validar(paciente).isEmpty());
    }

    @Test
    void camposOpcionalesVaciosSonValidos() {
        paciente.setTelefono("");
        paciente.setCorreo(null);
        paciente.setDireccion("");
        assertTrue(PacienteValidador.validar(paciente).isEmpty());
    }

    @Test
    void documentoConEspaciosEsRechazado() {
        paciente.setNumeroDocumento("102 345 678");
        assertEquals(1, PacienteValidador.validar(paciente).size());
    }

    @Test
    void fechaDeNacimientoFuturaEsRechazada() {
        paciente.setFechaNacimiento(LocalDate.now().plusDays(1));
        List<String> errores = PacienteValidador.validar(paciente);
        assertTrue(errores.contains("La fecha de nacimiento no puede ser posterior a hoy."));
    }

    @Test
    void correoSinDominioEsRechazado() {
        paciente.setCorreo("ana.perez@");
        assertEquals(1, PacienteValidador.validar(paciente).size());
    }

    @Test
    void pacienteVaciaReportaTodosLosObligatorios() {
        List<String> errores = PacienteValidador.validar(new Paciente());
        // tipo, número, nombres, apellidos y fecha de nacimiento
        assertEquals(5, errores.size());
    }
}
