#language: es
Característica: gestión de talleres

  Esquema del escenario: Nuevo tipo de equipo para asignar a talleres
    Dado que se ingresa el tipo de equipo con <name>
    Cuando presiono el botón de guardar tipoEquipo
    Entonces se espera el siguiente status: <status> con la respuesta: <respuesta>
Ejemplos:
      | name             | status | respuesta                                                  |
      | "amoladora"          |    200 | "Tipo de equipo amoladora registrado correctamente"          |
      | "soldadora"          |    200 | "Tipo de equipo soldadora registrado correctamente"          |
      | "taladro"            |    200 | "Tipo de equipo taladro registrado correctamente"            |
      | "pistola de pintura" |    200 | "Tipo de equipo pistola de pintura registrado correctamente" |