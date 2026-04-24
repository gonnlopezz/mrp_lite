const assert = require('assert');
const { Given, When, Then } = require('cucumber');



Given('se ingresa un nuevo producto con nombre {string}', function (nombre) {
    this.nombreProducto = nombre;
});



Given('se fabrica haciendo la siguiente lista de tareas', function (tabla) {
    this.listaTareas = tabla.hashes().map(tarea => ({
        nombre: tarea.nombreTarea,
        orden: parseInt(tarea.orden),
        tiempo: parseInt(tarea.tiempo),
        tipoEquipo: tarea.tipoEquipo
    }));
});

When('presiono el botón de guardar producto', async function () {
    try {
        const response = await fetch('http://backend:8080/products', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                nombre: this.nombreProducto,
                tareas: this.listaTareas
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