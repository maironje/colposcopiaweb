-- Datos de prueba para el módulo Pacientes.
-- Ejecutar DESPUÉS de primer_script_bd_colposcopia.sql
USE bd_colposcopia;

INSERT IGNORE INTO paciente
    (tipo_documento, numero_documento, nombres, apellidos, fecha_nacimiento, telefono, correo, direccion, eps)
VALUES
    ('CC', '1234567890', 'Ana María', 'Pérez Sánchez', '1988-03-12', '3001234567', 'ana.perez@correo.com', 'Calle 45 # 12-30', 'Sanitas'),
    ('CC', '1088765432', 'Laura', 'Gómez Romero', '1992-07-25', '3019876543', NULL, 'Carrera 7 # 80-15', 'Sura'),
    ('CC', '1122334455', 'Carolina', 'Torres Díaz', '1985-11-02', '3105553344', 'carolina.td@correo.com', NULL, 'Nueva EPS'),
    ('CE', 'E998877', 'Juliana', 'Ramírez López', '1995-01-30', '3204442211', NULL, 'Av. Boyacá # 63-10', 'Salud Total');

-- Médico y cita de ejemplo: permiten probar que NO se elimina una paciente con citas
INSERT IGNORE INTO medico (nombres, apellidos, registro_medico, especialidad, telefono, correo)
VALUES ('María', 'López', 'RM-55821', 'Ginecología', '6017654321', 'mlopez@endofertil.com');

INSERT INTO cita (fecha, hora, estado, id_paciente, id_medico)
SELECT '2026-10-20', '08:00:00', 'Programada', p.id_paciente, m.id_medico
FROM paciente p, medico m
WHERE p.numero_documento = '1234567890' AND m.registro_medico = 'RM-55821'
  AND NOT EXISTS (SELECT 1 FROM cita c WHERE c.id_paciente = p.id_paciente);
