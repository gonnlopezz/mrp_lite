import { Routes } from '@angular/router';
import { CustomersCreateComponent } from './customers/customers-create.component';
import { HomeComponent } from './home/home.component';
import { TipoEquipoCreateComponent } from './equipments/tipo-equipo-create.component';

export const routes: Routes = [
    {path: "", component: HomeComponent},
    {path: "clientes/nuevo", component: CustomersCreateComponent},
    {path: "talleres/nuevo-tipo-equipo", component: TipoEquipoCreateComponent},
];
