import { Routes } from '@angular/router';
import { CustomersCreateComponent } from './customers/customers-create.component';
import { HomeComponent } from './home/home.component';

export const routes: Routes = [
    {path: "", component: HomeComponent},
    {path: "clientes/new", component: CustomersCreateComponent}
];
