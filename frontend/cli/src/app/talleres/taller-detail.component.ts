import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';
import { Taller } from './taller';
import { TallerService } from './taller.service';
import { Equipo } from '../equipos/equipo';
import { FormsModule, NgForm } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { TipoEquipo } from '../equipos/tipo-equipo';
import { TipoEquipoService } from '../equipos/tipo-equipo.service';
import { NgbTypeaheadModule, NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { AlertModalComponent } from '../modals/alert-modal.component';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-taller-detail',
  imports: [RouterLink, FormsModule, CommonModule, NgbTypeaheadModule],
  templateUrl: './taller-detail.html',
  styles: ``
})
export class TallerDetailComponent implements OnInit {
  taller!: Taller;
  showEquipoForm: boolean = false;
  tiposEquipo: TipoEquipo[] = [];
  newEquipo!: Equipo;
  selectedTipoEquipo: TipoEquipo | null = null;

  @ViewChild('tallerForm') tallerForm!: NgForm;
  @ViewChild('firstInput') firstInputEl!: ElementRef;

  constructor(
    private tallerService: TallerService,
    private tipoEquipoService: TipoEquipoService, 
    private route: ActivatedRoute,
    private location: Location,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: ToastrService,
    private modalService: NgbModal
  ) {}

 ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.getTaller(params.get('id'));
    });
    this.getTiposEquipo();
  }

  getTaller(id: string | null): void {
    if(id === 'new' || !id) {
      this.taller = <Taller>{ codigo: "", nombre: "", equipos: <Equipo[]>[] };
    } else {
      this.tallerService.get(id).subscribe(dataPackage => {   
        this.taller = <Taller>dataPackage.data;
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
    this.newEquipo.tipo = type;
    this.cdr.markForCheck();
  }

  openEquipoForm(): void {
    this.newEquipo = {codigo: "", capacidad: 0, tipo: { nombre: ""}} as Equipo;
    this.selectedTipoEquipo = null;
    this.showEquipoForm = true;
  }

  addEquipo () {
    this.taller.equipos.push(this.newEquipo);
    this.showEquipoForm = false;
  }

  recentlyCreated: { item: Taller, time: string }[] = [];

  save(andNew: boolean = false): void {
    this.tallerService.save(this.taller).subscribe({
      next: (dataPackage) => {
        const saved = <Taller>dataPackage.data;
        this.toastr.success('Taller guardado con éxito!', 'Éxito');

        if (andNew) {
          this.recentlyCreated.unshift({
            item: saved,
            time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
          });
          if (this.recentlyCreated.length > 5) {
            this.recentlyCreated.pop();
          }
          if (this.tallerForm) {
            this.tallerForm.resetForm();
          }
          this.taller = <Taller>{ codigo: "", nombre: "", equipos: <Equipo[]>[] };
          this.selectedTipoEquipo = null;
          this.showEquipoForm = false;

          setTimeout(() => {
            if (this.firstInputEl) {
              this.firstInputEl.nativeElement.focus();
            }
          });

          this.cdr.markForCheck();
        } else {
          this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
            this.router.navigate(["/talleres/", + saved.id]);
          });
        }
      },
      error: (err) => {
        const errorMsg = err.error?.message || 'Ocurrió un error inesperado al intentar guardar el taller.';
        const alertRef = this.modalService.open(AlertModalComponent, {
          centered: true,
          backdrop: 'static'
        });
        alertRef.componentInstance.title = 'Error al guardar taller';
        alertRef.componentInstance.message = errorMsg;
      }
    });
  }

  deleteRecentlyCreated(id: number): void {
    this.tallerService.delete(id).subscribe(() => {
      this.recentlyCreated = this.recentlyCreated.filter(t => t.item.id !== id);
      this.toastr.success('Taller eliminado con éxito!', 'Éxito');
      this.cdr.markForCheck();
    });
  }

  goBack(): void {
    this.router.navigate(['/talleres']);
  }
}
