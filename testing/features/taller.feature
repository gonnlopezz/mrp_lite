#language: es
Característica: gestión de talleres

  Esquema del escenario: Nuevo taller sin equipos
    Dado que se ingresa el nuevo taller con "<codigo>" y "<nombre>"
    Cuando presiono el botón de guardar taller
    Entonces se espera el siguiente <status> con "<respuesta>"
    Ejemplos:
      | codigo | nombre                      | status | respuesta                            |
      | ALFA   | Taller Alfa para lo básico  |    200 | Taller ALFA ingresado correctamente  |
      | BETA   | Taller Beta es muy rápido   |    200 | Taller BETA ingresado correctamente  |
      | GAMA   | Taller Gama puede con todo  |    200 | Taller GAMA ingresado correctamente  |

  Esquema del escenario: Agregar equipos a los talleres existente
    Dado que existe el taller "<codigoTaller>"
    Y se agrega el equipo "<codigoEquipo>" del tipo "<tipoEquipo>" y <capacidad>
    Cuando presiono el botón de actualizar taller
    Entonces se espera el siguiente <status> con "<respuesta>"
    Ejemplos:
      | codigoTaller | codigoEquipo               | tipoEquipo         | capacidad | status | respuesta                             |
      | ALFA         | A01_amoladora              | amoladora          |         1 |    200 | Taller ALFA actualizado correctamente |
      | ALFA         | A02_soldadora              | soldadora          |         1 |    200 | Taller ALFA actualizado correctamente |
      | BETA         | B01_amoladora              | amoladora          |         4 |    200 | Taller BETA actualizado correctamente |
      | BETA         | B02_taladro                | taladro            |         4 |    200 | Taller BETA actualizado correctamente |
      | BETA         | B03_pistola                | pistola de pintura |         4 |    200 | Taller BETA actualizado correctamente |
      | GAMA         | G01_amoladora              | amoladora          |         2 |    200 | Taller GAMA actualizado correctamente |
      | GAMA         | G02_soldadora              | soldadora          |         2 |    200 | Taller GAMA actualizado correctamente |
      | GAMA         | G03_taladro                | taladro            |         2 |    200 | Taller GAMA actualizado correctamente |
      | GAMA         | G04_pistola                | pistola de pintura |         1 |    200 | Taller GAMA actualizado correctamente |
