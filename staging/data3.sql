TRUNCATE TABLE customer RESTART IDENTITY CASCADE;
TRUNCATE TABLE planning RESTART IDENTITY CASCADE;
TRUNCATE TABLE planning_process RESTART IDENTITY CASCADE;
TRUNCATE TABLE manufacturing_order RESTART IDENTITY CASCADE;

DO $$
DECLARE
    i INT;
    v_customer_id INT;
    v_product_id INT;
    -- Setamos la fecha de origen estricta que me pediste
    v_order_date DATE := '2026-06-28'; 
    v_delivery_date DATE;
    v_quantity INT;
BEGIN

    -- 2. GENERACIÓN AUTOMATIZADA DE 100 CLIENTES (IDs del 600 al 699)
    FOR i IN 0..99 LOOP
        INSERT INTO customer (id, cuit, company_name, observations)
        VALUES (
            600 + i, 
            20450000000 + i, 
            CASE 
                WHEN i % 4 = 0 THEN 'Metalúrgica Madryn - Sucursal ' || i
                WHEN i % 4 = 1 THEN 'Servicios Industriales Trelew N° ' || i
                WHEN i % 4 = 2 THEN 'Logística Patagónica S.A. ' || i
                ELSE 'Fábrica Golfo Nuevo Confidencial ' || i
            END,
            'Cliente automatizado para test de estrés de alta concurrencia.'
        );
    END LOOP;

    -- 3. GENERACIÓN AUTOMATIZADA DE 1000 PEDIDOS (IDs del 2000 al 2999)
    -- Distribuidos uniformemente entre tus productos reales para estresar ALFA, BETA y GAMA
    FOR i IN 0..999 LOOP
        -- Rota limpiamente entre tus 100 clientes creados arriba
        v_customer_id := 600 + (i % 100); 
        
        -- Rota entre tus productos operativos (ID 1 al 4) y mete un 5% de errores estructurales (ID 5)
        v_product_id := CASE WHEN i % 20 = 0 THEN 5 ELSE (i % 4) + 1 END;
        
        -- Escalona las fechas de entrega entre 1 y 15 días posteriores al 28 de Junio de 2026
        v_delivery_date := v_order_date + (i % 15 + 1); 
        
        -- Cantidades controladas variables (entre 2 y 35 unidades) para saturar talleres progresivamente
        v_quantity := (i * 13) % 34 + 2;

        INSERT INTO manufacturing_order (id, order_date, delivery_date, quantity, order_state, customer_id, product_id)
        VALUES (
            2000 + i, 
            v_order_date, 
            v_delivery_date, 
            v_quantity, 
            'PENDIENTE', 
            v_customer_id, 
            v_product_id
        );
    END LOOP;

END $$;