import { Component } from '@angular/core';
import { EquipmentType } from './equipment-type';
import { CommonModule, Location, UpperCasePipe } from '@angular/common';
import { EquipmentTypeService } from './equipment-type.service';
import { FormsModule } from '@angular/forms';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-tipo-equipo-create',
  imports: [CommonModule, UpperCasePipe, FormsModule, NgbTypeaheadModule, RouterLink],
  templateUrl: './tipo-equipo-create.html',
  styles: ``
})
export class TipoEquipoCreateComponent {
  equipmentType: EquipmentType = { id: null as unknown as number, nombre: "" };

  constructor(
    private equipmentTypeService: EquipmentTypeService,
    private location: Location,
  ) { }

  goBack(): void {
    this.location.back();
  }

  save(): void {
    this.equipmentTypeService.save(this.equipmentType).subscribe(dataPackage => {
      this.equipmentType = <EquipmentType>dataPackage.data;
      this.goBack();
    });
  }

}
