const assert = require('assert');
const { Given, When } = require('cucumber');

Given('que se ingresa el cliente con {string} y {word}', function (companyName, cuit) {
    this.companyName = companyName;
    this.customerCuit = cuit;
});

Given('que se ingresa el cliente con {string}, {word} y {string}', function (companyName, cuit, observations) {
    this.companyName = companyName;
    this.customerCuit = cuit;
    this.observations = observations;
});

Given('el cliente con {word}', async function (cuit) {
    this.customerCuit = cuit;
    const response = await fetch(`http://backend:8080/customers/cuit/${this.customerCuit}`);
    const dataPackage = await response.json();
    this.customer = dataPackage.data;
});

When('presiono el botón de guardar cliente', async function () {
    try {
        const response = await fetch('http://backend:8080/customers', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                companyName: this.companyName,
                cuit: this.customerCuit,
                observations: this.observations
            })
        });
        

        const textoCrudo = await response.text();
        let mensajeFinal = textoCrudo; 
        
        try {
            const jsonParseado = JSON.parse(textoCrudo);
            if (jsonParseado.message) {
                mensajeFinal = jsonParseado.message;
            }
        } catch (e) {
        }

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
