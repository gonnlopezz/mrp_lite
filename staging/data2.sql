TRUNCATE TABLE cliente RESTART IDENTITY CASCADE;
TRUNCATE TABLE planificacion RESTART IDENTITY CASCADE;
TRUNCATE TABLE proceso_planificacion RESTART IDENTITY CASCADE;
TRUNCATE TABLE pedido RESTART IDENTITY CASCADE;
DO $$
DECLARE
    i INT;
    v_customer_id INT;
    v_product_id INT;
    v_order_date DATE := '2026-06-16';
    v_delivery_date DATE;
    v_quantity INT;
BEGIN
    -- 2. DECLARACIÓN DE LOS 5 CLIENTES ESPECÍFICOS
    INSERT INTO cliente (id, cuit, razon_social, observaciones) VALUES 
    (500, 20449470843, 'Gonzalo Lopez', 'Cliente premium - Pruebas de ingeniería.'),
    (501, 20450000001, 'Demian', 'Requiere entregas preferentemente por la tarde.'),
    (502, 20450000002, 'Rodrigo Rene Cura', 'Inspección de calidad estricta en la recepción.'),
    (503, 30123456789, 'Aluar Aluminio Argentino SA', 'Planta Puerto Madryn - Enviar remito duplicado.'),
    (504, 30987654321, 'INFA SA', 'Proyectos industriales -兵 División Estructuras.');

    -- CASO 1: 40 Pedidos de Soporte Mediano (ID 1) -> ALFA con Cascada a GAMA
    -- Distribución cíclica pura entre los 5 clientes
    FOR i IN 1..40 LOOP
        v_customer_id   := 500 + (i % 5); -- Rota limpio entre 500 y 504
        v_delivery_date := v_order_date + (i % 4 + 1); -- Fechas entre 17/06 y 20/06
        v_quantity      := (i * 3) % 8 + 2; 
        
        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, 1);
    END LOOP;

    -- CASO 2: 40 Pedidos de Andamio Básico (ID 2) -> BETA con Cascada a GAMA
    -- Distribución cíclica desfasada para que no tengan los mismos ID correlativos
    FOR i IN 1..40 LOOP
        v_customer_id   := 500 + ((i + 1) % 5); 
        v_delivery_date := v_order_date + (i % 5 + 2); -- Fechas entre 18/06 y 22/06
        v_quantity      := (i * 7) % 12 + 4;
        
        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado, cliente_id, producto_id)
        VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, 2);
    END LOOP;

    -- CASO 3: 25 Pedidos Complejos de GAMA (Canastos ID 3 y Piezas ID 4)
    FOR i IN 1..25 LOOP
        v_customer_id := 500 + ((i + 2) % 5);
        
        IF i % 2 = 0 THEN
            v_delivery_date := v_order_date + (i % 3 + 3); -- Canasto
            v_quantity := 6;
            INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado, cliente_id, producto_id)
            VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, 3);
        ELSE
            v_delivery_date := v_order_date + (i % 2 + 4); -- Pieza en U
            v_quantity := 10;
            INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado, cliente_id, producto_id)
            VALUES (v_order_date, v_delivery_date, v_quantity, 'PENDIENTE', v_customer_id, 4);
        END IF;
    END LOOP;

    -- CASO 4: 5 RECHAZOS ESTRUCTURALES (Soporte en U - ID 5)
    -- Le asignamos exactamente UN RECHAZO a cada cliente.
    FOR i IN 0..4 LOOP
        INSERT INTO pedido (fecha_pedido, fecha_entrega, cantidad, estado, cliente_id, producto_id)
        VALUES (v_order_date, v_order_date + 3, 5, 'PENDIENTE', 500 + i, 5);
    END LOOP;

END $$;