-- =====================================================================
-- Ajustes y datos de prueba para los módulos Usuarios y Citas
-- Ejecutar UNA sola vez sobre bd_colposcopia (después del script actual)
-- =====================================================================
USE bd_colposcopia;

-- 1. La tabla usuario tiene dos índices UNIQUE iguales sobre la columna usuario
--    (uq_usuario y uk_usuario). Se elimina el repetido.
--    Si sale "Can't DROP 'uk_usuario'", ya estaba eliminado: continúe.
ALTER TABLE usuario DROP INDEX uk_usuario;

-- 2. Índice para que la agenda filtre rápido por fecha y hora
CREATE INDEX idx_cita_fecha_hora ON cita (fecha, hora);

-- 3. Usuarios de prueba (contraseñas ya cifradas con PBKDF2)
--    auxiliar   / Auxiliar2026  -> rol Auxiliar
--    dr.martinez / Medico2026   -> rol Medico
--    El usuario "mairon" (clave 123456 en texto plano) se cifra solo
--    la primera vez que inicie sesión.
INSERT IGNORE INTO usuario (usuario, password_hash, rol, estado) VALUES
    ('auxiliar',    'pbkdf2$120000$3D4qsls33NZybNCA2dCqxQ==$jlDMfbU26f0j16f17qfBdNYN+hkKcULF2ipubf8DP3k=', 'Auxiliar', 1),
    ('dr.martinez', 'pbkdf2$120000$F1q5YgzYE9f96W61Y2wxeQ==$cc0rH7SSdkNBsz+skgOCjSAowVqtZBFHOn7Pzki5RvY=', 'Medico',   1);

-- 4. Segundo médico, para probar cruces de horario entre médicos
INSERT IGNORE INTO medico (nombres, apellidos, registro_medico, especialidad, telefono, correo)
VALUES ('María', 'López Ruiz', 'RM-55821', 'Ginecología', '6017654321', 'mlopez@endofertil.com');

-- 5. Citas de ejemplo: hoy, mañana y una pasada
INSERT INTO cita (fecha, hora, estado, id_paciente, id_medico)
SELECT CURDATE(), '09:00:00', 'Programada', p.id_paciente, m.id_medico
FROM paciente p, medico m
WHERE p.numero_documento = '125487544' AND m.registro_medico = 'RM-123456';

INSERT INTO cita (fecha, hora, estado, id_paciente, id_medico)
SELECT CURDATE() + INTERVAL 1 DAY, '10:30:00', 'Programada', p.id_paciente, m.id_medico
FROM paciente p, medico m
WHERE p.numero_documento = '987654321' AND m.registro_medico = 'RM-55821';

INSERT INTO cita (fecha, hora, estado, id_paciente, id_medico)
SELECT CURDATE() - INTERVAL 3 DAY, '08:00:00', 'Atendida', p.id_paciente, m.id_medico
FROM paciente p, medico m
WHERE p.numero_documento = '125487544' AND m.registro_medico = 'RM-123456';
