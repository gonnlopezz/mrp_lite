const assert = require('assert');
const { Given, When } = require('cucumber');

Given('que se ingresa el nuevo taller con {string} y {string}', function (code, name) {
    this.workshopCode = code;
    this.name = name;
    this.equipments = [];
});

Given('que existe el taller {string}', async function (code) {
    this.workshopCode = code;
    const response = await fetch(`http://backend:8080/workshops/code/${this.workshopCode}`);
    const dataPackage = await response.json();
    this.taller = dataPackage.data;
});

Given('que no existe el taller {string}', function (workshopCode) {
    this.workshopCode = workshopCode;
});

Given('se agrega el equipo {string} del tipo {string} y {int}', async function (equipmentCode, equipmentType, capacity) {

    const response = await fetch(`http://backend:8080/equipment-types/search/${equipmentType}`);
    const dataPackage = await response.json();
    const realType = dataPackage.data;

    const equip = {
        código: equipmentCode,
        capacidad: capacity,
        tipo: { 
            id: realType.id,            
            nombre: realType.nombre
         }
    };

    this.taller.equipos.push(equip);
});

When('presiono el botón de guardar taller', async function () {
    try {

        const response = await fetch('http://backend:8080/workshops', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },


            body: JSON.stringify({
                codigo: this.workshopCode,
                nombre: this.name
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
            body: JSON.stringify(this.taller)
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