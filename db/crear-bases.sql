-- Ejecutar como administrador de MySQL. No borra esquemas existentes.
CREATE DATABASE IF NOT EXISTS gymadmin
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Exclusiva para pruebas automatizadas: sus movimientos se eliminan en cada caso.
CREATE DATABASE IF NOT EXISTS gymadmin_test
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
