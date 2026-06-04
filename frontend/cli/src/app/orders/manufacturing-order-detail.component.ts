import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { manufacturingOrder, orderState } from './manufacturingOrder';
import { Customer } from '../customers/customer';
import { Product } from '../products/product';
import { CommonModule, Location } from '@angular/common';
import { OrderService } from './manufacturing-order.service';
import { CustomerService } from '../customers/customer.service';
import { productService } from '../products/product.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, map } from 'rxjs/operators';

@Component({
    selector: 'app-manufacturing-order-detail',
    imports: [CommonModule, FormsModule, NgbTypeaheadModule, RouterLink],
    templateUrl: './manufacturing-order-detail.html',
    styles: ``
})
export class ManufacturingOrderDetailComponent implements OnInit {

    order!: manufacturingOrder;
    customers: Customer[] = [];
    products: Product[] = [];
    selectedCustomer: Customer | null = null;
    selectedProduct: Product | null = null;

    constructor(
        private orderService: OrderService,
        private customerService: CustomerService,
        private productService: productService,
        private route: ActivatedRoute,
        private router: Router,
        private toastr: ToastrService,
        private cdr: ChangeDetectorRef
    ) { }

    goBack(): void {
        this.router.navigate(['/orders']);
    }

    ngOnInit(): void {
        this.getCustomers();
        this.getProducts();
        this.get();
    }

    get(): void {
        const id = this.route.snapshot.paramMap.get('id');

        if (id === 'new' || !id) {
            this.order = <manufacturingOrder>{
                orderDate: new Date(),
                deliveryDate: new Date(),
                quantity: 1,
                state: orderState.PENDIENTE,
                customer: null as unknown as Customer,
                product: null as unknown as Product
            };
        } else {
            this.orderService.get(id).subscribe(dataPackage => {
                this.order = <manufacturingOrder>dataPackage.data;
                this.selectedCustomer = this.order.customer;
                this.selectedProduct = this.order.product;
                this.cdr.markForCheck();
            });
        }
    }

    getCustomers(): void {
        this.customerService.all().subscribe(dataPackage => {
            this.customers = <Customer[]>dataPackage.data;
            this.cdr.markForCheck();
        });
    }

    getProducts(): void {
        this.productService.all().subscribe(dataPackage => {
            this.products = <Product[]>dataPackage.data;
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
                    : of(this.customers.filter(c =>
                        c.companyName.toLowerCase().includes(term.toLowerCase())
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
                    : of(this.products.filter(p =>
                        p.name.toLowerCase().includes(term.toLowerCase())
                    )).pipe(
                        map(results => results.slice(0, 10))
                    )
            ),
            catchError(() => of([]))
        );

    customerInputFormatter = (customer: Customer): string => {
        return customer?.companyName ? customer.companyName : '';
    };

    customerResultFormatter = (customer: Customer): string => {
        return `${customer.companyName} - CUIT: ${this.formatCuit(customer.cuit)}`;
    };

    productInputFormatter = (product: Product): string => {
        return product?.name ? product.name : '';
    };

    productResultFormatter = (product: Product): string => {
        return product.name;
    };

    onCustomerSelected(customer: Customer): void {
        this.selectedCustomer = customer;
        this.order.customer = customer;
        this.cdr.markForCheck();
    }

    onProductSelected(product: Product): void {
        this.selectedProduct = product;
        this.order.product = product;
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
        if (!this.order.customer || !this.order.product) {
            this.toastr.error('Debe seleccionar cliente y producto', 'Error');
            return;
        }

        this.orderService.save(this.order).subscribe(dataPackage => {
            this.order = <manufacturingOrder>dataPackage.data;
            this.cdr.markForCheck();

            this.router.navigateByUrl('/', { skipLocationChange: true }).then(() => {
                this.router.navigate(['/orders', + this.order.id]);
                this.toastr.success('¡Pedido guardado con éxito!', 'Éxito');
            });
        });
    }

}
