# language: es

Característica: administrar pedidos de fabricación
   permite generar, recuperar y gestionar los pedidos de fabricación

  Esquema del escenario: Carga de clientes para los pedidos de fabricación
    Dado que se ingresa el cliente con "<razonSocial>" y <cuit>
    Cuando presiono el botón de guardar cliente
    Entonces se espera el siguiente <status> con "<respuesta>"
    Ejemplos:
      | razonSocial    | cuit        | status | respuesta                                                   |
      | Marty McFly    | 27123456781 |    200 | Cliente Marty McFly 27123456781 registrado correctamente    |
      | Sarah Connor   | 20304958722 |    200 | Cliente Sarah Connor 20304958722 registrado correctamente   |
      | Luke Skywalker | 23176843593 |    200 | Cliente Luke Skywalker 23176843593 registrado correctamente |
      | Ellen Ripley   | 27982145634 |    200 | Cliente Ellen Ripley 27982145634 registrado correctamente   |
      | Frodo Bolsón   | 20654239875 |    200 | Cliente Frodo Bolsón 20654239875 registrado correctamente   |

  Esquema del escenario: Generar nuevos pedidos de fabricación
    Dado el producto con nombre "<producto>"
    Y el cliente con <cuit>
    Cuando se solicita generar un pedido para ese cliente fecha de pedido "<fechaPedido>" para entregar en la fecha "<fechaEntrega>" la cantidad de <cantidad> del producto
    Entonces se espera el siguiente <status> con "<respuesta>"
    Ejemplos:
      | cuit        | fechaPedido  | fechaEntrega | cantidad | producto                  | status | respuesta                                    |
      | 27123456781 | 2025-02-01   | 2025-02-03   |        1 | Soporte metálico mediano  |    200 | Pedido de fabricación generado correctamente |
      | 20304958722 | 2025-02-04   | 2025-02-05   |        3 | Soporte metálico mediano  |    200 | Pedido de fabricación generado correctamente |
      | 23176843593 | 2025-02-08   | 2025-02-10   |       20 | Soporte metálico mediano  |    200 | Pedido de fabricación generado correctamente |
      | 27982145634 | 2025-02-09   | 2025-02-10   |        1 | Soporte metálico mediano  |    200 | Pedido de fabricación generado correctamente |      

  Esquema del escenario: Generar nuevos pedidos de fabricación para el proceso de planificación
    Dada el producto con nombre "<producto>"
    Y el cliente con <cuit>
    Cuando se solicita generar un pedido para ese cliente fecha de pedido "<fechaPedido>" para entregar en la fecha "<fechaEntrega>" la cantidad de <cantidad> del producto
    Entonces se espera el siguiente <status> con "<respuesta>"
    Ejemplos:
      | cuit        | fechaPedido  | fechaEntrega | cantidad | producto                        | status | respuesta                                    |
      | 20654239875 | 2025-03-01   | 2025-03-06   |        4 | Canasto de basura chico 1,5mts  |    200 | Pedido de fabricación generado correctamente |
      | 20654239875 | 2025-03-01   | 2025-03-06   |        3 | Pieza chica en U                |    200 | Pedido de fabricación generado correctamente |
      
 
