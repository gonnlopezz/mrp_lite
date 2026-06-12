TRUNCATE TABLE cliente RESTART IDENTITY CASCADE;
TRUNCATE TABLE planificacion RESTART IDENTITY CASCADE;
TRUNCATE TABLE proceso_planificacion RESTART IDENTITY CASCADE;
TRUNCATE TABLE pedido RESTART IDENTITY CASCADE;

DO $$
DECLARE
    i INT;
    v_global_counter INT := 1;
    v_customer_id INT;
    v_product_id INT;
    v_order_date DATE;
    v_delivery_date DATE;
    v_quantity INT;
BEGIN
    -- 1. REGISTRO DE LOS 5 CLIENTES ESPECÍFICOS
    INSERT INTO cliente (id, cuit, razon_social, observaciones) VALUES 
    (500, 20449470843, 'Gonzalo Lopez', 'Cliente premium - Pruebas de ingeniería.'),
    (501, 20450000001, 'Demian', 'Requiere entregas preferentemente por la tarde.'),
    (502, 20450000002, 'Rodrigo Rene Cura', 'Inspección de calidad estricta en la recepción.'),
    (503, 30123456789, 'Aluar Aluminio Argentino SA', 'Planta Puerto Madryn - Enviar remito duplicado.'),
    (504, 30987654321, 'INFA SA', 'Proyectos industriales - División Estructuras.');

    -- =========================================================================
    -- CASO 1: 15 Pedidos Base (Productos 1 y 2) -> Foco en ALFA y BETA
    -- =========================================================================
    FOR i IN 1..15 LOOP
        v_customer_id := 500 + (i % 5);
        v_product_id  := (i % 2) + 1; -- Alterna entre ID 1 (Soporte) y ID 2 (Andamio)
        v_quantity    := (i * 3) % 6 + 3; 

        -- Distribución de días de carga (Días 1, 2 y 3)
        IF i <= 6 THEN v_order_date := '2026-06-16';
        ELSIF i <= 11 THEN v_order_date := '2026-06-28';
        ELSE v_order_date := '2026-06-30';
        END IF;

        -- COMPRESIÓN AGRESIVA DE ENTREGAS (Rango de 4 días en junio)
        IF v_global_counter <= 20 THEN v_delivery_date := '2026-06-20';
        ELSIF v_global_counter <= 40 THEN v_delivery_date := '2026-06-22';
        ELSE v_delivery_date := '2026-06-24';
        END IF;

        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado_pedido, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, v_product_id);

        v_global_counter := v_global_counter + 1;
    END LOOP;

    -- =========================================================================
    -- CASO 2: 15 Pedidos Complejos Exclusivos (Productos 3 y 4) -> Foco en GAMA
    -- =========================================================================
    FOR i IN 1..15 LOOP
        v_customer_id := 500 + ((i + 1) % 5);
        v_product_id  := (i % 2) + 3; -- Alterna entre ID 3 (Canasto) y ID 4 (Pieza en U)
        v_quantity    := (i * 5) % 8 + 4;

        -- Distribución de días de carga (Días 2, 3 y 4)
        IF i <= 5 THEN v_order_date := '2026-06-20';
        ELSIF i <= 10 THEN v_order_date := '2026-06-25';
        ELSE v_order_date := '2026-07-02';
        END IF;

        -- COMPRESIÓN AGRESIVA DE ENTREGAS
        IF v_global_counter <= 20 THEN v_delivery_date := '2026-06-20';
        ELSIF v_global_counter <= 40 THEN v_delivery_date := '2026-06-22';
        ELSE v_delivery_date := '2026-06-24';
        END IF;

        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado_pedido, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, v_product_id);

        v_global_counter := v_global_counter + 1;
    END LOOP;

    -- =========================================================================
    -- CASO 3: 18 Pedidos Nuevos Catálogo (Estratégicamente balanceados con ID 7)
    -- =========================================================================
    FOR i IN 1..18 LOOP
        v_customer_id := 500 + ((i + 2) % 5);
        
        -- Estrategia para inyectar Gabinetes (ID 7) al principio y rotar el resto
        IF i <= 6 THEN 
            v_product_id := 7; -- Forzamos Gabinetes Eléctricos para rellenar amoladora y pistola por la mañana
        ELSE 
            v_product_id := 6 + (i % 5); -- Rota entre 6, 7, 8, 9 y 10 para DELTA y ALFA-2
        END IF;
        
        v_quantity    := ((i * 4) % 10) + 2;

        -- Distribución de días de carga
        IF i <= 6 THEN v_order_date := '2026-06-25';
        ELSIF i <= 12 THEN v_order_date := '2026-07-02';
        ELSE v_order_date := '2026-07-08';
        END IF;

        -- COMPRESIÓN AGRESIVA DE ENTREGAS
        IF v_global_counter <= 20 THEN v_delivery_date := '2026-06-20';
        ELSIF v_global_counter <= 40 THEN v_delivery_date := '2026-06-22';
        ELSE v_delivery_date := '2026-06-24';
        END IF;

        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado_pedido, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, v_product_id);

        v_global_counter := v_global_counter + 1;
    END LOOP;

    -- =========================================================================
    -- CASO 4: 2 RECHAZOS ESTRUCTURALES CONTROLADOS (Producto ID 5) -> Total 50 exactos
    -- =========================================================================
    FOR i IN 0..1 LOOP
        v_order_date    := '2026-06-16'; 
        v_delivery_date := '2026-06-24'; -- Cae dentro de la misma semana de stress de junio

        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado_pedido, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, 5, 'PENDIENTE', 500 + i, 5);

        v_global_counter := v_global_counter + 1;
    END LOOP;

END $$;