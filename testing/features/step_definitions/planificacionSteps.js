const assert = require('assert');
const { Given, When, Then } = require('cucumber');

Given('el producto con nombre {string}', async function (productName) {
    const response = await fetch(`http://backend:8080/products/name/${productName}`);
    const dataPackage = await response.json();
    const productFound = dataPackage.data;

    if (!productFound) throw new Error(`No se encontró un producto con el nombre ${productName}`);
    
});

Given('que existe el taller {string}', async function (workshopCode) {
    const response = await fetch(`http://backend:8080/workshops/code/${workshopCode}`);
    const dataPackage = await response.json();
    const workshopFound = dataPackage.data;

    if (!workshopFound) throw new Error(`No se encontró un taller con el código ${workshopCode}`);
});

When('se solicita planificar el producto en el taller el día {string}', async function (date) {
    const response = await fetch('http://backend:8080/planning', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            startDate: date,     
            productId: this.producto.id, 
            workshopId: this.taller.id  
        })
    });
    
    this.lastResponse = response; 
    this.dataPackage = await response.json();
});

Then('se generaron las siguientes planificaciones', function (dataTable) {
    const esperadas = dataTable.hashes(); 
    
    const reales = this.dataPackage.data.planificaciones; 

    assert.strictEqual(reales.length, esperadas.length, "La cantidad de bloques planificados no es la correcta");

    esperadas.forEach((esperada, index) => {
        const real = reales[index];

        assert.strictEqual(real.tarea.nombre, esperada.tarea);
        assert.strictEqual(real.equipo.codigo, esperada.equipo);

        assert.strictEqual(real.periodo.inicio, esperada.inicio);
        assert.strictEqual(real.periodo.fin, esperada.fin);
    });
});