import { Routes } from '@angular/router';
import { CustomersDetailComponent } from './customers/customers-detail.component';
import { HomeComponent } from './home/home.component';
import { CustomersComponent } from './customers/customers.component';
import { EquipmentTypesComponent } from './equipments/equipment-types.component';

export const routes: Routes = [
    {path: "", component: HomeComponent},
    {path: "customers/:id", component: CustomersDetailComponent},
    {path: "talleres", component: EquipmentTypesComponent},
    {path: "customers", component: CustomersComponent}
];
