-- =========================================================================
-- 1. LIMPIEZA TOTAL EN CASCADA (REINICIO DE ENTORNO SEGURO)
-- =========================================================================
TRUNCATE TABLE cliente RESTART IDENTITY CASCADE;
TRUNCATE TABLE tipo_equipo RESTART IDENTITY CASCADE;
TRUNCATE TABLE equipo RESTART IDENTITY CASCADE;
TRUNCATE TABLE taller RESTART IDENTITY CASCADE;
TRUNCATE TABLE taller_equipos RESTART IDENTITY CASCADE;
TRUNCATE TABLE producto RESTART IDENTITY CASCADE;
TRUNCATE TABLE tarea RESTART IDENTITY CASCADE;
TRUNCATE TABLE producto_tareas RESTART IDENTITY CASCADE;
TRUNCATE TABLE planificacion RESTART IDENTITY CASCADE;
TRUNCATE TABLE proceso_planificacion RESTART IDENTITY CASCADE;
TRUNCATE TABLE pedido RESTART IDENTITY CASCADE;

-- =========================================================================
-- 2. REGISTRO DE CLIENTES (DATOS DEL CONTROL DE PRODUCCIÓN)
-- =========================================================================
INSERT INTO cliente (id, cuit, razon_social, observaciones) VALUES 
(500, 20449470843, 'Gonzalo Lopez', 'Cliente premium - Pruebas de ingeniería.'),
(501, 20450000001, 'Demian', 'Requiere entregas preferentemente por la tarde.'),
(502, 20450000002, 'Rodrigo Rene Cura', 'Inspección de calidad estricta en la recepción.'),
(503, 30123456789, 'Aluar Aluminio Argentino SA', 'Planta Puerto Madryn - Enviar remito duplicado.'),
(504, 30987654321, 'INFA SA', 'Proyectos industriales - División Estructuras.');

-- =========================================================================
-- 3. REGISTRO DE LOS TIPOS DE EQUIPO (MAESTRO EXPLÍCITO)
-- =========================================================================
INSERT INTO tipo_equipo (id, nombre) VALUES
(1, 'amoladora'),
(2, 'soldadora'),
(3, 'taladro'),
(4, 'pistola de pintura'),
(5, 'sierra');

-- =========================================================================
-- 4. REGISTRO DE LOS 5 TALLERES BASE (DATOS DEL FEATURE)
-- =========================================================================
INSERT INTO taller (codigo, nombre) VALUES
('ALFA', 'Taller Alfa para lo básico'),
('BETA', 'Taller Beta es muy rápido'),
('GAMA', 'Taller Gama puede con todo'),
('DELTA', 'Taller Delta ensamble intermedio'),
('ALFA-2', 'Taller Alfa 2 alta capacidad serie');

-- =========================================================================
-- 5. REGISTRO DE EQUIPOS (CONFORME A TU ESTRUCTURA EN DB)
-- =========================================================================
INSERT INTO equipo (id, capacidad, codigo, tipo_id) VALUES
-- Equipos de ALFA
(1, 1, 'A01_amoladora', 1),
(2, 1, 'A02_soldadora', 2),
-- Equipos de BETA
(3, 4, 'B01_amoladora', 1),
(4, 4, 'B02_taladro', 3),
(5, 4, 'B03_pistola', 4),
-- Equipos de GAMA
(6, 2, 'G01_amoladora', 1),
(7, 2, 'G02_soldadora', 2),
(8, 2, 'G03_taladro', 3),
(9, 1, 'G04_pistola', 4),
-- Equipos de DELTA
(10, 2, 'D01_amoladora', 1),
(11, 2, 'D02_soldadora', 2),
(12, 1, 'D03_taladro', 3),
-- Equipos de ALFA-2
(13, 3, 'A2_AMOLAD_01', 1),
(14, 3, 'A2_AMOLAD_02', 1),
(15, 4, 'A2_SOLDAD_01', 2),
(16, 2, 'A2_PISTOL_01', 4);

