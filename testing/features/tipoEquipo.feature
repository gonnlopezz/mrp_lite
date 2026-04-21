#language: es
Característica: gestión de talleres

  Esquema del escenario: Nuevo tipo de equipo para asignar a talleres
    Dado que se ingresa el tipo de equipo con "<nombre>"
    Cuando presiono el botón de guardar tipoEquipo
    Entonces se espera el siguiente status: <status> con la respuesta: "<respuesta>"
Ejemplos:
      | nombre             | status | respuesta                    
      | amoladora          |    200 | Tipo de equipo amoladora registrado correctamente          |
      | soldadora          |    200 | Tipo de equipo soldadora registrado correctamente          |
      | taladro            |    200 | Tipo de equipo taladro registrado correctamente            |
      | pistola de pintura |    200 | Tipo de equipo pistola de pintura registrado correctamente |
      | ""                 |    400 | El nombre del equipo no puede estar vacío                  |
      | "   "              |    400 | El nombre del equipo no puede estar vacío                  |
      | a                  |    400 | El nombre del equipo debe tener al menos 2 caracteres      |