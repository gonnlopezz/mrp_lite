import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';
import { Taller } from './workshop';
import { WorkshopService } from './workshop.service';
import { Equipo } from '../equipments/equipment';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { TipoEquipo } from '../equipments/equipment-type';
import { EquipmentTypeService } from '../equipments/equipment-type.service';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, catchError, map } from 'rxjs/operators';

@Component({
  selector: 'app-workshop-detail',
  imports: [RouterLink, FormsModule, CommonModule, NgbTypeaheadModule],
  templateUrl: './workshops-detail.html'  ,
  styles: ``
})
export class WorkshopsDetailComponent {
  workshop!: Taller;
  showEquipmentForm: boolean = false;
  equipmentTypes!: TipoEquipo[]; 
  newEquipment!: Equipo;
  selectedEquipmentType: TipoEquipo | null = null;

  constructor(
    private workshopService: TallerService,  
    private equipmentTypeService: TipoEquipoService, 
    private route: ActivatedRoute,
    private location: Location,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: ToastrService
  ) {}

 ngOnInit(): void {
    this.getWorkshops();
    this.getEquipmentTypes();
  }

  getWorkshops(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if(id === 'new' || !id) {
      this.workshop = <Taller>{ código: "", nombre: "", equipos: <Equipo[]>[] };
    } else {
      this.workshopService.get(id).subscribe(dataPackage => {   
        this.workshop = <Taller>dataPackage.data;
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
    this.newEquipment.type = type;
    this.cdr.markForCheck();
  }

  openEquipmentForm(): void {
    this.newEquipment = {código: "", capacity: 0, type: { nombre: ""}} as Equipment;
    this.selectedEquipmentType = null;
    this.showEquipmentForm = true;
  }

  addEquipment () {
    this.workshop.equipos.push(this.newEquipment);
    this.showEquipmentForm = false;
  }

  save(): void {
    this.workshopService.save(this.workshop).subscribe(dataPackage => {
      this.workshop = <Taller>dataPackage.data;
      this.cdr.markForCheck();

      this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
        this.router.navigate(["/workshops/", + this.workshop.id]);

        this.toastr.success('Taller guardado con éxito!', 'Éxito');

      });
    });
  }

  goBack(): void {
    this.router.navigate(['/workshops']);
  }

 
}
