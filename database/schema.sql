-- ============================================================================
-- Sistema de Reservas de Aulas - Esquema MySQL 8
-- Equivale a lo que Hibernate crea con spring.jpa.hibernate.ddl-auto=update.
-- Ejecución manual (opcional):  mysql -u root -p < database/schema.sql
-- ============================================================================

CREATE DATABASE IF NOT EXISTS reservas_aulas
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE reservas_aulas;

-- ----------------------------------------------------------------------------
-- Usuarios. "password" guarda el hash BCrypt; es NULL mientras la persona
-- no haya creado su contraseña de acceso (no puede iniciar sesión).
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    nombre    VARCHAR(100) NOT NULL,
    correo    VARCHAR(120) NOT NULL,
    rol       VARCHAR(30)  NOT NULL,
    password  VARCHAR(100) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_correo UNIQUE (correo)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS aulas (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(50)  NOT NULL,
    capacidad   INT          NOT NULL,
    ubicacion   VARCHAR(100) NOT NULL,
    tipo        VARCHAR(50)  NOT NULL,
    disponible  BIT(1)       NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS cursos (
    id      BIGINT       NOT NULL AUTO_INCREMENT,
    nombre  VARCHAR(100) NOT NULL,
    horas   INT          NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS reservas (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    aula_id      BIGINT      NOT NULL,
    usuario_id   BIGINT      NOT NULL,
    curso_id     BIGINT      NULL,
    fecha        DATE        NOT NULL,
    hora_inicio  TIME        NOT NULL,
    hora_fin     TIME        NOT NULL,
    estado       VARCHAR(30) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_reservas_aula    FOREIGN KEY (aula_id)    REFERENCES aulas (id),
    CONSTRAINT fk_reservas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_reservas_curso   FOREIGN KEY (curso_id)   REFERENCES cursos (id)
) ENGINE = InnoDB;
