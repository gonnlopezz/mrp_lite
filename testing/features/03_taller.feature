#language: es
Característica: gestión de talleres

  Esquema del escenario: Nuevo taller sin equipos
    Dado que se ingresa el nuevo taller con "<code>" y "<name>"
    Cuando presiono el botón de guardar taller
    Entonces se espera el siguiente status: <status> con la respuesta: "<respuesta>"
    Ejemplos:
      | code   | name                                | status | respuesta                            |
      | ALFA   | Taller Alfa para lo básico          |    200 | Taller ALFA ingresado correctamente  |
      | BETA   | Taller Beta es muy rápido           |    200 | Taller BETA ingresado correctamente  |
      | GAMA   | Taller Gama puede con todo          |    200 | Taller GAMA ingresado correctamente  |
      | DELTA  | Taller Delta ensamble intermedio    |    200 | Taller DELTA ingresado correctamente |
      | ALFA-2 | Taller Alfa 2 alta capacidad serie  |    200 | Taller ALFA-2 ingresado correctamente|

  Esquema del escenario: Agregar equipos a los talleres existentes
    Dado que existe el taller "<code>"
    Y se agrega el equipo "<equipmentCode>" del tipo "<equipmentType>" y <capacity>
    Cuando presiono el botón de actualizar taller
    Entonces se espera el siguiente status: <status> con la respuesta: "<respuesta>"
    Ejemplos:
      | code   | equipmentCode | equipmentType      | capacity | status | respuesta                               |
      | ALFA   | A01_amoladora | amoladora          |        1 |    200 | Taller ALFA actualizado correctamente   |
      | ALFA   | A02_soldadora | soldadora          |        1 |    200 | Taller ALFA actualizado correctamente   |
      | BETA   | B01_amoladora | amoladora          |        4 |    200 | Taller BETA actualizado correctamente   |
      | BETA   | B02_taladro   | taladro            |        4 |    200 | Taller BETA actualizado correctamente   |
      | BETA   | B03_pistola   | pistola de pintura |        4 |    200 | Taller BETA actualizado correctamente   |
      | GAMA   | G01_amoladora | amoladora          |        2 |    200 | Taller GAMA actualizado correctamente   |
      | GAMA   | G02_soldadora | soldadora          |        2 |    200 | Taller GAMA actualizado correctamente   |
      | GAMA   | G03_taladro   | taladro            |        2 |    200 | Taller GAMA actualizado correctamente   |
      | GAMA   | G04_pistola   | pistola de pintura |        1 |    200 | Taller GAMA actualizado correctamente   |
      | DELTA  | D01_amoladora | amoladora          |        2 |    200 | Taller DELTA actualizado correctamente  |
      | DELTA  | D02_soldadora | soldadora          |        2 |    200 | Taller DELTA actualizado correctamente  |
      | DELTA  | D03_taladro   | taladro            |        1 |    200 | Taller DELTA actualizado correctamente  |
      | ALFA-2 | A2_AMOLAD_01  | amoladora          |        3 |    200 | Taller ALFA-2 actualizado correctamente |
      | ALFA-2 | A2_AMOLAD_02  | amoladora          |        3 |    200 | Taller ALFA-2 actualizado correctamente |
      | ALFA-2 | A2_SOLDAD_01  | soldadora          |        4 |    200 | Taller ALFA-2 actualizado correctamente |
      | ALFA-2 | A2_PISTOL_01  | pistola de pintura |        2 |    200 | Taller ALFA-2 actualizado correctamente |