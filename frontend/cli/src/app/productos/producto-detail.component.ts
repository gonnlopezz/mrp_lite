import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Producto } from './producto';
import { ProductoService } from './producto.service';
import { Tarea } from './tarea';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { TipoEquipo } from '../equipos/tipo-equipo';
import { TipoEquipoService } from '../equipos/tipo-equipo.service';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-producto-detail',
  imports: [RouterLink, FormsModule, CommonModule, NgbTypeaheadModule],
  templateUrl: './producto-detail.html',
  styles: ``
})
export class ProductosDetailComponent {
  producto!: Producto;
  showTaskForm: boolean = false;
  tiposEquipo!: TipoEquipo[];
  newTask!: Tarea;
  selectedTipoEquipo: TipoEquipo | null = null;

  constructor(
    private productoService: ProductoService,
    private tipoEquipoService: TipoEquipoService,
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

  getTiposEquipo(): void {
    this.tipoEquipoService.all().subscribe(dataPackage => {
      this.tiposEquipo = <TipoEquipo[]>dataPackage.data;
    });
  }

  searchTiposEquipo = (text$: Observable<string>) =>
    text$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(term =>
        term.length < 1
          ? of([])
          : of(this.tiposEquipo.filter(et =>
            et.nombre.toLowerCase().includes(term.toLowerCase())
          )).pipe(
            map(results => results.slice(0, 10))
          )
      ),
      catchError(() => of([]))
    );

  tipoEquipoInputFormatter = (type: TipoEquipo): string => {
    return type?.nombre ? type.nombre : '';
  };

  tipoEquipoResultFormatter = (type: TipoEquipo): string => {
    return type.nombre;
  };

  onTipoEquipoSelected(type: TipoEquipo): void {
    this.selectedTipoEquipo = type;
    this.newTask.tipo = type;
    this.cdr.markForCheck();
  }

  openTaskForm(): void {
    this.newTask = {nombre: "", tiempo: 0, tipo: { nombre: ""}} as Tarea;
    this.selectedTipoEquipo = null;
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
        this.router.navigate(["/productos/", + this.producto.id]);

        this.toastr.success('Producto guardado con éxito!', 'Éxito');

      });
    });
  }

  goBack(): void {
    this.router.navigate(['/productos']);
  }

  ngOnInit(): void {
    this.getTiposEquipo();
    this.getProduct();
  }
}
