const assert = require('assert');
const { Given, When, Then } = require('cucumber');

Given('que se ingresa el tipo de equipo con {string}', function (nombre) {
    this.nombre = nombre
});


When('presiono el botón de guardar tipoEquipo', async function () {
    try {
        const response = await fetch('http://backend:8080/equipmentTypes', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                nombre: this.nombre
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