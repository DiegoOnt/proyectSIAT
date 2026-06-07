-- Script de base de datos para sistema de inventario y auditorías con trazabilidad y alertas
CREATE DATABASE IF NOT EXISTS siat_inventario CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE siat_inventario;

CREATE TABLE roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) NOT NULL,
    level INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150) NOT NULL,
    role_id INT NOT NULL,
    status ENUM('ACTIVE','SUSPENDED','DELETED') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    FOREIGN KEY (role_id) REFERENCES roles(role_id)
);

CREATE TABLE assets (
    asset_id INT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL,
    location VARCHAR(100) NOT NULL,
    status ENUM('IN_SERVICE','UNDER_MAINTENANCE','RETIRED','LOST') NOT NULL DEFAULT 'IN_SERVICE',
    lifecycle_stage VARCHAR(100) NOT NULL DEFAULT 'OPERACIÓN',
    owner VARCHAR(150) NULL,
    purchase_date DATE NULL,
    warranty_expiry DATE NULL,
    cost DECIMAL(12,2) NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INT NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

CREATE TABLE asset_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT NOT NULL,
    previous_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NOT NULL,
    changed_by INT NOT NULL,
    change_reason VARCHAR(255) NULL,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id),
    FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

CREATE TABLE audits (
    audit_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NULL,
    action VARCHAR(100) NOT NULL,
    target_table VARCHAR(100) NULL,
    target_id VARCHAR(100) NULL,
    success BOOLEAN NOT NULL DEFAULT TRUE,
    details TEXT NULL,
    event_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45) NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT NULL,
    alert_type VARCHAR(100) NOT NULL,
    alert_level ENUM('INFO','WARNING','CRITICAL') NOT NULL DEFAULT 'WARNING',
    message VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMP NULL,
    resolved_by INT NULL,
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id),
    FOREIGN KEY (resolved_by) REFERENCES users(user_id)
);

INSERT INTO roles (name, description, level) VALUES
('Administrador', 'Acceso total al sistema y administración de usuarios', 100),
('Operador', 'Gestiona activos, auditorías y genera alertas', 50),
('Consulta', 'Solo visualiza datos y reportes', 10);

INSERT INTO users (username, password_hash, full_name, role_id) VALUES
('admin', 'admin123', 'Administrador Principal', 1),
('operador', 'operador123', 'Operador de Inventario', 2),
('consulta', 'consulta123', 'Usuario de Consulta', 3);

-- Categorías iniciales
CREATE TABLE categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255) NULL
);

