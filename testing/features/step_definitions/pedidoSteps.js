const assert = require('assert');
const { Given, When, Then } = require('cucumber');

When('se solicita generar un pedido para ese cliente fecha de pedido {string} para entregar en la fecha {string} la cantidad de {int} del producto') , function (fechaPedido, fechaEntrega, cantidad) {
    this.pedido = {
        fechaPedido: fechaPedido,
        fechaEntrega: fechaEntrega,
        cantidad: cantidad
    };
    try {
        const response = await fetch('http://backend:8080/orders', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                cuit: this.cuit,
                product: this.product,
                fechaPedido: this.pedido.fechaPedido,
                fechaEntrega: this.pedido.fechaEntrega,
                cantidad: this.pedido.cantidad
            })
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
};
