const assert = require('assert');
const { Given, When, Then } = require('cucumber');



Given('se ingresa un nuevo producto con nombre {string}', function (name) {
    this.productName = name;
});

Given('el producto con nombre {string}', async function (name) {
    this.productName = name;
    const response = await fetch(`http://backend:8080/products/name/${this.productName}`);
    const dataPackage = await response.json();
    this.product = dataPackage.data;
});



Given('se fabrica haciendo la siguiente lista de tareas', async function (tabla) {
    const filas = tabla.hashes();
    this.tasks = await Promise.all(filas.map(async (fila) => {

        const response = await fetch(`http://backend:8080/equipment-types/search/${fila.tipoEquipo}`);
        const dataPackage = await response.json();

        const realType = dataPackage.data;


        return {
            name: fila.nombreTarea,
            orderTask: parseInt(fila.orden),
            duration: parseInt(fila.tiempo),
            type: {
                id: realType.id,
                name: realType.name
            }
        };
    }));
});

When('presiono el botón de guardar producto', async function () {
    try {
        // console.log("Enviando solicitud para crear producto:", JSON.stringify({
        //     name: this.productName,
        //     tasks: this.tasks
        // }, null, 2));
        const response = await fetch('http://backend:8080/products', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: this.productName,
                tasks: this.tasks
            })
        });


        const textoCrudo = await response.text();
        let mensajeFinal = textoCrudo;

        try {
            const jsonParseado = JSON.parse(textoCrudo);
            if (jsonParseado.message) mensajeFinal = jsonParseado.message;
        } catch (e) { }

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

Then('se espera el siguiente {int} con {string}', function (status, mensajeEsperado) {
    assert.strictEqual(this.resultado.status, status);
    assert.strictEqual(this.resultado.respuesta.trim(), mensajeEsperado.trim());
});