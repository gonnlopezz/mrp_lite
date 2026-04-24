const assert = require('assert');
const { Then } = require('cucumber');


Then('se espera el siguiente status: {int} con la respuesta: {string}', function (status, respuesta) {
    assert.strictEqual(this.resultado.status, status); 
    assert.strictEqual(this.resultado.respuesta.trim(), respuesta.trim());
});

