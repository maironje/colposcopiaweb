CREATE DATABASE IF NOT EXISTS bd_colposcopia;

USE bd_colposcopia;

-- ==========================================
-- TABLA PACIENTE
-- ==========================================

CREATE TABLE paciente (
    id_paciente INT AUTO_INCREMENT,
    tipo_documento VARCHAR(20) NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    nombres VARCHAR(60) NOT NULL,
    apellidos VARCHAR(60) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    telefono VARCHAR(20),
    correo VARCHAR(100),
    direccion VARCHAR(150),
    eps VARCHAR(100),

    CONSTRAINT pk_paciente
        PRIMARY KEY (id_paciente),

    CONSTRAINT uq_paciente_documento
        UNIQUE (numero_documento)
);

-- ==========================================
-- TABLA MEDICO
-- ==========================================

CREATE TABLE medico (
    id_medico INT AUTO_INCREMENT,
    nombres VARCHAR(60) NOT NULL,
    apellidos VARCHAR(60) NOT NULL,
    registro_medico VARCHAR(30) NOT NULL,
    especialidad VARCHAR(80) NOT NULL,
    telefono VARCHAR(20),
    correo VARCHAR(100),

    CONSTRAINT pk_medico
        PRIMARY KEY (id_medico),

    CONSTRAINT uq_medico_registro
        UNIQUE (registro_medico)
);

-- ==========================================
-- TABLA USUARIO
-- ==========================================

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT,
    usuario VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_usuario
        PRIMARY KEY (id_usuario),

    CONSTRAINT uq_usuario
        UNIQUE (usuario),

    CONSTRAINT chk_usuario_rol
        CHECK (rol IN ('Administrador', 'Medico', 'Auxiliar'))
);

-- ==========================================
-- TABLA CITA
-- ==========================================

CREATE TABLE cita (
    id_cita INT AUTO_INCREMENT,
    fecha DATE NOT NULL,
    hora TIME NOT NULL,
    estado VARCHAR(20) NOT NULL,
    id_paciente INT NOT NULL,
    id_medico INT NOT NULL,

    CONSTRAINT pk_cita
        PRIMARY KEY (id_cita),

    CONSTRAINT chk_cita_estado
        CHECK (
            estado IN (
                'Programada',
                'Atendida',
                'Cancelada',
                'No asistio'
            )
        ),

    CONSTRAINT fk_cita_paciente
        FOREIGN KEY (id_paciente)
        REFERENCES paciente(id_paciente)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_cita_medico
        FOREIGN KEY (id_medico)
        REFERENCES medico(id_medico)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- ==========================================
-- TABLA COLPOSCOPIA
-- ==========================================

CREATE TABLE colposcopia (
    id_colposcopia INT AUTO_INCREMENT,
    fecha DATE NOT NULL,
    hallazgos TEXT,
    observaciones TEXT,
    id_cita INT NOT NULL,

    CONSTRAINT pk_colposcopia
        PRIMARY KEY (id_colposcopia),

    CONSTRAINT uq_colposcopia_cita
        UNIQUE (id_cita),

    CONSTRAINT fk_colposcopia_cita
        FOREIGN KEY (id_cita)
        REFERENCES cita(id_cita)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- ==========================================
-- TABLA DIAGNOSTICO
-- ==========================================

CREATE TABLE diagnostico (
    id_diagnostico INT AUTO_INCREMENT,
    descripcion TEXT NOT NULL,
    cie10 VARCHAR(10) NOT NULL,
    id_colposcopia INT NOT NULL,

    CONSTRAINT pk_diagnostico
        PRIMARY KEY (id_diagnostico),

    CONSTRAINT fk_diagnostico_colposcopia
        FOREIGN KEY (id_colposcopia)
        REFERENCES colposcopia(id_colposcopia)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ==========================================
-- TABLA BIOPSIA
-- ==========================================

CREATE TABLE biopsia (
    id_biopsia INT AUTO_INCREMENT,
    sitio_toma VARCHAR(100) NOT NULL,
    resultado TEXT,
    fecha_resultado DATE,
    id_colposcopia INT NOT NULL,

    CONSTRAINT pk_biopsia
        PRIMARY KEY (id_biopsia),

    CONSTRAINT fk_biopsia_colposcopia
        FOREIGN KEY (id_colposcopia)
        REFERENCES colposcopia(id_colposcopia)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ==========================================
-- TABLA IMAGEN
-- ==========================================

CREATE TABLE imagen (
    id_imagen INT AUTO_INCREMENT,
    ruta_imagen VARCHAR(255) NOT NULL,
    descripcion VARCHAR(200),
    fecha DATE NOT NULL,
    id_colposcopia INT NOT NULL,

    CONSTRAINT pk_imagen
        PRIMARY KEY (id_imagen),

    CONSTRAINT fk_imagen_colposcopia
        FOREIGN KEY (id_colposcopia)
        REFERENCES colposcopia(id_colposcopia)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);
