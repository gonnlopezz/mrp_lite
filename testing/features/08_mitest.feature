   # language: es
   Característica: Proceso de planificación con colisiones
   Esquema del escenario: Generar nuevos pedidos de fabricación para el proceso de planificación
    Dado el producto con nombre "<producto>"
    Y el cliente con <cuit>
    Cuando se solicita generar un pedido para ese cliente fecha de pedido "<fechaPedido>" para entregar en la fecha "<fechaEntrega>" la cantidad de <cantidad> del producto
    Entonces se espera el siguiente <status> con "<respuesta>"

Ejemplos:
      | cuit        | fechaPedido | fechaEntrega | cantidad | producto                       | status | respuesta                                    |
      | 10000000013 | 2025-06-10  | 2025-06-15   |        2 | Canasto de basura chico 1,5mts |    200 | Pedido de fabricación generado correctamente |
      | 20000000022 | 2025-06-10  | 2025-06-15   |        2 | Pieza chica en U               |    200 | Pedido de fabricación generado correctamente |
      | 40000000044 | 2025-06-10  | 2025-06-15   |        1 | Andamio básico 2x2x4           |    200 | Pedido de fabricación generado correctamente |
      | 50000000055 | 2025-06-10  | 2025-06-15   |        3 | Pieza chica en U               |    200 | Pedido de fabricación generado correctamente |

Escenario: Planificación de pedido de Canasto de basura con 2 unidades
    Dado que existe el pedido para el cliente "10000000013" con fecha de entrega "15-06-2025"
    Cuando se solicita planificar el pedido el día "12-06-2025"
    Entonces se espera el siguiente 200 con "Pedido planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea               |
      | 2025-06-14 20:55 | 2025-06-14 21:25 | G01_amoladora | cortar perfiles     |
      | 2025-06-14 21:25 | 2025-06-14 21:35 | G01_amoladora | cortar malla        |
      | 2025-06-14 21:35 | 2025-06-14 21:50 | G02_soldadora | soldar canasto      |
      | 2025-06-14 21:50 | 2025-06-14 22:00 | G02_soldadora | unir pie            |
      | 2025-06-14 21:55 | 2025-06-14 22:25 | G01_amoladora | cortar perfiles     |
      | 2025-06-14 22:00 | 2025-06-14 23:00 | G04_pistola   | pintar antioxidante |
      | 2025-06-14 22:25 | 2025-06-14 22:35 | G01_amoladora | cortar malla        |
      | 2025-06-14 22:35 | 2025-06-14 22:50 | G02_soldadora | soldar canasto      |
      | 2025-06-14 22:50 | 2025-06-14 23:00 | G02_soldadora | unir pie            |
      | 2025-06-14 23:00 | 2025-06-15 00:00 | G04_pistola   | pintar antioxidante |

Escenario: Planificación de pedido de Pieza chica en U con 2 unidades y colisiones
    Dado que existe el pedido para el cliente "20000000022" con fecha de entrega "15-06-2025"
    Cuando se solicita planificar el pedido el día "12-06-2025"
    Entonces se espera el siguiente 200 con "Pedido planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea                  |
      | 2025-06-14 21:05 | 2025-06-14 21:20 | G02_soldadora | unir ramas             |
      | 2025-06-14 21:20 | 2025-06-14 22:40 | G03_taladro   | realizar perforaciones |
      | 2025-06-14 22:20 | 2025-06-14 22:35 | G02_soldadora | unir ramas             |
      | 2025-06-14 22:40 | 2025-06-15 00:00 | G03_taladro   | realizar perforaciones |


Escenario: Planificación de pedido masivo de Pieza chica en U con 3 unidades
    Dado que existe el pedido para el cliente "50000000055" con fecha de entrega "15-06-2025"
    Cuando se solicita planificar el pedido el día "12-06-2025"
    Entonces se espera el siguiente 200 con "Pedido planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea                  |
      | 2025-06-14 17:05 | 2025-06-14 17:20 | G02_soldadora | unir ramas             |
      | 2025-06-14 17:20 | 2025-06-14 18:40 | G03_taladro   | realizar perforaciones |
      | 2025-06-14 18:25 | 2025-06-14 18:40 | G02_soldadora | unir ramas             |
      | 2025-06-14 18:40 | 2025-06-14 20:00 | G03_taladro   | realizar perforaciones |
      | 2025-06-14 19:45 | 2025-06-14 20:00 | G02_soldadora | unir ramas             |
      | 2025-06-14 20:00 | 2025-06-14 21:20 | G03_taladro   | realizar perforaciones |