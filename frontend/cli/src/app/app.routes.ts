import { Routes } from '@angular/router';
import { ClientesDetailComponent } from './clientes/cliente-detail.component';
import { HomeComponent } from './home/home.component';
import { ClientesComponent } from './clientes/clientes.component';
import { TiposEquipoComponent } from './equipos/tipos-equipo.component';
import { TalleresComponent } from './talleres/talleres.component';
import { TallerDetailComponent } from './talleres/taller-detail.component';
import { ProductosComponent } from './productos/productos.component';
import { ProductosDetailComponent } from './productos/producto-detail.component';
import { PlanificacionesComponent } from './planificacion/planificaciones.component';
import { PedidosComponent } from './pedidos/pedidos.component';
import { PedidoDetailComponent } from './pedidos/pedido-detail.component';
import { PlanificacionDashboardComponent } from './planificacion/planificacion-dashboard.component';

export const routes: Routes = [
    {path: "", component: HomeComponent},
    {path: "clientes", component: ClientesComponent},
    {path: "clientes/:id", component: ClientesDetailComponent},
    {path: "talleres", component: TalleresComponent},
    {path: "talleres/tipos-equipo", component: TiposEquipoComponent},
    {path: "talleres/:id", component: TallerDetailComponent},
    {path: "productos", component: ProductosComponent},
    {path: "productos/:id", component: ProductosDetailComponent},
    {path: "talleres/:id/planificaciones", component: PlanificacionesComponent},
    {path: "pedidos", component: PedidosComponent},
    {path: "pedidos/:id", component: PedidoDetailComponent},
    {path: "pedidos/:id/planificaciones", component: PlanificacionDashboardComponent},
    {path: "planificacion-dashboard", component: PlanificacionDashboardComponent}
];
