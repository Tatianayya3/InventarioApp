-- Script de inicialización para base de datos inventario_db
-- Ejecutar en MySQL Workbench o cliente MySQL

CREATE DATABASE inventario_db;
USE inventario_db;

-- Crear usuario específico para la aplicación
CREATE USER 'inv_user'@'localhost' IDENTIFIED BY 'inv_pass';
GRANT ALL PRIVILEGES ON inventario_db.* TO 'inv_user'@'localhost';
FLUSH PRIVILEGES;

-- Crear tabla productos
CREATE TABLE productos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(32) NOT NULL UNIQUE,
    nombre VARCHAR(120) NOT NULL,
    categoria VARCHAR(60) NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    activo TINYINT(1) NOT NULL DEFAULT 1,

    INDEX idx_codigo (codigo),
    INDEX idx_categoria (categoria)
);

-- Insertar datos de prueba
INSERT INTO productos (codigo, nombre, categoria, precio, stock, activo) VALUES
('LAP001', 'Laptop Dell Inspiron 15', 'Electronicos', 2499.99, 5, 1),
('MOU001', 'Mouse Inalámbrico Logitech', 'Accesorios', 79.99, 25, 1),
('CHR001', 'Silla Ergonómica de Oficina', 'Muebles', 899.99, 3, 1);

-- Verificar inserción
SELECT * FROM productos;