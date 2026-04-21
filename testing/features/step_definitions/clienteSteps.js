const assert = require('assert');
const { Given, When, Then } = require('cucumber');

Given('que se ingresa el cliente con {string}, {word} y {string}', function (razonSocial, cuit, observaciones) {
    this.razonSocial = razonSocial;
    this.cuit = cuit;
    this.observaciones = observaciones;
});


When('presiono el botón de guardar', async function () {
    try {
        const response = await fetch('http://backend:8080/customers', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                razonSocial: this.razonSocial,
                cuit: this.cuit,
                observaciones: this.observaciones
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
});

Then('se espera el siguiente status: {int} con la respuesta: {string}', function (status, respuesta) {
    assert.strictEqual(this.resultado.status, status); // Ya no hace falta parseInt, {int} lo hace solo
    assert.strictEqual(this.resultado.respuesta.trim(), respuesta.trim());
});