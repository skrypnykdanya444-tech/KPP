SET NAMES utf8mb4;

DROP DATABASE IF EXISTS transport_db;
CREATE DATABASE transport_db CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE transport_db;

DROP TABLE IF EXISTS response_task;
DROP TABLE IF EXISTS incident;
DROP TABLE IF EXISTS route;
DROP TABLE IF EXISTS vehicle;

CREATE TABLE vehicle (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           code VARCHAR(50) NOT NULL,
                           name VARCHAR(100) NOT NULL,
                           category VARCHAR(50) NOT NULL
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE route (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         vehicle_id BIGINT NOT NULL,
                         route_number VARCHAR(20) NOT NULL,
                         stop_name VARCHAR(100) NOT NULL,
                         passenger_flow DOUBLE NOT NULL DEFAULT 0,
                         f2 DOUBLE NOT NULL DEFAULT 0,
                         CONSTRAINT fk_route_vehicle FOREIGN KEY (vehicle_id)
                             REFERENCES vehicle (id) ON DELETE CASCADE ON UPDATE CASCADE
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE incident (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          route_id BIGINT NOT NULL,
                          incident_type VARCHAR(50) NOT NULL,
                          state VARCHAR(50) NOT NULL DEFAULT 'NEW',
                          disruption_duration DOUBLE NOT NULL DEFAULT 0,
                          f1 DOUBLE NOT NULL DEFAULT 0,
                          f2 DOUBLE NOT NULL DEFAULT 0,
                          f3 DOUBLE NOT NULL DEFAULT 0,
                          severity_index DOUBLE NOT NULL DEFAULT 0,
                          CONSTRAINT fk_incident_route FOREIGN KEY (route_id)
                              REFERENCES route (id) ON DELETE CASCADE ON UPDATE CASCADE
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE TABLE response_task (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              incident_id BIGINT NOT NULL,
                              description TEXT NOT NULL,
                              planned_date DATETIME NOT NULL,
                              CONSTRAINT fk_task_incident FOREIGN KEY (incident_id)
                                  REFERENCES incident (id) ON DELETE CASCADE ON UPDATE CASCADE
) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

INSERT INTO vehicle (id, code, name, category) VALUES
    (1, 'TR-01', 'ЕТ-120', 'Тролейбус'),
    (2, 'BS-45', 'Богдан А092', 'Автобус'),
    (3, 'TM-12', 'Tatra T3', 'Трамвай'),
    (4, 'TR-52', 'Дніпро Т203', 'Тролейбус'),
    (5, 'BS-61', 'Ataman A092H6', 'Автобус'),
    (6, 'TM-66', 'Tatra KT4SU', 'Трамвай'),
    (7, 'TR-16', 'Електрон T191', 'Тролейбус'),
    (8, 'BS-79', 'Solaris Urbino 12', 'Автобус'),
    (9, 'TM-89', 'K3R-N', 'Трамвай'),
    (10, 'TR-47', 'Богдан Т70117', 'Тролейбус');

INSERT INTO route (id, vehicle_id, route_number, stop_name, passenger_flow, f2) VALUES
    (1, 1, '5', 'Центральний ринок', 1800, 90),
    (2, 2, '22', 'Університет', 1200, 65),
    (3, 3, '1', 'Вокзал-Головний', 2400, 95),
    (4, 4, '27', 'Школа №5', 900, 50),
    (5, 5, '104', 'Парк Шевченка', 1500, 70),
    (6, 6, '10', 'вул. Соборна', 700, 45),
    (7, 7, '22', 'Автовокзал', 1300, 60),
    (8, 8, '3', 'вул. Європейська', 1100, 55),
    (9, 9, '1', 'Спортивний комплекс', 800, 50),
    (10, 10, '104', 'вул. Заводська', 1600, 75);

INSERT INTO incident (id, route_id, incident_type, state, disruption_duration,
                      f1, f2, f3, severity_index) VALUES
    (1, 1, 'ДТП', 'NEW', 45, 72, 90, 18.75, 64.99),
    (2, 3, 'Технічна несправність', 'CONFIRMED', 120, 96, 95, 50, 84.15),
    (3, 2, 'Затримка руху', 'RESPONSE', 180, 48, 65, 75, 60.7),
    (4, 5, 'ДТП', 'NEW', 30, 60, 70, 12.5, 51.63),
    (5, 8, 'Затримка руху', 'RESPONSE', 240, 44, 55, 100, 61.85),
    (6, 3, 'ДТП', 'CONFIRMED', 90, 96, 95, 37.5, 81.03),
    (7, 4, 'Технічна несправність', 'NEW', 150, 36, 50, 62.5, 47.53),
    (8, 10, 'Затримка руху', 'CLOSED', 60, 64, 75, 25, 58.1),
    (9, 6, 'ДТП', 'RESPONSE', 200, 28, 45, 83.33, 47.78),
    (10, 7, 'Технічна несправність', 'CLOSED', 75, 52, 60, 31.25, 49.61);

INSERT INTO response_task (id, incident_id, description, planned_date) VALUES
    (1, 1, 'Направити технічну допомогу на контактну мережу', '2026-06-01 10:30:00'),
    (2, 2, 'Евакуювати трамвай у депо для ремонту', '2026-06-01 12:00:00'),
    (3, 3, 'Перенаправити суміжний транспорт на маршрут', '2026-06-02 09:15:00'),
    (4, 4, 'Викликати екіпаж патрульної поліції', '2026-06-02 14:45:00'),
    (5, 5, 'Замінити засіб на резервний з автопарку', '2026-06-03 08:20:00'),
    (6, 6, 'Провести оперативний огляд гальмівної системи', '2026-06-03 16:50:00'),
    (7, 7, 'Огороджувати місце ДТП та викликати швидку', '2026-06-04 11:05:00'),
    (8, 8, 'Направити технічну допомогу на контактну мережу', '2026-06-04 15:30:00'),
    (9, 9, 'Евакуювати у депо для ремонту', '2026-06-05 07:40:00'),
    (10, 10, 'Викликати екіпаж патрульної поліції', '2026-06-05 13:20:00');
