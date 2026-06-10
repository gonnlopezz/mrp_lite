import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';
import { Taller } from './taller';
import { TallerService } from './taller.service';
import { Equipo } from '../equipos/equipo';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { TipoEquipo } from '../equipos/tipo-equipo';
import { TipoEquipoService } from '../equipos/tipo-equipo.service';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-taller-detail',
  imports: [RouterLink, FormsModule, CommonModule, NgbTypeaheadModule],
  templateUrl: './taller-detail.html',
  styles: ``
})
export class TallerDetailComponent {
  taller!: Taller;
  showEquipoForm: boolean = false;
  tiposEquipo: TipoEquipo[] = [];
  newEquipo!: Equipo;
  selectedTipoEquipo: TipoEquipo | null = null;

  constructor(
    private tallerService: TallerService,
    private tipoEquipoService: TipoEquipoService, 
    private route: ActivatedRoute,
    private location: Location,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: ToastrService
  ) {}

 ngOnInit(): void {
    this.getTaller();
    this.getTiposEquipo();
  }

  getTaller(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if(id === 'new' || !id) {
      this.taller = <Taller>{ codigo: "", nombre: "", equipos: <Equipo[]>[] };
    } else {
      this.tallerService.get(id).subscribe(dataPackage => {   
        this.taller = <Taller>dataPackage.data;
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

  save(): void {
    this.tallerService.save(this.taller).subscribe(dataPackage => {
      this.taller = <Taller>dataPackage.data;
      this.cdr.markForCheck();

      this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
        this.router.navigate(["/talleres/", + this.taller.id]);

        this.toastr.success('Taller guardado con éxito!', 'Éxito');

      });
    });
  }

  goBack(): void {
    this.router.navigate(['/talleres']);
  }

 
}
