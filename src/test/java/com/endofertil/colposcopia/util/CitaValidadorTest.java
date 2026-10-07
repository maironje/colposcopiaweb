package com.endofertil.colposcopia.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.endofertil.colposcopia.modelo.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas de las reglas de programación de citas. Se usa una fecha fija
 * como "hoy" para que los resultados no dependan del día en que se ejecuten.
 */
class CitaValidadorTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 7);
    private Cita cita;

    @BeforeEach
    void crearCitaValida() {
        cita = new Cita();
        cita.setIdPaciente(7);
        cita.setIdMedico(1);
        cita.setFecha(HOY.plusDays(1));
        cita.setHora(LocalTime.of(9, 30));
    }

    @Test
    void citaCompletaNoTieneErrores() {
        assertTrue(CitaValidador.validar(cita, HOY).isEmpty());
    }

    @Test
    void citaNuevaEnFechaPasadaEsRechazada() {
        cita.setFecha(HOY.minusDays(1));
        assertTrue(CitaValidador.validar(cita, HOY)
                .contains("No se puede programar una cita en una fecha pasada."));
    }

    @Test
    void citaExistenteEnFechaPasadaSePuedeEditar() {
        cita.setIdCita(3);
        cita.setFecha(HOY.minusDays(2));
        cita.setEstado(Cita.ATENDIDA);
        assertTrue(CitaValidador.validar(cita, HOY).isEmpty());
    }

    @Test
    void horaFueraDelHorarioEsRechazada() {
        cita.setHora(LocalTime.of(6, 30));
        assertEquals(1, CitaValidador.validar(cita, HOY).size());
        cita.setHora(LocalTime.of(18, 0));
        assertEquals(1, CitaValidador.validar(cita, HOY).size());
    }

    @Test
    void limitesDelHorarioSonValidos() {
        cita.setHora(CitaValidador.HORA_APERTURA);
        assertTrue(CitaValidador.validar(cita, HOY).isEmpty());
        cita.setHora(CitaValidador.HORA_ULTIMA_CITA);
        assertTrue(CitaValidador.validar(cita, HOY).isEmpty());
    }

    @Test
    void citaVaciaReportaTodosLosObligatorios() {
        List<String> errores = CitaValidador.validar(new Cita(), HOY);
        // paciente, médico, fecha y hora
        assertEquals(4, errores.size());
    }

    @Test
    void citaFuturaNoPuedeMarcarseAtendida() {
        assertEquals(1, CitaValidador.validarEstado(Cita.ATENDIDA, HOY.plusDays(1), HOY).size());
        assertEquals(1, CitaValidador.validarEstado(Cita.NO_ASISTIO, HOY.plusDays(1), HOY).size());
        assertTrue(CitaValidador.validarEstado(Cita.CANCELADA, HOY.plusDays(1), HOY).isEmpty());
    }

    @Test
    void citaDeHoySePuedeMarcarAtendida() {
        assertTrue(CitaValidador.validarEstado(Cita.ATENDIDA, HOY, HOY).isEmpty());
    }

    @Test
    void estadoFueraDeLaListaEsRechazado() {
        assertTrue(CitaValidador.validarEstado("Pendiente", HOY, HOY).contains("Seleccione un estado válido."));
    }
}
