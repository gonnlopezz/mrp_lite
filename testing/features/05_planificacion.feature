Característica: Proceso de planificación progresivo
  Escenario: Planificación de un producto sobre un taller seleccionado vacío
    Dado el producto con nombre "Soporte metálico mediano"
    Y que existe el taller "ALFA"
    Cuando se solicita planificar el producto en el taller el día "01-01-2025"
    Entonces se espera el siguiente 200 con "Producto planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea               |
      | 2025-01-01 00:00 | 2025-01-01 01:00 | A01_amoladora | cortar planchas     |
      | 2025-01-01 01:00 | 2025-01-01 01:20 | A01_amoladora | cortar perfiles     |
      | 2025-01-01 01:20 | 2025-01-01 02:50 | A02_soldadora | armado              |
