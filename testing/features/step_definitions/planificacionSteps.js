const assert = require('assert');
const { Given, When, Then } = require('cucumber');


Given('el criterio de selección es alfabeticamente por código', function () {
    return true;
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


When('se solicita planificar el pedido el día {string}', async function (date) {
    const [dia, mes, año] = date.split('-');
    const fechaISO = `${año}-${mes}-${dia}T00:00:00`;

    const response = await fetch('http://backend:8080/plannings/order', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            order: this.pedido,
            startDate: fechaISO
        })
    });

    const data = await response.json();
    this.resultado = { status: response.status, respuesta: data.message };
    this.responseBody = data.data;
});


When('se solicita planificar todos los pedidos pendientes el día {string}', async function (date) {
    const [dia, mes, año] = date.split('-');
    const fechaISO = `${año}-${mes}-${dia}T00:00:00`;

    const response = await fetch('http://backend:8080/plannings/pending', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            startDate: fechaISO
        })
    });

    const data = await response.json();
    this.resultado = { status: response.status, respuesta: data.message };
    this.responseBody = data.data;
});




Then('se generaron las siguientes planificaciones', function (dataTable) {
    const planificacionesEsperadas = dataTable.hashes();
    
    let procesosRaw = this.responseBody.data ? this.responseBody.data : this.responseBody;
    
    const procesos = Array.isArray(procesosRaw) ? procesosRaw : [procesosRaw];

    let planificacionesReal = [];
    procesos.forEach(proceso => {
        if (proceso && proceso.plannings) {
            planificacionesReal.push(...proceso.plannings);
        }
    });

    planificacionesReal.sort((a, b) => {
        const dateA = new Date(a.period.start);
        const dateB = new Date(b.period.start);
        
        if (dateA.getTime() === dateB.getTime()) {
            return a.equipment.code.localeCompare(b.equipment.code); // Desempate
        }
        return dateA - dateB;
    });

    assert.strictEqual(planificacionesReal.length, planificacionesEsperadas.length, 
        `La cantidad de tareas no coincide. Esperadas: ${planificacionesEsperadas.length}, Reales: ${planificacionesReal.length}`);
    planificacionesEsperadas.forEach((esperada, index) => {
        const real = planificacionesReal[index];
        const inicio = real.period.start.replace('T', ' ').substring(0, 16);
        const fin = real.period.endDate.replace('T', ' ').substring(0, 16);
        const equipo = real.equipment.code;
        const tarea = real.task.name;
        // if (inicio.startsWith('2025-03-05')) {
        //     console.log(`| ${inicio} | ${fin} | ${equipo} | ${tarea}        |`);
        // }

        assert.strictEqual(inicio, esperada.inicio, `Error en inicio (Fila ${index + 1})`);
        assert.strictEqual(fin, esperada.fin, `Error en fin (Fila ${index + 1})`);
        assert.strictEqual(equipo, esperada.equipo, `Error en equipo (Fila ${index + 1})`);
        assert.strictEqual(tarea, esperada.tarea, `Error en tarea (Fila ${index + 1})`);
    });
});

Then('se generaron {int} planificaciones para el equipo {string}', function (cantidadEsperada, codigoEquipo) {
    let procesosRaw = this.responseBody.data ? this.responseBody.data : this.responseBody;
    
    const procesos = Array.isArray(procesosRaw) ? procesosRaw : [procesosRaw];

    let planificacionesReal = [];
    procesos.forEach(proceso => {
        if (proceso && proceso.plannings) {
            planificacionesReal.push(...proceso.plannings);
        }
    });

    const tareasDelEquipo = planificacionesReal.filter(tarea => tarea.equipment.code === codigoEquipo);
    
    const assert = require('assert');
    assert.strictEqual(tareasDelEquipo.length, cantidadEsperada, 
        `Se esperaban ${cantidadEsperada} tareas para ${codigoEquipo}, pero se encontraron ${tareasDelEquipo.length}`);
});
