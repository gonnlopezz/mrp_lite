-- ── 1. REGISTRO DEL NUEVO CLIENTE ────────────────────────────────────────────────────────
INSERT INTO customer (id, cuit, company_name, observations) VALUES (1001, 44947084, 'Gonzalo Lopez', 'Cliente de prueba para validación y estrés del motor de planificación backward.');

-- ── 2. REGISTRO DE ÓRDENES DE FABRICACIÓN (PENDIENTES DE PLANIFICAR) ─────────────────────

-- Caso 1: Planificable por Taller Alfa (Lote chico, margen cómodo de 1 día)
INSERT INTO manufacturing_order (id, order_date, delivery_date, quantity, state, customer_id, product_id)
VALUES (5001, '2026-06-16', '2026-06-17', 1, 'PENDIENTE', 1001, 2001);

-- Caso 2: Pasa a Taller Gama por volumen (Alfa rebalsa capacidad en 24hs, Gama absorbe las 20 unidades)
INSERT INTO manufacturing_order (id, order_date, delivery_date, quantity, state, customer_id, product_id)
VALUES (5002, '2026-06-16', '2026-06-17', 20, 'PENDIENTE', 1001, 2001);

-- Caso 3: No planificable por falta de tiempo/capacidad (200 unidades es un excedente absoluto para solo 48hs)
INSERT INTO manufacturing_order (id, order_date, delivery_date, quantity, state, customer_id, product_id)
VALUES (5003, '2026-06-16', '2026-06-18', 200, 'PENDIENTE', 1001, 2001);

-- Caso 4: No planificable por Error Estructural (Ningún taller cuenta con las máquinas para el Soporte en U)
INSERT INTO manufacturing_order (id, order_date, delivery_date, quantity, state, customer_id, product_id)
VALUES (5004, '2026-06-16', '2026-06-16', 1, 'PENDIENTE', 1001, 2002);