-- Modificación de assets: nuevos campos y referencia a categorías
DROP TABLE IF EXISTS assets;
CREATE TABLE assets (
    asset_id INT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    category_id INT NOT NULL,
    location VARCHAR(100) NOT NULL,
    status ENUM('IN_SERVICE','UNDER_MAINTENANCE','RETIRED','LOST') NOT NULL DEFAULT 'IN_SERVICE',
    lifecycle_stage VARCHAR(100) NOT NULL DEFAULT 'OPERACIÓN',
    purchase_date DATE NULL,
    decommission_date DATE NULL,
    lifecycle_days INT GENERATED ALWAYS AS (DATEDIFF(COALESCE(decommission_date, CURRENT_DATE), purchase_date)) STORED,
    maintenance ENUM('YES','NO') NOT NULL DEFAULT 'NO',
    quantity INT NOT NULL DEFAULT 1,
    warranty_expiry DATE NULL,
    cost DECIMAL(12,2) NULL,
    created_by INT NOT NULL,
    responsible_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by INT NULL,
    updated_at TIMESTAMP NULL,
    FOREIGN KEY (category_id) REFERENCES categories(category_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    FOREIGN KEY (updated_by) REFERENCES users(user_id),
    FOREIGN KEY (responsible_id) REFERENCES users(user_id)
);

-- Tabla asset_history actualizada
DROP TABLE IF EXISTS asset_history;
CREATE TABLE asset_history (
    history_id INT AUTO_INCREMENT PRIMARY KEY,
    asset_id INT NOT NULL,
    previous_status VARCHAR(50) NULL,
    new_status VARCHAR(50) NOT NULL,
    previous_quantity INT NULL,
    new_quantity INT NULL,
    changed_by INT NOT NULL,
    change_reason VARCHAR(255) NULL,
    change_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (asset_id) REFERENCES assets(asset_id),
    FOREIGN KEY (changed_by) REFERENCES users(user_id)
);

-- Categorías iniciales
INSERT INTO categories (name, description) VALUES
('Hardware','Equipos de cómputo y periféricos'),
('Infraestructura','Servidores y equipos de sala'),
('Red','Equipos de red y comunicaciones'),
('Consumibles','Material de consumo');

-- Activos de ejemplo (responsible_id se establece igual que created_by en inserción demo)
INSERT INTO assets (sku, name, category_id, location, status, lifecycle_stage, purchase_date, decommission_date, maintenance, quantity, warranty_expiry, cost, created_by, responsible_id, created_at)
VALUES
('ACT-001','Laptop Dell Latitude',1,'Almacén Central','IN_SERVICE','Operación','2024-05-10',NULL,'NO',5,'2026-05-10',1200.00,1,1,NOW()),
('ACT-002','Servidor HP ProLiant',2,'Sala de Servidores','UNDER_MAINTENANCE','Mantenimiento','2023-11-22',NULL,'YES',1,'2025-11-22',8000.00,1,1,NOW()),
('ACT-003','Router Cisco',3,'Cuarto de Redes','IN_SERVICE','Operación','2024-01-15',NULL,'NO',2,'2026-01-15',1500.00,1,1,NOW());

INSERT INTO audits (user_id, action, target_table, target_id, success, details, ip_address)
VALUES
(1,'LOGIN','users','1',TRUE,'Inicio de sesión exitoso','192.168.1.10'),
(2,'ACTUALIZAR_ESTADO','assets','2',TRUE,'Cambio de estado a UNDER_MAINTENANCE','192.168.1.11');

-- Triggers: mantener historial y generar alertas automáticas
DELIMITER $$
CREATE TRIGGER tr_asset_update_history
AFTER UPDATE ON assets
FOR EACH ROW
BEGIN
    INSERT INTO asset_history(asset_id, previous_status, new_status, previous_quantity, new_quantity, changed_by, change_reason, change_date)
    VALUES(NEW.asset_id, OLD.status, NEW.status, OLD.quantity, NEW.quantity, NEW.updated_by, CONCAT('Cambio automático en ', NOW()), NOW());
END$$

CREATE TRIGGER tr_asset_insert_alerts
AFTER INSERT ON assets
FOR EACH ROW
BEGIN
    -- alerta por mantenimiento marcado
    IF NEW.maintenance = 'YES' THEN
        INSERT INTO alerts(asset_id, alert_type, alert_level, message, created_at)
        VALUES(NEW.asset_id, 'MANTENIMIENTO_REQUERIDO', 'CRITICAL', CONCAT('Mantenimiento requerido para ', NEW.name), NOW());
    END IF;

    -- alerta por baja cantidad
    IF NEW.quantity <= 1 THEN
        INSERT INTO alerts(asset_id, alert_type, alert_level, message, created_at)
        VALUES(NEW.asset_id, 'BAJA_CANTIDAD', 'WARNING', CONCAT('Cantidad baja (', NEW.quantity, ') para ', NEW.name), NOW());
    END IF;
END$$

CREATE TRIGGER tr_asset_update_alerts
AFTER UPDATE ON assets
FOR EACH ROW
BEGIN
    -- si cambia mantenimiento a YES generar alerta
    IF OLD.maintenance = 'NO' AND NEW.maintenance = 'YES' THEN
        INSERT INTO alerts(asset_id, alert_type, alert_level, message, created_at)
        VALUES(NEW.asset_id, 'MANTENIMIENTO_REQUERIDO', 'CRITICAL', CONCAT('Mantenimiento requerido para ', NEW.name), NOW());
    END IF;

    -- si cantidad baja generar alerta
    IF NEW.quantity <= 1 AND (OLD.quantity IS NULL OR OLD.quantity > 1) THEN
        INSERT INTO alerts(asset_id, alert_type, alert_level, message, created_at)
        VALUES(NEW.asset_id, 'BAJA_CANTIDAD', 'WARNING', CONCAT('Cantidad baja (', NEW.quantity, ') para ', NEW.name), NOW());
    END IF;

    -- alerta por garantía vencida
    IF NEW.warranty_expiry IS NOT NULL AND NEW.warranty_expiry < CURRENT_DATE() THEN
        INSERT INTO alerts(asset_id, alert_type, alert_level, message, created_at)
        VALUES(NEW.asset_id, 'GARANTIA_VENCIDA', 'WARNING', CONCAT('Garantía vencida para ', NEW.name), NOW());
    END IF;
END$$
DELIMITER ;

-- Vista resumen para dashboard con métricas y alertas pendientes
CREATE OR REPLACE VIEW vw_asset_summary AS
SELECT a.asset_id,
       a.sku,
       a.name,
       c.name AS category,
       a.location,
       a.status,
       a.lifecycle_stage,
       a.purchase_date,
       a.decommission_date,
       a.lifecycle_days,
       a.maintenance,
       a.quantity,
       a.cost,
       a.created_by,
       a.responsible_id,
       (SELECT COUNT(*) FROM alerts al WHERE al.asset_id = a.asset_id AND al.resolved = FALSE) AS unresolved_alerts
FROM assets a
JOIN categories c ON a.category_id = c.category_id;

-- Generar alerta para activos con garantía vencida al crear registros de ejemplo
INSERT INTO alerts (asset_id, alert_type, alert_level, message)
SELECT asset_id, 'GARANTIA_VENCIDA', 'WARNING', CONCAT('Garantía vencida para ', name)
FROM assets
WHERE warranty_expiry IS NOT NULL AND warranty_expiry < CURRENT_DATE();
