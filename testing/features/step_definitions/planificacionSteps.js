const assert = require('assert');
const { Given, When, Then } = require('cucumber');

// Given('el producto con nombre {string}', async function (productName) {
//     this.payloadPlanificacion = this.payloadPlanificacion || {};
//     this.payloadPlanificacion.productName = productName;
// });


Given('el criterio de selección es alfabeticamente por código', function () {

});

When('se solicita planificar el producto en el taller el día {string}', async function (date) {
    const [dia, mes, año] = date.split('-');
    const fechaISO = `${año}-${mes}-${dia}T00:00:00`;

    const response = await fetch('http://backend:8080/plannings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            startDate: fechaISO,
            productName: this.productName,
            workshopCode: this.workshopCode
        })
    });

    const data = await response.json();
    this.resultado = {
        status: response.status,
        respuesta: data.message
    };
    this.responseBody = data.data;
});

When('se solicita planificar el producto el día {string}', async function (date) {
    const [dia, mes, año] = date.split('-');
    const fechaISO = `${año}-${mes}-${dia}T00:00:00`;

    const response = await fetch('http://backend:8080/plannings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            startDate: fechaISO,
            productName: this.productName,
            workshopCode: ""
        })
    });


    const data = await response.json();
    this.resultado = {
        status: response.status,
        respuesta: data.message
    };
    this.responseBody = data.data;
});



Then('se generaron las siguientes planificaciones', function (dataTable) {

    const planificacionesEsperadas = dataTable.hashes();
    const planificacionesReal = this.responseBody.plannings;

    assert.ok(planificacionesReal, "El backend no devolvió planificaciones");
    assert.strictEqual(planificacionesReal.length, planificacionesEsperadas.length,
        `La cantidad de tareas planificadas no es la correcta`);

    planificacionesEsperadas.forEach((esperada, index) => {
        const real = planificacionesReal[index];

        const inicio = real.period.start.replace('T', ' ').substring(0, 16);
        const fin = real.period.endDate.replace('T', ' ').substring(0, 16);
        const equipo = real.equipment.code;
        const tarea = real.task.name;

        assert.strictEqual(inicio, esperada.inicio, `Error en inicio (Fila ${index + 1})`);
        assert.strictEqual(fin, esperada.fin, `Error en fin (Fila ${index + 1})`);
        assert.strictEqual(equipo, esperada.equipo, `Error en equipo (Fila ${index + 1})`);
        assert.strictEqual(tarea, esperada.tarea, `Error en tarea (Fila ${index + 1})`);
    });
});