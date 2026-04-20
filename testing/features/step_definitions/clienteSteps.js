
const assert = require('assert');
const { Given, When, Then } = require('@cucumber/cucumber');

Given("que se ingresa el cliente con <razonSocial>, <cuit> y <observaciones>");

When("se presiona el botón de guardar");

Then("se espera el siguiente <status> con la <respuesta>");