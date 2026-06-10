import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { PedidoFabricacion, EstadoPedido } from './pedido';
import { Cliente } from '../clientes/cliente';
import { Producto } from '../productos/producto';
import { CommonModule, Location } from '@angular/common';
import { PedidoService } from './pedido.service';
import { ClienteService } from '../clientes/cliente.service';
import { ProductoService } from '../productos/producto.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, map } from 'rxjs/operators';

@Component({
    selector: 'app-pedido-detail',
    imports: [CommonModule, FormsModule, NgbTypeaheadModule, RouterLink],
    templateUrl: './pedido-detail.html',
    styles: ``
})
export class PedidoDetailComponent implements OnInit {

    pedido!: PedidoFabricacion;
    clientes: Cliente[] = [];
    productos: Producto[] = [];
    selectedCustomer: Cliente | null = null;
    selectedProduct: Producto | null = null;

    constructor(
        private pedidoService: PedidoService,
        private clienteService: ClienteService,
        private productoService: ProductoService,
        private route: ActivatedRoute,
        private router: Router,
        private toastr: ToastrService,
        private cdr: ChangeDetectorRef
    ) { }

    goBack(): void {
        this.router.navigate(['/pedidos']);
    }

    ngOnInit(): void {
        this.getClientes();
        this.getProductos();
        this.get();
    }

    get(): void {
        const id = this.route.snapshot.paramMap.get('id');

        if (id === 'new' || !id) {
            const todayStr = new Date().toISOString().split('T')[0];

            this.pedido = <PedidoFabricacion>{
                fechaPedido: todayStr,      
                fechaEntrega: todayStr,    
                cantidad: 1,
                estado: EstadoPedido.PENDIENTE,
                cliente: null as unknown as Cliente,
                producto: null as unknown as Producto
            };
        } else {
            this.pedidoService.get(id).subscribe(dataPackage => {
                this.pedido = <PedidoFabricacion>dataPackage.data;
                this.selectedCustomer = this.pedido.cliente;
                this.selectedProduct = this.pedido.producto;
                this.cdr.markForCheck();
            });
        }
    }

    getClientes(): void {
        this.clienteService.all().subscribe(dataPackage => {
            this.clientes = <Cliente[]>dataPackage.data;
            this.cdr.markForCheck();
        });
    }

    getProductos(): void {
        this.productoService.all().subscribe(dataPackage => {
            this.productos = <Producto[]>dataPackage.data;
            this.cdr.markForCheck();
        });
    }

    searchCustomers = (text$: Observable<string>) =>
        text$.pipe(
            debounceTime(200),
            distinctUntilChanged(),
            switchMap(term =>
                term.length < 1
                    ? of([])
                    : of(this.clientes.filter(c =>
                        c.razonSocial.toLowerCase().includes(term.toLowerCase())
                    )).pipe(
                        map(results => results.slice(0, 10))
                    )
            ),
            catchError(() => of([]))
        );

    searchProducts = (text$: Observable<string>) =>
        text$.pipe(
            debounceTime(200),
            distinctUntilChanged(),
            switchMap(term =>
                term.length < 1
                    ? of([])
                    : of(this.productos.filter(p =>
                        p.nombre.toLowerCase().includes(term.toLowerCase())
                    )).pipe(
                        map(results => results.slice(0, 10))
                    )
            ),
            catchError(() => of([]))
        );

    customerInputFormatter = (customer: Cliente): string => {
        return customer?.razonSocial ? customer.razonSocial : '';
    };

    customerResultFormatter = (customer: Cliente): string => {
        return `${customer.razonSocial} - CUIT: ${this.formatCuit(customer.cuit)}`;
    };

    productInputFormatter = (product: Producto): string => {
        return product?.nombre ? product.nombre : '';
    };

    productResultFormatter = (product: Producto): string => {
        return product.nombre;
    };

    onCustomerSelected(customer: Cliente): void {
        this.selectedCustomer = customer;
        this.pedido.cliente = customer;
        this.cdr.markForCheck();
    }

    onProductSelected(product: Producto): void {
        this.selectedProduct = product;
        this.pedido.producto = product;
        this.cdr.markForCheck();
    }

    formatCuit(value: number | string): string {
        if (!value) return '';

        let s = value.toString().replace(/\D/g, '');

        if (s.length > 2 && s.length <= 10) {
            return `${s.slice(0, 2)}-${s.slice(2)}`;
        } else if (s.length > 10) {
            return `${s.slice(0, 2)}-${s.slice(2, 10)}-${s.slice(10, 11)}`;
        }
        return s;
    }

    formatDate(date: Date | string): string {
        if (!date) return '';
        const d = new Date(date);
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        const year = d.getFullYear();
        return `${year}-${month}-${day}`;
    }

    save(): void {
        if (!this.pedido.cliente || !this.pedido.producto) {
            this.toastr.error('Debe seleccionar cliente y producto', 'Error');
            return;
        }

        this.pedidoService.save(this.pedido).subscribe(dataPackage => {
            this.pedido = <PedidoFabricacion>dataPackage.data;
            this.cdr.markForCheck();

            this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
                this.router.navigate(['/pedidos', + this.pedido.id]);
                this.toastr.success('¡Pedido guardado con éxito!', 'Éxito');
            });
        });
    }

}
