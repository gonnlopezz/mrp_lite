#language: es
Característica: gestión de clientes

   Esquema del escenario: Nuevo cliente que encargan pedidos de fabricación de productos
      Dado que se ingresa el cliente con <razonSocial>, <cuit> y <observaciones>
      Cuando presiono el botón de guardar
      Entonces se espera el siguientes <status> con la <respuesta>

      Ejemplos:
      | razonSocial                | cuit       | status | respuesta                        |
      | prilidiano pueyrredon      | 1000000001 | 200    | Cliente prilidiano pueyrredon (1000000001) registrado correctamente |
      | marcelo t. de alvear       | 2000000002 | 200    | Cliente marcelo t. de alvear (2000000002) registrado correctamente | 
      | domingo faustino sarmiento | 3000000003 | 200    | Cliente domingo faustino sarmiento (3000000003) registrado correctamente | 
      | walter runciman            | 4000000004 | 200    | Cliente walter runciman (4000000004) registrado correctamente | 
      | julio argentino roca       | 5000000005 | 200    | Cliente julio argentino roca (5000000005) registrado correctamente | 

