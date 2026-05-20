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
      | 2025-01-02 21:00 | 2025-01-02 22:10 | A01_amoladora | cortar planchas     |
      | 2025-01-02 22:10 | 2025-01-02 22:30 | A01_amoladora | cortar perfiles     |
      | 2025-01-02 22:30 | 2025-01-03 00:00 | A02_soldadora | armado              |
