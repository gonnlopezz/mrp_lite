import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Producto } from './product';
import { productService } from './product.service';
import { Tarea } from './task';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { TipoEquipo } from '../equipments/equipment-type';
import { EquipmentTypeService } from '../equipments/equipment-type.service';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-products-detail',
  imports: [RouterLink, FormsModule, CommonModule, NgbTypeaheadModule],
  templateUrl: './products-detail.html',
  styles: ``
})
export class ProductsDetailComponent {
  product!: Producto;
  showTaskForm: boolean = false;
  equipmentTypes!: TipoEquipo[];
  newTask!: Tarea;
  selectedEquipmentType: TipoEquipo | null = null;

  constructor(
    private productService: productService,
    private equipmentTypeService: TipoEquipoService,
    private route: ActivatedRoute,
    private location: Location,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: ToastrService
  ) {}

  getProduct(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if(id === 'new' || !id) {
      this.producto = <Producto>{ nombre: "", tareas: <Tarea[]>[] };
    } else {
      this.productoService.get(id).subscribe(dataPackage => {
        this.producto = <Producto>dataPackage.data;
      });
    }
  }

  getEquipmentTypes(): void {
    this.equipoTypeService.all().subscribe(dataPackage => {
      this.equipoTypes = <TipoEquipo[]>dataPackage.data;
    });
  }

  searchEquipmentTypes = (text$: Observable<string>) =>
    text$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(term =>
        term.length < 1
          ? of([])
          : of(this.equipoTypes.filter(et =>
            et.nombre.toLowerCase().includes(term.toLowerCase())
          )).pipe(
            map(results => results.slice(0, 10))
          )
      ),
      catchError(() => of([]))
    );

  equipmentTypeInputFormatter = (type: TipoEquipo): string => {
    return type?.name ? type.nombre : '';
  };

  equipmentTypeResultFormatter = (type: TipoEquipo): string => {
    return type.nombre;
  };

  onEquipmentTypeSelected(type: TipoEquipo): void {
    this.selectedEquipmentType = type;
    this.newTask.type = type;
    this.cdr.markForCheck();
  }

  openTaskForm(): void {
    this.newTask = {nombre: "", duration: 0, type: { nombre: ""}} as Task;
    this.selectedEquipmentType = null;
    this.showTaskForm = true;
  }

  addTask(): void {
    this.producto.tareas.push(this.newTask);
    this.showTaskForm = false;
  }

  save(): void {
    this.productoService.save(this.producto).subscribe(dataPackage => {
      this.producto = <Producto>dataPackage.data;
      this.cdr.markForCheck();

      this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
        this.router.navigate(["/products/", + this.producto.id]);

        this.toastr.success('Producto guardado con éxito!', 'Éxito');

      });
    });
  }

  goBack(): void {
    this.router.navigate(['/products']);
  }

  ngOnInit(): void {
    this.getEquipmentTypes();
    this.getProduct();
  }
}
