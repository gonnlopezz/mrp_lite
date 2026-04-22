import { Component } from '@angular/core';
import { EquipmentType } from './equipment-type';
import { CommonModule, Location, UpperCasePipe } from '@angular/common';
import { EquipmentTypeService } from './equipment-type.service';
import { FormsModule } from '@angular/forms';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-equipment-type-create',
  imports: [CommonModule, UpperCasePipe, FormsModule, NgbTypeaheadModule, RouterLink],
  templateUrl: './equipment-type-create.html',
  styles: ``
})
export class EquipmentTypeCreateComponent {
  equipmentType: EquipmentType = { id: null as unknown as number, name: ""  };

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
