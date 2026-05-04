import { Routes } from '@angular/router';
import { CustomersDetailComponent } from './customers/customers-detail.component';
import { HomeComponent } from './home/home.component';
import { CustomersComponent } from './customers/customers.component';
import { EquipmentTypesComponent } from './equipments/equipment-types.component';
import { WorkshopsComponent } from './workshops/workshops.component';
import { WorkshopsDetailComponent } from './workshops/workshops-detail.component';
import { ProductsComponent } from './products/products.component';
import { ProductsDetailComponent } from './products/products-detail.component';

export const routes: Routes = [
    {path: "", component: HomeComponent},
    {path: "customers", component: CustomersComponent},
    {path: "customers/:id", component: CustomersDetailComponent},
    {path: "workshops", component: WorkshopsComponent},
    {path: "workshops/equipment-types", component: EquipmentTypesComponent},
    {path: "workshops/:id", component: WorkshopsDetailComponent},
    {path: "products", component: ProductsComponent},
    {path: "products/:id", component: ProductsDetailComponent},
];
