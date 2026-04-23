#language: es
Característica: gestión de clientes

   Esquema del escenario: Nuevo cliente que encargan pedidos de fabricación de productos
      Dado que se ingresa el cliente con <companyName>, <cuit> y <observations>
      Cuando presiono el botón de guardar
      Entonces se espera el siguiente status: <status> con la respuesta: <respuesta>

    Ejemplos:
      | companyName                  | cuit         | observations | status | respuesta                                                                   |
      | "prilidiano pueyrredon"      | 10000000013  | ""            | 200    | "Cliente prilidiano pueyrredon (10000000013) registrado correctamente"      |
      | "marcelo t. de alvear"       | 20000000022  | ""            | 200    | "Cliente marcelo t. de alvear (20000000022) registrado correctamente"       | 
      | "domingo faustino sarmiento" | 30000000033  | ""            | 200    | "Cliente domingo faustino sarmiento (30000000033) registrado correctamente" | 
      | "walter runciman"            | 40000000044  | ""            | 200    | "Cliente walter runciman (40000000044) registrado correctamente"            | 
      | "julio argentino roca"       | 50000000055  | ""            | 200    | "Cliente julio argentino roca (50000000055) registrado correctamente"        |