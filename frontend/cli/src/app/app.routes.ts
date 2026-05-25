import { Routes } from '@angular/router';
import { CustomersDetailComponent } from './customers/customers-detail.component';
import { HomeComponent } from './home/home.component';
import { CustomersComponent } from './customers/customers.component';
import { EquipmentTypesComponent } from './equipments/equipment-types.component';
import { WorkshopsComponent } from './workshops/workshops.component';
import { WorkshopsDetailComponent } from './workshops/workshops-detail.component';
import { ProductsComponent } from './products/products.component';
import { ProductsDetailComponent } from './products/products-detail.component';
import { PlanningComponent } from './planning/plannings.component';
import { ManufacturingOrdersComponent } from './orders/manufacturing-orders.component';
import { ManufacturingOrderDetailComponent } from './orders/manufacturing-order-detail.component';
import { PlanningDashboardComponent } from './planning/planning-dashboard.component';

export const routes: Routes = [
    {path: "", component: HomeComponent},
    {path: "customers", component: CustomersComponent},
    {path: "customers/:id", component: CustomersDetailComponent},
    {path: "workshops", component: WorkshopsComponent},
    {path: "workshops/equipment-types", component: EquipmentTypesComponent},
    {path: "workshops/:id", component: WorkshopsDetailComponent},
    {path: "products", component: ProductsComponent},
    {path: "products/:id", component: ProductsDetailComponent},
    {path: "workshops/:id/plannings", component: PlanningComponent},
    {path: "orders", component: ManufacturingOrdersComponent},
    {path: "orders/:id", component: ManufacturingOrderDetailComponent},
    {path: "orders/:id/plannings", component: PlanningComponent},
    {path: "planning-dashboard", component: PlanningDashboardComponent}
];
