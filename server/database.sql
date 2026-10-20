-- Створення та вибір бази даних
CREATE DATABASE IF NOT EXISTS transport_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE transport_db;

-- Видалення існуючих таблиць у правильному порядку (щоб уникнути помилок FK) перед їх створенням
DROP TABLE IF EXISTS ResponseTask;
DROP TABLE IF EXISTS Incident;
DROP TABLE IF EXISTS Route;
DROP TABLE IF EXISTS Vehicle;

-- 1. Таблиця транспортних засобів
CREATE TABLE Vehicle (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         code VARCHAR(50) NOT NULL,
                         name VARCHAR(100) NOT NULL,
                         category VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 2. Таблиця маршрутів
CREATE TABLE Route (
                       id INT AUTO_INCREMENT PRIMARY KEY,
                       vehicleId INT NOT NULL,
                       routeNumber VARCHAR(20) NOT NULL,
                       stopName VARCHAR(100) NOT NULL,
                       addressScore FLOAT,
                       severityScore FLOAT,
                       CONSTRAINT fk_route_vehicle FOREIGN KEY (vehicleId)
                           REFERENCES Vehicle(id)
                           ON DELETE CASCADE
                           ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 3. Таблиця інцидентів
CREATE TABLE Incident (
                          id INT AUTO_INCREMENT PRIMARY KEY,
                          routeId INT NOT NULL,
                          state VARCHAR(50) NOT NULL,
                          F1 FLOAT,
                          F2 FLOAT,
                          F3 FLOAT,
                          severityIndex FLOAT,
                          CONSTRAINT fk_incident_route FOREIGN KEY (routeId)
                              REFERENCES Route(id)
                              ON DELETE CASCADE
                              ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- 4. Таблиця завдань реагування
CREATE TABLE ResponseTask (
                              id INT AUTO_INCREMENT PRIMARY KEY,
                              incidentId INT NOT NULL,
                              description TEXT NOT NULL,
                              plannedDate DATETIME NOT NULL,
                              CONSTRAINT fk_task_incident FOREIGN KEY (incidentId)
                                  REFERENCES Incident(id)
                                  ON DELETE CASCADE
                                  ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- ==========================================
-- ПОЧАТКОВІ ДЕМОНСТРАЦІЙНІ ДАНІ (SEED DATA)
-- ==========================================

-- Додавання транспортних засобів
INSERT INTO Vehicle (id, code, name, category) VALUES
                                                   (1, 'TR-01', 'ЕТ-120', 'Тролейбус'),
                                                   (2, 'BS-45', 'Богдан А092', 'Автобус'),
                                                   (3, 'TM-12', 'Tatra T3', 'Трамвай');

-- Додавання маршрутів
INSERT INTO Route (id, vehicleId, routeNumber, stopName, addressScore, severityScore) VALUES
                                                                                          (1, 1, '5', 'Центральний ринок', 3.5, 2.1),
                                                                                          (2, 2, '22', 'Університет', 1.2, 0.5),
                                                                                          (3, 3, '1', 'Вокзал-Головний', 4.0, 3.8);

-- Додавання інцидентів
INSERT INTO Incident (id, routeId, state, F1, F2, F3, severityIndex) VALUES
                                                                         (1, 1, 'Затримка руху', 2.0, 3.0, 1.5, 2.16),
                                                                         (2, 3, 'Технічна несправність', 4.0, 4.0, 5.0, 4.33);

-- Додавання завдань реагування
INSERT INTO ResponseTask (id, incidentId, description, plannedDate) VALUES
                                                                        (1, 1, 'Направити технічну допомогу на контактну мережу', '2026-06-01 10:30:00'),
                                                                        (2, 2, 'Евакуювати трамвай у депо для ремонту', '2026-06-01 12:00:00');