-- =========================================================================
-- 6. ASIGNACIÓN EN TABLA INTERMEDIA (MAPEO TALLER <-> EQUIPO)
-- CORREGIDO: Usando IDs numéricos (BIGINT) correspondientes al orden de inserción
-- =========================================================================
INSERT INTO taller_equipos (taller_id, equipos_id) VALUES
-- Equipos asignados a ALFA (ID 1)
(1, 1), (1, 2),
-- Equipos asignados a BETA (ID 2)
(2, 3), (2, 4), (2, 5),
-- Equipos asignados a GAMA (ID 3)
(3, 6), (3, 7), (3, 8), (3, 9),
-- Equipos asignados a DELTA (ID 4)
(4, 10), (4, 11), (4, 12),
-- Equipos asignados a ALFA-2 (ID 5)
(5, 13), (5, 14), (5, 15), (5, 16);

-- =========================================================================
-- 7. REGISTRO DEL CATÁLOGO DE PRODUCTOS (ID 1 AL 10 DE TUS FEATURES)
-- =========================================================================
INSERT INTO producto (id, nombre) VALUES
(1, 'Soporte metálico mediano'),
(2, 'Andamio básico 2x2x4'),
(3, 'Canasto de basura chico 1,5mts'),
(4, 'Pieza chica en U'),
(5, 'Soporte en U para estantería'),
(6, 'Estanteria Pesada Industrial'),
(7, 'Gabinete Electrico Estanco'),
(8, 'Contenedor de Residuos 1000L'),
(9, 'Carro de Carga de 4 Ruedas'),
(10, 'Brazo de Izaje Articulado');

-- =========================================================================
-- 8. HOJAS DE RUTA: TAREAS INDEPENDIENTES (SIN LA COLUMNA PRODUCTO_ID)
-- =========================================================================
INSERT INTO tarea (id, nombre, orden, tiempo, tipo_id) VALUES
-- Producto 1: Soporte metálico mediano
(1, 'cortar planchas', 1, 60, 1),
(2, 'cortar perfiles', 2, 20, 1),
(3, 'armado', 3, 90, 2),

-- Producto 2: Andamio básico 2x2x4
(4, 'cortar caños', 1, 20, 1),
(5, 'realizar perforaciones', 2, 40, 3),
(6, 'aplicar capa protectora', 3, 80, 4),

-- Producto 3: Canasto de basura chico 1,5mts
(7, 'cortar perfiles', 1, 60, 1),
(8, 'cortar malla', 2, 20, 1),
(9, 'soldar canasto', 3, 30, 2),
(10, 'unir pie', 4, 20, 2),
(11, 'pintar antioxidante', 5, 60, 4),

-- Producto 4: Pieza chica en U
(12, 'unir ramas', 1, 30, 2),
(13, 'realizar perforaciones', 2, 160, 3),

-- Producto 5: Soporte en U para estantería
(14, 'unir ramas', 1, 30, 2),
(15, 'realizar perforaciones', 2, 160, 3),
(16, 'cortar maderas', 3, 50, 5),

-- Producto 6: Estanteria Pesada Industrial
(17, 'Cortar perfiles estructurales', 1, 45, 1),
(18, 'Perforar anclajes de base', 2, 30, 3),

-- Producto 7: Gabinete Electrico Estanco
(19, 'Dimensionar chapa plegada', 1, 50, 1),
(20, 'Soldar escuadras y bisagras', 2, 40, 2),
(21, 'Acabado con pintura poliuretan', 3, 60, 4),

-- Producto 8: Contenedor de Residuos 1000L
(22, 'Cortar chapas de piso 1/4', 1, 90, 1),
(23, 'Soldadura perimetral reforzad', 2, 120, 2),

-- Producto 9: Carro de Carga de 4 Ruedas
(24, 'Estructurar chasis inferior', 1, 35, 2),
(25, 'Perforar ejes de rodamiento', 2, 25, 3),

-- Producto 10: Brazo de Izaje Articulado
(26, 'Cortar vigas doble T', 1, 60, 1),
(27, 'Mecanizar buje central', 2, 50, 3),
(28, 'Union de costillas de refuerz', 3, 80, 2);

