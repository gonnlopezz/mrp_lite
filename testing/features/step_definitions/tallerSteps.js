const assert = require('assert');
const { Given, When } = require('cucumber');

Given('que se ingresa el nuevo taller con {string} y {string}', function (code, name) {
    this.code = code;
    this.name = name;
    this.equipments = [];
});

Given('que existe el taller {string}', async function (code) {
    this.code = code;
    const response = await fetch(`http://backend:8080/workshops/code/${code}`);
    const dataPackage = await response.json();
    const tallerEncontrado = dataPackage.data;

    if (!tallerEncontrado) {
        throw new Error(`No se encontró un taller con el código ${code}`);
    }

    this.tallerActualizado = {
        id: tallerEncontrado.id,
        code: tallerEncontrado.code,
        name: tallerEncontrado.name,
        equipments: tallerEncontrado.equipments
    };
});

Given('se agrega el equipo {string} del tipo {string} y {int}', async function (equipmentCode, equipmentType, capacity) {

    const response = await fetch(`http://backend:8080/equipment-types/search/${equipmentType}`);
    const dataPackage = await response.json();
    const realType = dataPackage.data;

    const equip = {
        code: equipmentCode,
        capacity: capacity,
        type: { 
            id: realType.id,            
            name: realType.name
         }
    };

    this.tallerActualizado.equipments.push(equip);
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

        // console.log("Enviando solicitud para actualizar taller:", JSON.stringify({
        //     id: this.tallerActualizado.id,
        //     code: this.tallerActualizado.code,
        //     name: this.tallerActualizado.name,
        //     equipments: this.tallerActualizado.equipments
        // }, null, 2));
        const putResponse = await fetch('http://backend:8080/workshops', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(this.tallerActualizado)
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