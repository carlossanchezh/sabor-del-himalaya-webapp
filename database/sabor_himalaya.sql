-- MySQL dump 10.13  Distrib 8.0.45, for Win64 (x86_64)
--
-- Host: localhost    Database: sabor_himalaya
-- ------------------------------------------------------
-- Server version	9.6.0

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
SET @MYSQLDUMP_TEMP_LOG_BIN = @@SESSION.SQL_LOG_BIN;
SET @@SESSION.SQL_LOG_BIN= 0;

--
-- Table structure for table `linea_pedido_menudiario`
--

DROP TABLE IF EXISTS `linea_pedido_menudiario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `linea_pedido_menudiario` (
  `id_linea_pedido_menu_diario` bigint NOT NULL AUTO_INCREMENT,
  `id_pedido` bigint NOT NULL,
  `id_menu_diario` bigint NOT NULL,
  `id_entrante` bigint NOT NULL,
  `id_principal` bigint NOT NULL,
  `id_postre` bigint NOT NULL,
  `id_bebida` bigint NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(8,2) NOT NULL,
  PRIMARY KEY (`id_linea_pedido_menu_diario`),
  KEY `fk_lpmd_pedido` (`id_pedido`),
  KEY `fk_lpmd_menu` (`id_menu_diario`),
  KEY `fk_lpmd_entrante` (`id_entrante`),
  KEY `fk_lpmd_principal` (`id_principal`),
  KEY `fk_lpmd_postre` (`id_postre`),
  KEY `fk_lpmd_bebida` (`id_bebida`),
  CONSTRAINT `fk_lpmd_bebida` FOREIGN KEY (`id_bebida`) REFERENCES `plato` (`id_plato`),
  CONSTRAINT `fk_lpmd_entrante` FOREIGN KEY (`id_entrante`) REFERENCES `plato` (`id_plato`),
  CONSTRAINT `fk_lpmd_menu` FOREIGN KEY (`id_menu_diario`) REFERENCES `menu_diario` (`id_menu_diario`),
  CONSTRAINT `fk_lpmd_pedido` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id_pedido`) ON DELETE CASCADE,
  CONSTRAINT `fk_lpmd_postre` FOREIGN KEY (`id_postre`) REFERENCES `plato` (`id_plato`),
  CONSTRAINT `fk_lpmd_principal` FOREIGN KEY (`id_principal`) REFERENCES `plato` (`id_plato`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `linea_pedido_menudiario`
--

LOCK TABLES `linea_pedido_menudiario` WRITE;
/*!40000 ALTER TABLE `linea_pedido_menudiario` DISABLE KEYS */;
/*!40000 ALTER TABLE `linea_pedido_menudiario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `linea_pedido_plato`
--

DROP TABLE IF EXISTS `linea_pedido_plato`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `linea_pedido_plato` (
  `id_linea_pedido_plato` bigint NOT NULL AUTO_INCREMENT,
  `id_pedido` bigint NOT NULL,
  `id_plato` bigint NOT NULL,
  `cantidad` int NOT NULL,
  `precio_unitario` decimal(8,2) NOT NULL,
  PRIMARY KEY (`id_linea_pedido_plato`),
  KEY `fk_lpp_pedido` (`id_pedido`),
  KEY `fk_lpp_plato` (`id_plato`),
  CONSTRAINT `fk_lpp_pedido` FOREIGN KEY (`id_pedido`) REFERENCES `pedido` (`id_pedido`) ON DELETE CASCADE,
  CONSTRAINT `fk_lpp_plato` FOREIGN KEY (`id_plato`) REFERENCES `plato` (`id_plato`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `linea_pedido_plato`
--

LOCK TABLES `linea_pedido_plato` WRITE;
/*!40000 ALTER TABLE `linea_pedido_plato` DISABLE KEYS */;
/*!40000 ALTER TABLE `linea_pedido_plato` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menu_diario`
--

DROP TABLE IF EXISTS `menu_diario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu_diario` (
  `id_menu_diario` bigint NOT NULL AUTO_INCREMENT,
  `fecha` date NOT NULL,
  `precio` decimal(8,2) NOT NULL DEFAULT '15.00',
  PRIMARY KEY (`id_menu_diario`),
  UNIQUE KEY `Fecha` (`fecha`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu_diario`
--

LOCK TABLES `menu_diario` WRITE;
/*!40000 ALTER TABLE `menu_diario` DISABLE KEYS */;
/*!40000 ALTER TABLE `menu_diario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `menu_diario_opciones`
--

DROP TABLE IF EXISTS `menu_diario_opciones`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `menu_diario_opciones` (
  `id_menu_diario_opciones` bigint NOT NULL AUTO_INCREMENT,
  `id_menu_diario` bigint NOT NULL,
  `id_plato` bigint NOT NULL,
  `categoria` enum('ENTRANTE','PRINCIPAL','POSTRE') COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id_menu_diario_opciones`),
  KEY `fk_mdo_menu` (`id_menu_diario`),
  KEY `fk_mdo_plato` (`id_plato`),
  CONSTRAINT `fk_mdo_menu` FOREIGN KEY (`id_menu_diario`) REFERENCES `menu_diario` (`id_menu_diario`) ON DELETE CASCADE,
  CONSTRAINT `fk_mdo_plato` FOREIGN KEY (`id_plato`) REFERENCES `plato` (`id_plato`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `menu_diario_opciones`
--

LOCK TABLES `menu_diario_opciones` WRITE;
/*!40000 ALTER TABLE `menu_diario_opciones` DISABLE KEYS */;
/*!40000 ALTER TABLE `menu_diario_opciones` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `pedido`
--

DROP TABLE IF EXISTS `pedido`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pedido` (
  `id_pedido` bigint NOT NULL AUTO_INCREMENT,
  `id_usuario` bigint NOT NULL,
  `direccion_entrega` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `importe_total` decimal(10,2) NOT NULL,
  `estado` enum('RECIBIDO','EN_PREPARACION','EN_REPARTO','ENTREGADO','CANCELADO') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'RECIBIDO',
  PRIMARY KEY (`id_pedido`),
  KEY `fk_pedido_usuario` (`id_usuario`),
  CONSTRAINT `fk_pedido_usuario` FOREIGN KEY (`id_usuario`) REFERENCES `usuario` (`id_usuario`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `pedido`
--

LOCK TABLES `pedido` WRITE;
/*!40000 ALTER TABLE `pedido` DISABLE KEYS */;
/*!40000 ALTER TABLE `pedido` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `plato`
--

DROP TABLE IF EXISTS `plato`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `plato` (
  `id_plato` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `descripcion` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `precio` decimal(8,2) NOT NULL,
  `categoria` enum('ENTRANTE','PRINCIPAL','POSTRE','BEBIDA') COLLATE utf8mb4_unicode_ci NOT NULL,
  `esta_en_carta` tinyint(1) NOT NULL,
  `es_del_chef` tinyint(1) NOT NULL,
  PRIMARY KEY (`id_plato`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `plato`
--

LOCK TABLES `plato` WRITE;
/*!40000 ALTER TABLE `plato` DISABLE KEYS */;
/*!40000 ALTER TABLE `plato` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id_usuario` bigint NOT NULL AUTO_INCREMENT,
  `nombre` varchar(120) COLLATE utf8mb4_unicode_ci NOT NULL,
  `direccion` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `correo` varchar(150) COLLATE utf8mb4_unicode_ci NOT NULL,
  `login` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `contrasena` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `estado_activacion` tinyint(1) NOT NULL DEFAULT '0',
  `rol` enum('CLIENTE','DUENO','CHEF','STAFF') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'CLIENTE',
  `token` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id_usuario`),
  UNIQUE KEY `Correo` (`correo`),
  UNIQUE KEY `Login` (`login`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;
SET @@SESSION.SQL_LOG_BIN = @MYSQLDUMP_TEMP_LOG_BIN;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-30  9:57:07
