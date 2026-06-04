# language: es
Característica: Casos negativos de planificación de pedidos
Escenario: Intentar planificar un pedido que no existe
    Dado que no existe ningún pedido con id 99999
    Cuando se solicita planificar el pedido el día "01-02-2025"
    Entonces se espera el siguiente 404 con "Pedido no encontrado"

Escenario: Intentar planificar un pedido cuyo estado no admite replanificación
    Dado que existe el pedido para el cliente "27123456781" con fecha de entrega "03-02-2025"
    Cuando se solicita planificar el pedido el día "01-02-2025"
    Entonces se espera el siguiente 409 con "El pedido ya se encuentra en estado planificado"

Escenario: El producto del pedido requiere un tipo de equipo que ningún taller posee
    Dado que existe el pedido para el cliente "20654239875" con fecha de entrega "06-06-2025"
    Cuando se solicita planificar el pedido el día "01-06-2025"
    Entonces se espera el siguiente 200 con "El pedido no pudo planificarse en el plazo requerido"

Escenario: La fecha de entrega coincide con la fecha de ejecución dejando margen de tiempo cero
    Dado que existe el pedido para el cliente "27123456781" con fecha de entrega "03-06-2025"
    Cuando se solicita planificar el pedido el día "03-06-2025"
    Entonces se espera el siguiente 200 con "El pedido no pudo planificarse en el plazo requerido"

Escenario: La cantidad de unidades supera la capacidad de todos los talleres en el plazo requerido
    Dado que existe el pedido para el cliente "30000000033" con fecha de entrega "10-02-2025"
    Cuando se solicita planificar el pedido el día "09-02-2025"
    Entonces se espera el siguiente 200 con "El pedido no pudo planificarse en el plazo requerido"