const assert = require('assert');
const { Given, When } = require('cucumber');

Given('que se ingresa el nuevo taller con {string} y {string}', function (code, name) {
    this.code = code;
    this.equipos = [];
});

Given('que existe el taller {string}', function (code) {
    this.code = code;
});

Given('se agrega el equipo {string} del tipo {string} y {int}', function (equipmentCode, equipmentType, capacity) {
    if(!this.equipos) this.equipos = [];

    this.equipos.push = {
        equipmentCode: equipmentCode,
        equipmentType: equipmentType,
        capacity: capacity
    }
});

When('presiono el botón de guardar taller', async function () {
    try {
        const response = await fetch('http://backend:8080/workshops', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            

            body: JSON.stringify({
                code: this.code,
                name: this.name
            })
        });

        const textoCrudo = await response.text();
        let mensajeFinal = textoCrudo;

        try {
            const jsonParseado = JSON.parse(textoCrudo);
            if (jsonParseado.message) {
                mensajeFinal = jsonParseado.message;
            }
        } catch (e) { }

        this.resultado = {
            status: response.status,
            respuesta: mensajeFinal
        }

    } catch (error) {
        this.resultado = {
            status:500,
            respuesta: "Error de conexión: " + error.message
        };
    }
});

When('presiono el botón de actualizar taller', async function () {
    try {
        const response = await fetch('http//backend:8080/workshops/code/{this.code}', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },

            body: JSON.stringify(this.equipos)
        });

        const textoCrudo = await response.text();
        let mensajeFinal = textoCrudo;

        try {
            const jsonParseado = JSON.parse(textoCrudo);
            if (jsonParseado.message) {
                mensajeFinal = jsonParseado.message;
            }
        } catch (e) { }

        this.resultado = {
            status: response.status,
            respuesta: mensajeFinal
        }
    } catch (error) {
        this.resultado = {
            status:500,
            respuesta: "Error de conexión: " + error.message
        };  
    }
})

