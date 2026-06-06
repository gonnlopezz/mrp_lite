-- ── 1. REGISTRO DEL NUEVO CLIENTE ────────────────────────────────────────────────────────
INSERT INTO customer (id, cuit, company_name, observations) VALUES (500, 20449470843, 'Gonzalo Lopez', 'Cliente de prueba para validación y estrés del motor de planificación backward.');

-- ── 2. REGISTRO DE ÓRDENES DE FABRICACIÓN (PENDIENTES DE PLANIFICAR) ─────────────────────

-- Caso 1: Planificable por Taller Alfa (Lote chico, margen cómodo de 1 día)
INSERT INTO manufacturing_order (id, order_date, delivery_date, quantity, order_state, customer_id, product_id)
VALUES (501, '2026-06-16', '2026-06-17', 1, 'PENDIENTE', 500, 1);

-- -- Caso 2: Pasa a Taller Gama por volumen (Alfa rebalsa capacidad en 24hs, Gama absorbe las 20 unidades)
INSERT INTO manufacturing_order (id, order_date, delivery_date, quantity, order_state, customer_id, product_id)
VALUES (502, '2026-06-16', '2026-06-17', 20, 'PENDIENTE', 500, 1);

-- -- Caso 3: No planificable por falta de tiempo/capacidad (200 unidades es un excedente absoluto para solo 48hs)
INSERT INTO manufacturing_order (id, order_date, delivery_date, quantity, order_state, customer_id, product_id)
VALUES (503, '2026-06-16', '2026-06-18', 200, 'PENDIENTE', 500, 1);

-- -- Caso 4: No planificable por Error Estructural (Ningún taller cuenta con las máquinas para el Soporte en U)
INSERT INTO manufacturing_order (id, order_date, delivery_date, quantity, order_state, customer_id, product_id)
VALUES (504, '2026-06-16', '2026-06-16', 1, 'PENDIENTE', 500, 5);

