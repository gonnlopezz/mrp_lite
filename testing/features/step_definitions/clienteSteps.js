
const assert = require('assert');
const { Given, When, Then } = require('cucumber');
const axios = require('axios');

Given('que se ingresa el cliente con {string}, {int} y {string}', function (razonSocial, cuit, observaciones) {
    this.razonSocial = razonSocial;
    this.cuit = cuit;
    this.observaciones = observaciones;
});

When('presiono el boton de guardar', async function () {
    try {
        const response = await axios.post('http://backend:8000/customers', {
            razonSocial: this.razonSocial,
            cuit: this.cuit,
            observaciones: this.observaciones
        });
        this.resultado = {
            status: response.status,
            respuesta: response.data.message
        };
    } catch (error) {
        this.resultado = {
            status: error.response.status,
            respuesta: error.response.data.message
        };
    }
});


Then('se espera el siguiente {int} con la {string}', function (status, respuesta) {
    assert.strictEqual(this.resultado.status, parseInt(status));
    assert.strictEqual(this.resultado.respuesta, respuesta);
});
