const assert = require('assert');
const { Given, When } = require('cucumber');

Given('que se ingresa el nuevo taller con {string} y {string}', function (code, name) {
    this.code = code;
    this.name = name;
    this.equipments = [];
});

Given('que existe el taller {string}', function (code) {
    this.code = code;
});

Given('se agrega el equipo {string} del tipo {string} y {int}', function (equipmentCode, equipmentType, capacity) {
    if (!this.equipments) {
        this.equipments = [];
    }
    const equip = {
        code: equipmentCode,
        capacity: capacity,
        type: { name: equipmentType }
    };

    this.equipments.push(equip);
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
            status: 500,
            respuesta: "Error de conexión: " + error.message
        };
    }
});

When('presiono el botón de actualizar taller', async function () {
    try {
        const getResponse = await fetch(`http://backend:8080/workshops/code/${this.code}`);
        const jsonResponse = await getResponse.json();

        const taller = jsonResponse.data;

        const tallerActualizado = Object.assign({}, taller, {
            equipments: (taller.equipments).concat(this.equipments)
        });

        const putResponse = await fetch('http://backend:8080/workshops', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(tallerActualizado)
        });

        const textoCrudo = await putResponse.text();
        let mensajeFinal = textoCrudo;

        try {
            const jsonParseado = JSON.parse(textoCrudo);
            if (jsonParseado.message) mensajeFinal = jsonParseado.message;
        } catch (e) { }


        this.resultado = {
            status: putResponse.status,
            respuesta: mensajeFinal
        };

    } catch (error) {
        this.resultado = {
            status: 500,
            respuesta: "Error: " + error.message
        };
    }
});