-- =========================================================================
-- 8.5 RELACIÓN EN TABLA INTERMEDIA (producto_tareas)
-- =========================================================================
INSERT INTO producto_tareas (producto_id, tareas_id) VALUES
-- Producto 1
(1, 1), (1, 2), (1, 3),
-- Producto 2
(2, 4), (2, 5), (2, 6),
-- Producto 3
(3, 7), (3, 8), (3, 9), (3, 10), (3, 11),
-- Producto 4
(4, 12), (4, 13),
-- Producto 5
(5, 14), (5, 15), (5, 16),
-- Producto 6
(6, 17), (6, 18),
-- Producto 7
(7, 19), (7, 20), (7, 21),
-- Producto 8
(8, 22), (8, 23),
-- Producto 9
(9, 24), (9, 25),
-- Producto 10
(10, 26), (10, 27), (10, 28);

-- =========================================================================
-- 9. BLOQUE ANÓNIMO EXCLUSIVO PARA GENERACIÓN DE BUCLES (50 PEDIDOS)
-- =========================================================================
DO $BODY$
DECLARE
    i INT;
    v_global_counter INT := 1;
    v_customer_id INT;
    v_product_id INT;
    v_order_date DATE;
    v_delivery_date DATE;
    v_quantity INT;
BEGIN

    -- CASO LOTES 1: 15 Pedidos Base (Productos 1 y 2) -> Foco en ALFA y BETA
    FOR i IN 1..15 LOOP
        v_customer_id := 500 + (i % 5);
        v_product_id  := (i % 2) + 1;
        v_quantity    := (i * 3) % 6 + 3; 

        IF i <= 6 THEN v_order_date := '2026-06-16';
        ELSIF i <= 11 THEN v_order_date := '2026-06-28';
        ELSE v_order_date := '2026-06-30';
        END IF;

        IF v_global_counter <= 20 THEN v_delivery_date := '2026-06-20';
        ELSIF v_global_counter <= 40 THEN v_delivery_date := '2026-06-22';
        ELSE v_delivery_date := '2026-06-24';
        END IF;

        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado_pedido, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, v_product_id);

        v_global_counter := v_global_counter + 1;
    END LOOP;

    -- CASO LOTES 2: 15 Pedidos Complejos Exclusivos (Productos 3 y 4) -> Foco en GAMA
    FOR i IN 1..15 LOOP
        v_customer_id := 500 + ((i + 1) % 5);
        v_product_id  := (i % 2) + 3;
        v_quantity    := (i * 5) % 8 + 4;

        IF i <= 5 THEN v_order_date := '2026-06-20';
        ELSIF i <= 10 THEN v_order_date := '2026-06-25';
        ELSE v_order_date := '2026-07-02';
        END IF;

        IF v_global_counter <= 20 THEN v_delivery_date := '2026-06-20';
        ELSIF v_global_counter <= 40 THEN v_delivery_date := '2026-06-22';
        ELSE v_delivery_date := '2026-06-24';
        END IF;

        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado_pedido, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, v_product_id);

        v_global_counter := v_global_counter + 1;
    END LOOP;

    -- CASO LOTES 3: 18 Pedidos Catálogo (Balanceo estructural con Gabinetes ID 7)
    FOR i IN 1..18 LOOP
        v_customer_id := 500 + ((i + 2) % 5);
        
        IF i <= 6 THEN v_product_id := 7;
        ELSE v_product_id := 6 + (i % 5);
        END IF;
        
        v_quantity    := ((i * 4) % 10) + 2;

        IF i <= 6 THEN v_order_date := '2026-06-25';
        ELSIF i <= 12 THEN v_order_date := '2026-07-02';
        ELSE v_order_date := '2026-07-08';
        END IF;

        IF v_global_counter <= 20 THEN v_delivery_date := '2026-06-20';
        ELSIF v_global_counter <= 40 THEN v_delivery_date := '2026-06-22';
        ELSE v_delivery_date := '2026-06-24';
        END IF;

        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado_pedido, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, v_product_id);

        v_global_counter := v_global_counter + 1;
    END LOOP;

    -- CASO LOTES 4: 2 RECHAZOS ESTRUCTURALES CONTROLADOS (Producto ID 5)
    FOR i IN 0..1 LOOP
        v_order_date    := '2026-06-16';
        v_delivery_date := '2026-06-24';

        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado_pedido, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, 5, 'PENDIENTE', 500 + i, 5);

        v_global_counter := v_global_counter + 1;
    END LOOP;

END $BODY$;