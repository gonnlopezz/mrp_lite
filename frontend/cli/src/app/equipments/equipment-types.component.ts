import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { EquipmentTypeService } from './equipment-type.service';
import { ResultsPage } from '../results-page';
import { PaginationComponent } from '../pagination/pagination.component';
import { RouterModule } from '@angular/router';
import { CommonModule, UpperCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EquipmentType } from './equipment-type';

@Component({
  selector: 'app-equipment-types',
  standalone: true,
  imports: [PaginationComponent, RouterModule, CommonModule, FormsModule, UpperCasePipe],
  templateUrl: './equipment-types.html',
  styles: ``
})
export class EquipmentTypesComponent implements OnInit {
  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage: number = 1;
  
  // Lógica para el alta rápida
  showCreateForm: boolean = false;
  newEquipmentType: EquipmentType = { id: 0, name: "" };

  constructor(
    private equipmentTypeService: EquipmentTypeService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.getEquipmentTypes();
  }

  getEquipmentTypes(): void {
    // Mantenemos el estándar de tu componente Customers (pasando currentPage directo)
    this.equipmentTypeService.byPage(this.currentPage, 6).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

  onPageChangeRequested(page: number): void {
    this.currentPage = page;
    this.getEquipmentTypes();
  }

  toggleCreateForm(): void {
    this.showCreateForm = !this.showCreateForm;
    if (!this.showCreateForm) {
      this.newEquipmentType = { id: 0, name: "" };
    }
  }

  save(): void {
    if (!this.newEquipmentType.name.trim()) return;

    this.equipmentTypeService.save(this.newEquipmentType).subscribe(dataPackage => {
      this.showCreateForm = false; // Cerramos el input
      this.newEquipmentType = { id: 0, name: "" }; // Limpiamos
      this.getEquipmentTypes(); // Recargamos la lista
    });
  }

  delete(id: number): void {
    if (confirm("¿Confirma que desea eliminar este tipo de equipo?")) {
      this.equipmentTypeService.delete(id).subscribe(() => {
        this.getEquipmentTypes();
      });
    }
  }
}