const assert = require('assert');
const { Given, When, Then } = require('cucumber');

Given('que se ingresa el tipo de equipo con {string}', function (nombre) {
    this.nombre = nombre
});


When('presiono el botón de guardar tipoEquipo', async function () {
    try {
        const response = await fetch('http://backend:8080/equipment-types', {
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
