import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Producto } from './producto';
import { ProductoService } from './producto.service';
import { Tarea } from './tarea';
import { FormsModule, NgForm } from '@angular/forms';
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
export class ProductosDetailComponent implements OnInit {
  producto!: Producto;
  showTaskForm: boolean = false;
  tiposEquipo!: TipoEquipo[];
  newTask!: Tarea;
  selectedTipoEquipo: TipoEquipo | null = null;

  @ViewChild('productForm') productForm!: NgForm;
  @ViewChild('firstInput') firstInputEl!: ElementRef;

  constructor(
    private productoService: ProductoService,
    private tipoEquipoService: TipoEquipoService,
    private route: ActivatedRoute,
    private location: Location,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: ToastrService
  ) {}

  getProduct(id: string | null): void {
    if(id === 'new' || !id) {
      this.producto = <Producto>{ nombre: "", tareas: <Tarea[]>[] };
    } else {
      this.productoService.get(id).subscribe(dataPackage => {
        this.producto = <Producto>dataPackage.data;
        this.cdr.markForCheck();
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
  recentlyCreated: { item: Producto, time: string }[] = [];

  save(andNew: boolean = false): void {
    this.productoService.save(this.producto).subscribe(dataPackage => {
      const saved = <Producto>dataPackage.data;
      this.toastr.success('Producto guardado con éxito!', 'Éxito');

      if (andNew) {
        this.recentlyCreated.unshift({
          item: saved,
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
        });
        if (this.recentlyCreated.length > 5) {
          this.recentlyCreated.pop();
        }
        if (this.productForm) {
          this.productForm.resetForm();
        }
        this.producto = <Producto>{ nombre: "", tareas: <Tarea[]>[] };
        this.selectedTipoEquipo = null;
        this.showTaskForm = false;

        setTimeout(() => {
          if (this.firstInputEl) {
            this.firstInputEl.nativeElement.focus();
          }
        });

        this.cdr.markForCheck();
      } else {
        this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
          this.router.navigate(["/productos/", + saved.id]);
        });
      }
    });
  }

  deleteRecentlyCreated(id: number): void {
    this.productoService.delete(id).subscribe(() => {
      this.recentlyCreated = this.recentlyCreated.filter(p => p.item.id !== id);
      this.toastr.success('Producto eliminado con éxito!', 'Éxito');
      this.cdr.markForCheck();
    });
  }

  goBack(): void {
    this.router.navigate(['/productos']);
  }

  ngOnInit(): void {
    this.getTiposEquipo();
    this.route.paramMap.subscribe(params => {
      this.getProduct(params.get('id'));
    });
  }
}
