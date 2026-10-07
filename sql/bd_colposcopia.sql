-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: bd_colposcopia
-- ------------------------------------------------------
-- Server version	8.0.40

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `biopsia`
--

DROP TABLE IF EXISTS `biopsia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `biopsia` (
  `id_biopsia` int NOT NULL AUTO_INCREMENT,
  `sitio_toma` varchar(100) NOT NULL,
  `resultado` text,
  `fecha_resultado` date DEFAULT NULL,
  `id_colposcopia` int NOT NULL,
  PRIMARY KEY (`id_biopsia`),
  KEY `fk_biopsia_colposcopia` (`id_colposcopia`),
  CONSTRAINT `fk_biopsia_colposcopia` FOREIGN KEY (`id_colposcopia`) REFERENCES `colposcopia` (`id_colposcopia`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `biopsia`
--

LOCK TABLES `biopsia` WRITE;
/*!40000 ALTER TABLE `biopsia` DISABLE KEYS */;
/*!40000 ALTER TABLE `biopsia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cita`
--

DROP TABLE IF EXISTS `cita`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cita` (
  `id_cita` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `hora` time NOT NULL,
  `estado` varchar(20) NOT NULL,
  `id_paciente` int NOT NULL,
  `id_medico` int NOT NULL,
  PRIMARY KEY (`id_cita`),
  KEY `fk_cita_paciente` (`id_paciente`),
  KEY `fk_cita_medico` (`id_medico`),
  CONSTRAINT `fk_cita_medico` FOREIGN KEY (`id_medico`) REFERENCES `medico` (`id_medico`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_cita_paciente` FOREIGN KEY (`id_paciente`) REFERENCES `paciente` (`id_paciente`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `chk_cita_estado` CHECK ((`estado` in (_utf8mb4'Programada',_utf8mb4'Atendida',_utf8mb4'Cancelada',_utf8mb4'No asistio')))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cita`
--

LOCK TABLES `cita` WRITE;
/*!40000 ALTER TABLE `cita` DISABLE KEYS */;
/*!40000 ALTER TABLE `cita` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `colposcopia`
--

DROP TABLE IF EXISTS `colposcopia`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `colposcopia` (
  `id_colposcopia` int NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `hallazgos` text,
  `observaciones` text,
  `id_cita` int NOT NULL,
  PRIMARY KEY (`id_colposcopia`),
  UNIQUE KEY `uq_colposcopia_cita` (`id_cita`),
  CONSTRAINT `fk_colposcopia_cita` FOREIGN KEY (`id_cita`) REFERENCES `cita` (`id_cita`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `colposcopia`
--

LOCK TABLES `colposcopia` WRITE;
/*!40000 ALTER TABLE `colposcopia` DISABLE KEYS */;
/*!40000 ALTER TABLE `colposcopia` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `diagnostico`
--

DROP TABLE IF EXISTS `diagnostico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diagnostico` (
  `id_diagnostico` int NOT NULL AUTO_INCREMENT,
  `descripcion` text NOT NULL,
  `cie10` varchar(10) NOT NULL,
  `id_colposcopia` int NOT NULL,
  PRIMARY KEY (`id_diagnostico`),
  KEY `fk_diagnostico_colposcopia` (`id_colposcopia`),
  CONSTRAINT `fk_diagnostico_colposcopia` FOREIGN KEY (`id_colposcopia`) REFERENCES `colposcopia` (`id_colposcopia`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `diagnostico`
--

LOCK TABLES `diagnostico` WRITE;
/*!40000 ALTER TABLE `diagnostico` DISABLE KEYS */;
/*!40000 ALTER TABLE `diagnostico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `imagen`
--

DROP TABLE IF EXISTS `imagen`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `imagen` (
  `id_imagen` int NOT NULL AUTO_INCREMENT,
  `ruta_imagen` varchar(255) NOT NULL,
  `descripcion` varchar(200) DEFAULT NULL,
  `fecha` date NOT NULL,
  `id_colposcopia` int NOT NULL,
  PRIMARY KEY (`id_imagen`),
  KEY `fk_imagen_colposcopia` (`id_colposcopia`),
  CONSTRAINT `fk_imagen_colposcopia` FOREIGN KEY (`id_colposcopia`) REFERENCES `colposcopia` (`id_colposcopia`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `imagen`
--

LOCK TABLES `imagen` WRITE;
/*!40000 ALTER TABLE `imagen` DISABLE KEYS */;
/*!40000 ALTER TABLE `imagen` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `medico`
--

DROP TABLE IF EXISTS `medico`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `medico` (
  `id_medico` int NOT NULL AUTO_INCREMENT,
  `nombres` varchar(60) NOT NULL,
  `apellidos` varchar(60) NOT NULL,
  `registro_medico` varchar(30) NOT NULL,
  `especialidad` varchar(80) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_medico`),
  UNIQUE KEY `uq_medico_registro` (`registro_medico`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `medico`
--

LOCK TABLES `medico` WRITE;
/*!40000 ALTER TABLE `medico` DISABLE KEYS */;
INSERT INTO `medico` VALUES (1,'Carlos Andres','Martinez Perez','RM-123456','Ginecologia','3001234567','carlos.martinez@gmail.com');
/*!40000 ALTER TABLE `medico` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `paciente`
--

DROP TABLE IF EXISTS `paciente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `paciente` (
  `id_paciente` int NOT NULL AUTO_INCREMENT,
  `tipo_documento` varchar(20) NOT NULL,
  `numero_documento` varchar(20) NOT NULL,
  `nombres` varchar(60) NOT NULL,
  `apellidos` varchar(60) NOT NULL,
  `fecha_nacimiento` date NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `correo` varchar(100) DEFAULT NULL,
  `direccion` varchar(150) DEFAULT NULL,
  `eps` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id_paciente`),
  UNIQUE KEY `uq_paciente_documento` (`numero_documento`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `paciente`
--

LOCK TABLES `paciente` WRITE;
/*!40000 ALTER TABLE `paciente` DISABLE KEYS */;
INSERT INTO `paciente` VALUES (6,'cedula','987654321','Luciana','Medez martines','2026-10-06','3256845212','maironje@gmail.com','Corozal sucre','Mutual Ser'),(7,'CC','125487544','Mairon','Medez Martinez','1995-01-11','3005878888','prueba@gmail.com','corozal','Compensar');
/*!40000 ALTER TABLE `paciente` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` int NOT NULL AUTO_INCREMENT,
  `usuario` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `rol` varchar(30) NOT NULL,
  `estado` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `uq_usuario` (`usuario`),
  UNIQUE KEY `uk_usuario` (`usuario`),
  CONSTRAINT `chk_usuario_rol` CHECK ((`rol` in (_utf8mb4'Administrador',_utf8mb4'Medico',_utf8mb4'Auxiliar')))
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,'mairon','123456','Administrador',1);
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-07 15:10:15
