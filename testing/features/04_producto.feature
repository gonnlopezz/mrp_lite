#language: es
Característica: gestión de productos

  Escenario: Crear un producto con sus tareas que pueda fabricarse en el taller ALFA
    Dado se ingresa un nuevo producto con nombre "Soporte metálico mediano"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea     | orden | tiempo | tipoEquipo |
      | cortar planchas |     1 |     60 | amoladora  |
      | cortar perfiles |     2 |     20 | amoladora  |
      | armado          |     3 |     90 | soldadora  |       
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
      | nombreTarea            | orden | tiempo | tipoEquipo |
      | unir ramas             |     1 |     30 | soldadora  |
      | realizar perforaciones |     2 |    160 | taladro    |
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Pieza chica en U creado exitosamente"

  Escenario: Crear un producto con sus tareas que no pueda fabricarse en ningun taller (Falla Estructural).
    Dado se ingresa un nuevo producto con nombre "Soporte en U para estantería"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea            | orden | tiempo | tipoEquipo |
      | unir ramas             |     1 |     30 | soldadora  |
      | realizar perforaciones |     2 |    160 | taladro    |
      | cortar maderas         |     3 |     50 | sierra     |
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Soporte en U para estantería creado exitosamente"

  Escenario: Crear el Producto 6 que explote la redundancia de amoladoras en DELTA y ALFA-2
    Dado se ingresa un nuevo producto con nombre "Estanteria Pesada Industrial"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea                  | orden | tiempo | tipoEquipo |
      | Cortar perfiles estructurales |     1 |     45 | amoladora  |
      | Perforar anclajes de base     |     2 |     30 | taladro    |
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Estanteria Pesada Industrial creado exitosamente"

  Escenario: Crear el Producto 7 con alta demanda de pintura aplicable en BETA, GAMA y ALFA-2
    Dado se ingresa un nuevo producto con nombre "Gabinete Electrico Estanco"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea                    | orden | tiempo | tipoEquipo         |
      | Dimensionar chapa plegada      |     1 |     50 | amoladora          |
      | Soldar escuadras y bisagras    |     2 |     40 | soldadora          |
      | Acabado con pintura poliuretan |     3 |     60 | pistola de pintura |
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Gabinete Electrico Estanco creado exitosamente"

  Escenario: Crear el Producto 8 de alta carga operativa para soldadura pesada
    Dado se ingresa un nuevo producto con nombre "Contenedor de Residuos 1000L"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea                   | orden | tiempo | tipoEquipo |
      | Cortar chapas de piso 1/4     |     1 |     90 | amoladora  |
      | Soldadura perimetral reforzad |     2 |    120 | soldadora  |
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Contenedor de Residuos 1000L creado exitosamente"

  Escenario: Crear el Producto 9 que cruce operaciones ligeras entre talleres mecanizados
    Dado se ingresa un nuevo producto con nombre "Carro de Carga de 4 Ruedas"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea                  | orden | tiempo | tipoEquipo |
      | Estructurar chasis inferior  |     1 |     35 | soldadora  |
      | Perforar ejes de rodamiento  |     2 |     25 | taladro    |
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Carro de Carga de 4 Ruedas creado exitosamente"

  Escenario: Crear el Producto 10 de maxima superposicion tecnologica
    Dado se ingresa un nuevo producto con nombre "Brazo de Izaje Articulado"
    Y se fabrica haciendo la siguiente lista de tareas
      | nombreTarea                   | orden | tiempo | tipoEquipo |
      | Cortar vigas doble T          |     1 |     60 | amoladora  |
      | Mecanizar buje central        |     2 |     50 | taladro    |
      | Union de costillas de refuerz |     3 |     80 | soldadora  |
    Cuando presiono el botón de guardar producto
    Entonces se espera el siguiente 200 con "Producto Brazo de Izaje Articulado creado exitosamente"