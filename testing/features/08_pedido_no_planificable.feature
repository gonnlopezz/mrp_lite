  # language: es
  Característica: Generar pedidos que no puedan ser fabricables 

  Esquema del escenario: Generar nuevos pedidos de fabricación no planificables
    Dada el producto con nombre "<producto>"
    Y el cliente con <cuit>
    Cuando se solicita generar un pedido para ese cliente fecha de pedido "<fechaPedido>" para entregar en la fecha "<fechaEntrega>" la cantidad de <cantidad> del producto
    Entonces se espera el siguiente <status> con "<respuesta>"
    Ejemplos:
      | cuit        | fechaPedido  | fechaEntrega | cantidad | producto                      | status | respuesta                                    |
      | 20654239875 | 2025-06-01   | 2025-06-06   |        3 | Soporte en U para estantería  |    200 | Pedido de fabricación generado correctamente |
      | 27123456781 | 2025-06-03   | 2025-06-03   |        1 | Soporte en U para estantería  |    200 | Pedido de fabricación generado correctamente |

Escenario: Generación exitosa de un pedido no planificable con alta cantidad de unidades
    Dado el producto con nombre "Soporte metálico mediano"
    Y el cliente con 30000000033
    Cuando se solicita generar un pedido para ese cliente fecha de pedido "2025-02-09" para entregar en la fecha "2025-02-10" la cantidad de 100 del producto
    Entonces se espera el siguiente status: 200 con la respuesta: "Pedido de fabricación generado correctamente"

    Esquema del escenario: Generar nuevos pedidos de fabricación no planificables y planificables
    Dada el producto con nombre "<producto>"
    Y el cliente con <cuit>
    Cuando se solicita generar un pedido para ese cliente fecha de pedido "<fechaPedido>" para entregar en la fecha "<fechaEntrega>" la cantidad de <cantidad> del producto
    Entonces se espera el siguiente <status> con "<respuesta>"
    Ejemplos:
      | cuit        | fechaPedido | fechaEntrega | cantidad | producto                     | status | respuesta                                    |
      | 10000000013 | 2025-06-03  | 2025-06-09   |        1 | Soporte en U para estantería |    200 | Pedido de fabricación generado correctamente |
      | 10000000013 | 2025-06-01  | 2025-06-09   |        3 | Soporte en U para estantería |    200 | Pedido de fabricación generado correctamente |
      | 10000000013 | 2025-06-01  | 2025-06-15   |        2 | Soporte metálico mediano     |    200 | Pedido de fabricación generado correctamente |