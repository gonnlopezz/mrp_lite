#language: es
Característica: Proceso de planificación progresivo
  Realiza de forma progresiva todo el camino de planificación desde la creación de los productos, talleres y equipos.
  Aquí solo se prueban los casos positivos, los errores irán por otra característica.

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


  Escenario: Planificación de un producto sobre un taller seleccionado con planificaciones previas
    Dado el producto con nombre "Soporte metálico mediano"
    Y que existe el taller "ALFA"
    Cuando se solicita planificar el producto en el taller el día "01-01-2025"
    Entonces se espera el siguiente 200 con "Producto planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea               |
      | 2025-01-01 01:20 | 2025-01-01 02:20 | A01_amoladora | cortar planchas     |
      | 2025-01-01 02:20 | 2025-01-01 02:40 | A01_amoladora | cortar perfiles     |
      | 2025-01-01 02:50 | 2025-01-01 04:20 | A02_soldadora | armado              |


Escenario: Planificación de un producto sobre un taller seleccionado con mayor capacidad
    Dado el producto con nombre "Andamio básico 2x2x4"
    Dado que existe el taller "BETA"
    Cuando se solicita planificar el producto en el taller el día "01-01-2025"
    Entonces se espera el siguiente 200 con "Producto planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea                   |
      | 2025-01-01 00:00 | 2025-01-01 00:05 | B01_amoladora | cortar caños            |
      | 2025-01-01 00:05 | 2025-01-01 00:15 | B02_taladro   | realizar perforaciones  |
      | 2025-01-01 00:15 | 2025-01-01 00:35 | B03_pistola   | aplicar capa protectora |     


Escenario: Planificación de un producto sin indicar el taller, pero solo uno puede hacerlo
    Dado el producto con nombre "Canasto de basura chico 1,5mts"
    Cuando se solicita planificar el producto el día "01-01-2025"
    Entonces se espera el siguiente 200 con "Producto planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea               |
      | 2025-01-01 00:00 | 2025-01-01 00:30 | G01_amoladora | cortar perfiles     |
      | 2025-01-01 00:30 | 2025-01-01 00:40 | G01_amoladora | cortar malla        |
      | 2025-01-01 00:40 | 2025-01-01 00:55 | G02_soldadora | soldar canasto      |
      | 2025-01-01 00:55 | 2025-01-01 01:05 | G02_soldadora | unir pie            |
      | 2025-01-01 01:05 | 2025-01-01 02:05 | G04_pistola   | pintar antioxidante |


  Escenario: Planificación de un producto sin indicar el taller, pero solo uno puede hacerlo y con las planificaciones previas
    Dado el producto con nombre "Canasto de basura chico 1,5mts"
    Cuando se solicita planificar el producto el día "01-01-2025"
    Entonces se espera el siguiente 200 con "Producto planificado con éxito"
    Y se generaron las siguientes planificaciones
      | inicio           | fin              | equipo        | tarea               |
      | 2025-01-01 00:40 | 2025-01-01 01:10 | G01_amoladora | cortar perfiles     |
      | 2025-01-01 01:10 | 2025-01-01 01:20 | G01_amoladora | cortar malla        |
      | 2025-01-01 01:20 | 2025-01-01 01:35 | G02_soldadora | soldar canasto      |
      | 2025-01-01 01:35 | 2025-01-01 01:45 | G02_soldadora | unir pie            |
      | 2025-01-01 02:05 | 2025-01-01 03:05 | G04_pistola   | pintar antioxidante |


  Escenario: Intentar planificar un producto que no existe
    Dado el producto con nombre "Producto Fantasma"
    Cuando se solicita planificar el producto el día "01-01-2025"
    Entonces se espera el siguiente 404 con "Producto no encontrado"

 Escenario: Intentar planificar en un taller que no existe
    Dado el producto con nombre "Soporte metálico mediano"
    Y que no existe el taller "TALLER_INEXISTENTE"
    Cuando se solicita planificar el producto en el taller el día "01-01-2025"
    Entonces se espera el siguiente 404 con "Taller no encontrado."

  Escenario: Planificación automática falla porque ningún taller tiene los equipos necesarios
    Dado el producto con nombre "Soporte en U para estantería"
    Cuando se solicita planificar el producto el día "01-01-2025"
    Entonces se espera el siguiente 409 con "No se encontró un taller con el equipamiento requerido para el producto"

  Escenario: Intentar planificar en un taller que no tiene los equipos requeridos por el producto
    Dado el producto con nombre "Canasto de basura chico 1,5mts"
    Y que existe el taller "BETA"
    Cuando se solicita planificar el producto en el taller el día "01-01-2025"
    Entonces se espera el siguiente 409 con "El taller no cuenta con los equipos necesarios para fabricar el producto"
