import { Routes } from '@angular/router';
import { CustomersDetailComponent } from './customers/customers-detail.component';
import { HomeComponent } from './home/home.component';
import { EquipmentTypeCreateComponent } from './equipments/equipment-type-create.component';
import { CustomersComponent } from './customers/customers.component';

export const routes: Routes = [
    {path: "", component: HomeComponent},
    {path: "customers/:id", component: CustomersDetailComponent},
    {path: "talleres/new-equipment-type", component: EquipmentTypeCreateComponent},
    {path: "customers", component: CustomersComponent}
];
