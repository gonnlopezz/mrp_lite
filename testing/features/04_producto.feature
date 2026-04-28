#language: es
Característica: gestión de productos

  Escenario: Crear un producto con sus tareas que pueda fabricarse en el taller ALFA
    Dado se ingresa un nuevo producto con nombre "Soporte metálico mediano"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea         | orden | tiempo | tipoEquipo         |
      | cortar planchas     |     1 |     60 | amoladora          |
      | cortar perfiles     |     2 |     20 | amoladora          |
      | armado              |     3 |     90 | soldadora          |      
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Soporte metálico mediano creado exitosamente"

  Escenario: Crear un producto con sus tareas que pueda fabricarse en el taller BETA
    Dado se ingresa un nuevo producto con nombre "Andamio básico 2x2x4"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea             | orden | tiempo | tipoEquipo         |
      | cortar caños            |     1 |     20 | amoladora          |
      | realizar perforaciones  |     2 |     40 | taladro            |
      | aplicar capa protectora |     3 |     80 | pistola de pintura |      
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Andamio básico 2x2x4 creado exitosamente"

  Escenario: Crear un producto con sus tareas que pueda fabricarse solo en el taller GAMA
    Dado se ingresa un nuevo producto con nombre "Canasto de basura chico 1,5mts"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea         | orden | tiempo | tipoEquipo         |
      | cortar perfiles     |     1 |     60 | amoladora          |
      | cortar malla        |     2 |     20 | amoladora          |
      | soldar canasto      |     3 |     30 | soldadora          |
      | unir pie            |     4 |     20 | soldadora          |
      | pintar antioxidante |     5 |     60 | pistola de pintura |
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Canasto de basura chico 1,5mts creado exitosamente"
  
  Escenario: Crear un producto con sus tareas que pueda fabricarse solo en el taller GAMA, diferente al anterior.
    Dado se ingresa un nuevo producto con nombre "Pieza chica en U"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea            | orden | tiempo | tipoEquipo         |
      | unir ramas             |     1 |     30 | soldadora          |
      | realizar perforaciones |     2 |    160 | taladro            |
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Pieza chica en U creado exitosamente"
