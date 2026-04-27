import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { Workshop } from './workshop';
import { WorkshopService } from './workshop.service';
import { Equipment } from '../equipments/equipment';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { EquipmentType } from '../equipments/equipment-type';
import { EquipmentTypeService } from '../equipments/equipment-type.service';

@Component({
  selector: 'app-workshop-detail',
  imports: [RouterModule, FormsModule, CommonModule],
  templateUrl: './workshops-detail.html'  ,
  styles: ``
})
export class WorkshopsDetailComponent {
  workshop!: Workshop;
  showEquipmentForm: boolean = false;
  equipmentTypes!: EquipmentType[]; 

  constructor(
    private workshopService: WorkshopService,  
    private equipmentTypeService: EquipmentTypeService, 
    private route: ActivatedRoute,
    private location: Location,
    private cdr: ChangeDetectorRef,
    private toastr: ToastrService
  ) {}

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

  addEquipment () {
    this.showEquipmentForm = false;
  }

  save(): void {
    this.workshopService.save(this.workshop).subscribe(dataPackage => {
      this.workshop = <Workshop>dataPackage.data;
      this.cdr.markForCheck();
      this.toastr.success('¡Taller guardado con éxito!', 'Éxito');  
    });
  }



  goBack(): void {
    this.location.back();
  }

  ngOnInit(): void {
    this.getWorkshops();
    this.getEquipmentTypes();
  }
}
