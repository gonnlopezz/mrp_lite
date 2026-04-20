#language: es
Característica: gestión de clientes

   Esquema del escenario: Nuevo cliente que encargan pedidos de fabricación de productos
      Dado que se ingresa el cliente con <razonSocial>, <cuit> y <observaciones>
      Cuando presiono el botón de guardar
      Entonces se espera el siguientes <status> con la <respuesta>

    Ejemplos:
      | razonSocial                  | cuit        | status | respuesta                                                                |
      | "prilidiano pueyrredon"      | 10000000011  | 200    | Cliente prilidiano pueyrredon (1000000001) registrado correctamente      |
      | "marcelo t. de alvear"       | 20000000022  | 200    | Cliente marcelo t. de alvear (2000000002) registrado correctamente       | 
      | "domingo faustino sarmiento" | 30000000033  | 200    | Cliente domingo faustino sarmiento (3000000003) registrado correctamente | 
      | "walter runciman"            | 40000000044  | 200    | Cliente walter runciman (4000000004) registrado correctamente            | 
      | "julio argentino roca"       | 50000000055  | 200    | Cliente julio argentino roca (5000000005) registrado correctamente       |
      | "un cliente sin cuit"        | 123         | 400    | El CUIT debe tener exactamente 11 números                                |
      |            ""                | 100000000101 | 400    | La razón social no puede estar vacía                                     |
      | "cliente letras cuit"        | ABCDEFGHIJKG | 400    | El CUIT debe contener solo caracteres numéricos                          |