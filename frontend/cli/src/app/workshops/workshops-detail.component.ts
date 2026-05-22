import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Router, RouterLink, RouterModule } from '@angular/router';
import { Workshop } from './workshop';
import { WorkshopService } from './workshop.service';
import { Equipment } from '../equipments/equipment';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { EquipmentType } from '../equipments/equipment-type';
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
  workshop!: Workshop;
  showEquipmentForm: boolean = false;
  equipmentTypes!: EquipmentType[]; 
  newEquipment!: Equipment;
  selectedEquipmentType: EquipmentType | null = null;

  constructor(
    private workshopService: WorkshopService,  
    private equipmentTypeService: EquipmentTypeService, 
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
      this.workshop = <Workshop>{ code: "", name: "", equipments: <Equipment[]>[] };
    } else {
      this.workshopService.get(id).subscribe(dataPackage => {   
        this.workshop = <Workshop>dataPackage.data;
      });       
    }
  }

  getEquipmentTypes(): void {
    this.equipmentTypeService.all().subscribe(dataPackage => {
      this.equipmentTypes = <EquipmentType[]>dataPackage.data;
    }); 
  }

  searchEquipmentTypes = (text$: Observable<string>) =>
    text$.pipe(
      debounceTime(200),
      distinctUntilChanged(),
      switchMap(term =>
        term.length < 1
          ? of([])
          : of(this.equipmentTypes.filter(et =>
            et.name.toLowerCase().includes(term.toLowerCase())
          )).pipe(
            map(results => results.slice(0, 10))
          )
      ),
      catchError(() => of([]))
    );

  equipmentTypeInputFormatter = (type: EquipmentType): string => {
    return type?.name ? type.name : '';
  };

  equipmentTypeResultFormatter = (type: EquipmentType): string => {
    return type.name;
  };

  onEquipmentTypeSelected(type: EquipmentType): void {
    this.selectedEquipmentType = type;
    this.newEquipment.type = type;
    this.cdr.markForCheck();
  }

  openEquipmentForm(): void {
    this.newEquipment = {code: "", capacity: 0, type: { name: ""}} as Equipment;
    this.selectedEquipmentType = null;
    this.showEquipmentForm = true;
  }

  addEquipment () {
    this.workshop.equipments.push(this.newEquipment);
    this.showEquipmentForm = false;
  }

  save(): void {
    this.workshopService.save(this.workshop).subscribe(dataPackage => {
      this.workshop = <Workshop>dataPackage.data;
      this.cdr.markForCheck();

      this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
        this.router.navigate(["/workshops/", + this.workshop.id]);

        this.toastr.success('Taller guardado con éxito!', 'Éxito');

      });
    });
  }

  goBack(): void {
    this.router.navigateByUrl('/workshops', { skipLocationChange: true });
  }

 
}
