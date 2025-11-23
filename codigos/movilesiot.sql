-- phpMyAdmin SQL Dump
-- version 5.2.3
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 23-11-2025 a las 02:57:42
-- Versión del servidor: 10.5.29-MariaDB
-- Versión de PHP: 8.4.13

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `movilesiot`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `DEPARTAMENTOS`
--

CREATE TABLE `DEPARTAMENTOS` (
  `id_departamento` int(11) NOT NULL,
  `numero` varchar(10) NOT NULL,
  `torre` varchar(50) DEFAULT NULL,
  `otros_datos` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `EVENTOS_ACCESO`
--

CREATE TABLE `EVENTOS_ACCESO` (
  `id_evento` int(11) NOT NULL,
  `id_sensor` int(11) DEFAULT NULL,
  `id_usuario_ejecutor` int(11) DEFAULT NULL,
  `tipo_evento` enum('ACCESO_VALIDO','ACCESO_RECHAZADO','APERTURA_MANUAL','CIERRE_MANUAL') NOT NULL,
  `fecha_hora` datetime NOT NULL,
  `resultado` enum('PERMITIDO','DENEGADO') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `SENSORES`
--

CREATE TABLE `SENSORES` (
  `id_sensor` int(11) NOT NULL,
  `codigo_sensor` varchar(50) NOT NULL,
  `id_departamento` int(11) NOT NULL,
  `id_usuario_asociado` int(11) DEFAULT NULL,
  `tipo` enum('TARJETA','LLAVERO') NOT NULL,
  `estado` enum('ACTIVO','INACTIVO','PERDIDO','BLOQUEADO') NOT NULL DEFAULT 'ACTIVO',
  `fecha_alta` datetime NOT NULL,
  `fecha_baja` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci;

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `USUARIOS`
--

CREATE TABLE `USUARIOS` (
  `id_usuario` int(11) NOT NULL,
  `id_departamento` int(11) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `rol` enum('ADMINISTRADOR','OPERADOR') NOT NULL,
  `es_administrador` tinyint(1) NOT NULL,
  `estado` enum('ACTIVO','INACTIVO','BLOQUEADO') NOT NULL DEFAULT 'ACTIVO',
  `otros_datos` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_spanish_ci;

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `DEPARTAMENTOS`
--
ALTER TABLE `DEPARTAMENTOS`
  ADD PRIMARY KEY (`id_departamento`),
  ADD UNIQUE KEY `numero` (`numero`);

--
-- Indices de la tabla `EVENTOS_ACCESO`
--
ALTER TABLE `EVENTOS_ACCESO`
  ADD PRIMARY KEY (`id_evento`),
  ADD KEY `id_sensor` (`id_sensor`),
  ADD KEY `id_usuario_ejecutor` (`id_usuario_ejecutor`);

--
-- Indices de la tabla `SENSORES`
--
ALTER TABLE `SENSORES`
  ADD PRIMARY KEY (`id_sensor`),
  ADD UNIQUE KEY `codigo_sensor` (`codigo_sensor`),
  ADD KEY `id_departamento` (`id_departamento`),
  ADD KEY `id_usuario_asociado` (`id_usuario_asociado`);

--
-- Indices de la tabla `USUARIOS`
--
ALTER TABLE `USUARIOS`
  ADD PRIMARY KEY (`id_usuario`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_departamento` (`id_departamento`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `DEPARTAMENTOS`
--
ALTER TABLE `DEPARTAMENTOS`
  MODIFY `id_departamento` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `EVENTOS_ACCESO`
--
ALTER TABLE `EVENTOS_ACCESO`
  MODIFY `id_evento` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `SENSORES`
--
ALTER TABLE `SENSORES`
  MODIFY `id_sensor` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT de la tabla `USUARIOS`
--
ALTER TABLE `USUARIOS`
  MODIFY `id_usuario` int(11) NOT NULL AUTO_INCREMENT;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `EVENTOS_ACCESO`
--
ALTER TABLE `EVENTOS_ACCESO`
  ADD CONSTRAINT `EVENTOS_ACCESO_ibfk_1` FOREIGN KEY (`id_sensor`) REFERENCES `SENSORES` (`id_sensor`),
  ADD CONSTRAINT `EVENTOS_ACCESO_ibfk_2` FOREIGN KEY (`id_usuario_ejecutor`) REFERENCES `USUARIOS` (`id_usuario`);

--
-- Filtros para la tabla `SENSORES`
--
ALTER TABLE `SENSORES`
  ADD CONSTRAINT `SENSORES_ibfk_1` FOREIGN KEY (`id_departamento`) REFERENCES `DEPARTAMENTOS` (`id_departamento`),
  ADD CONSTRAINT `SENSORES_ibfk_2` FOREIGN KEY (`id_usuario_asociado`) REFERENCES `USUARIOS` (`id_usuario`);

--
-- Filtros para la tabla `USUARIOS`
--
ALTER TABLE `USUARIOS`
  ADD CONSTRAINT `USUARIOS_ibfk_1` FOREIGN KEY (`id_departamento`) REFERENCES `DEPARTAMENTOS` (`id_departamento`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
