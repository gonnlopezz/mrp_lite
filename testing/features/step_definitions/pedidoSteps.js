const assert = require('assert');
const { Given, When, Then } = require('cucumber');

Given('que existe el pedido para el cliente {string} con fecha de entrega {string}', async function (aCustomerCuit, aFechaEntrega) {
    const [dia, mes, año] = aFechaEntrega.split('-');
    const fechaISO = `${año}-${mes}-${dia}`;
    
    const response = await fetch(`http://backend:8080/orders/cuit/${aCustomerCuit}/deliveryDate/${fechaISO}`);
    const dataPackage = await response.json(); 
    this.pedido = dataPackage.data;
});

Given('que existen los pedidos pendientes de planificacion antes cargados', async function () {
    return true;
});


When('se solicita generar un pedido para ese cliente fecha de pedido {string} para entregar en la fecha {string} la cantidad de {int} del producto' , async function (fechaPedido, fechaEntrega, cantidad) {

    this.pedido = {
        orderDate: fechaPedido,
        deliveryDate: fechaEntrega,
        quantity: cantidad,
        customer: this.customer,
        product: this.product
    };

 
    try {
        const response = await fetch('http://backend:8080/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(this.pedido)
        });
        

        const textoCrudo = await response.text();
        let mensajeFinal = textoCrudo; 
        
        try {
            const jsonParseado = JSON.parse(textoCrudo);
            if (jsonParseado.message) {
                mensajeFinal = jsonParseado.message;
            }
        } catch (e) {
        }

        this.resultado = {
            status: response.status,
            respuesta: mensajeFinal
        };
    } catch (error) {
        this.resultado = {
            status: 500,
            respuesta: "Error de conexión: " + error.message
        };
    }
});
