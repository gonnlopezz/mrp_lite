# language: es
Característica: Proceso de planificación progresivo
  Realiza de forma progresiva todo el camino de planificacion desde la creación de los productos, talleres y equipos.
  Aquí solo se prueban los casos positivos, los errores iran por otra característica.

  Escenario: Planificación de un pedido con un producto para estar terminado en la fecha de entrega.
    Dado que existe el pedido para el cliente "27123456781" con fecha de entrega "03-02-2025"
    Cuando se solicita planificar el pedido el día "01-02-2025"
    Entonces se espera el siguiente 200 con "Pedido planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea               |
      | 2025-02-02 21:10 | 2025-02-02 22:10 | A01_amoladora | cortar planchas     |
      | 2025-02-02 22:10 | 2025-02-02 22:30 | A01_amoladora | cortar perfiles     |
      | 2025-02-02 22:30 | 2025-02-03 00:00 | A02_soldadora | armado              |


  Escenario: Planificación de un pedido con varios productos para estar terminado en la fecha de entrega.
    Dado que existe el pedido para el cliente "20304958722" con fecha de entrega "05-02-2025"
    Cuando se solicita planificar el pedido el día "04-02-2025"
    Entonces se espera el siguiente 200 con "Pedido planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea               |
      | 2025-02-04 18:10 | 2025-02-04 19:10 | A01_amoladora | cortar planchas     |
      | 2025-02-04 19:10 | 2025-02-04 19:30 | A01_amoladora | cortar perfiles     |
      | 2025-02-04 19:30 | 2025-02-04 21:00 | A02_soldadora | armado              |
      | 2025-02-04 19:40 | 2025-02-04 20:40 | A01_amoladora | cortar planchas     |
      | 2025-02-04 20:40 | 2025-02-04 21:00 | A01_amoladora | cortar perfiles     |
      | 2025-02-04 21:00 | 2025-02-04 22:30 | A02_soldadora | armado              |
      | 2025-02-04 21:10 | 2025-02-04 22:10 | A01_amoladora | cortar planchas     |
      | 2025-02-04 22:10 | 2025-02-04 22:30 | A01_amoladora | cortar perfiles     |
      | 2025-02-04 22:30 | 2025-02-05 00:00 | A02_soldadora | armado              |


  Escenario: Planificación de un pedido con muchos productos para estar terminado en la fecha de entrega.
    Dado que existe el pedido para el cliente "23176843593" con fecha de entrega "10-02-2025"
    Cuando se solicita planificar el pedido el día "08-02-2025"
    Entonces se espera el siguiente 200 con "Pedido planificado con éxito"
    Y se generaron 40 planificaciones para el equipo "A01_amoladora"
    Y se generaron 20 planificaciones para el equipo "A02_soldadora"

Escenario: Planificación de un pedido con el taller primera opción completamente ocupado.
    Dado que existe el pedido para el cliente "27982145634" con fecha de entrega "10-02-2025"
    Cuando se solicita planificar el pedido el día "09-02-2025"
    Entonces se espera el siguiente 200 con "Pedido planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea               |
      | 2025-02-09 22:35 | 2025-02-09 23:05 | G01_amoladora | cortar planchas     |
      | 2025-02-09 23:05 | 2025-02-09 23:15 | G01_amoladora | cortar perfiles     |
      | 2025-02-09 23:15 | 2025-02-10 00:00 | G02_soldadora | armado              |


Escenario: Planificación de todos los pedidos pendientes.
    Dado que existe el pedido para el cliente "27982145634" con fecha de entrega "10-02-2025"
    Cuando se solicita planificar el pedido el día "09-02-2025"
    Entonces se espera el siguiente 200 con "Pedido planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea           |
      | 2025-02-09 21:50 | 2025-02-09 22:20 | G01_amoladora | cortar planchas |
      | 2025-02-09 22:20 | 2025-02-09 22:30 | G01_amoladora | cortar perfiles |
      | 2025-02-09 22:30 | 2025-02-09 23:15 | G02_soldadora | armado          |


  Escenario: Planificación de todos los pedidos pendientes. Y uso de huecos en equipos.
    Dado que existen los pedidos pendientes de planificacion antes cargados
    Cuando se solicita planificar el pedido el día "02-03-2025"
    Entonces se espera el siguiente 200 con "Pedido planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea                  |
      | 2025-03-05 18:55 | 2025-03-05 19:25 | G01_amoladora | cortar perfiles        |
      | 2025-03-05 19:55 | 2025-03-05 20:25 | G01_amoladora | cortar perfiles        |
      | 2025-03-05 20:55 | 2025-03-05 21:25 | G01_amoladora | cortar perfiles        |
      | 2025-03-05 21:55 | 2025-03-05 22:25 | G01_amoladora | cortar perfiles        |
      | 2025-03-05 19:25 | 2025-03-05 19:35 | G01_amoladora | cortar malla           |
      | 2025-03-05 20:25 | 2025-03-05 20:35 | G01_amoladora | cortar malla           |
      | 2025-03-05 21:25 | 2025-03-05 21:35 | G01_amoladora | cortar malla           |
      | 2025-03-05 22:25 | 2025-03-05 22:35 | G01_amoladora | cortar malla           |
      | 2025-03-05 19:35 | 2025-03-05 19:50 | G02_soldadora | soldar canasto         |
      | 2025-03-05 20:35 | 2025-03-05 20:50 | G02_soldadora | soldar canasto         |
      | 2025-03-05 21:35 | 2025-03-05 21:50 | G02_soldadora | soldar canasto         |
      | 2025-03-05 22:35 | 2025-03-05 22:50 | G02_soldadora | soldar canasto         |
      | 2025-03-05 19:50 | 2025-03-05 20:00 | G02_soldadora | unir pie               |
      | 2025-03-05 20:50 | 2025-03-05 21:00 | G02_soldadora | unir pie               |
      | 2025-03-05 21:50 | 2025-03-05 22:00 | G02_soldadora | unir pie               |
      | 2025-03-05 22:50 | 2025-03-05 23:00 | G02_soldadora | unir pie               |
      | 2025-03-05 20:00 | 2025-03-05 21:00 | G04_pistola   | pintar antioxidante    |
      | 2025-03-05 21:00 | 2025-03-05 22:00 | G04_pistola   | pintar antioxidante    |
      | 2025-03-05 22:00 | 2025-03-05 23:00 | G04_pistola   | pintar antioxidante    |
      | 2025-03-05 23:00 | 2025-03-06 00:00 | G04_pistola   | pintar antioxidante    |
      | 2025-03-05 19:20 | 2025-03-05 19:35 | G02_soldadora | unir ramas             |
      | 2025-03-05 21:05 | 2025-03-05 21:20 | G02_soldadora | unir ramas             |
      | 2025-03-05 22:20 | 2025-03-05 22:35 | G02_soldadora | unir ramas             |
      | 2025-03-05 20:00 | 2025-03-05 21:20 | G03_taladro   | realizar perforaciones |
      | 2025-03-05 21:20 | 2025-03-05 22:40 | G03_taladro   | realizar perforaciones |
      | 2025-03-05 22:40 | 2025-03-06 00:00 | G03_taladro   | realizar perforaciones |
