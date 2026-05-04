import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { EquipmentTypeService } from './equipment-type.service';
import { ResultsPage } from '../results-page';
import { PaginationComponent } from '../pagination/pagination.component';
import { RouterModule } from '@angular/router';
import { CommonModule, UpperCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { EquipmentType } from './equipment-type';
import { NgbModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmModalComponent } from '../modals/confirm-modal.component';
import { ToastrModule } from 'ngx-toastr';

@Component({
  selector: 'app-equipment-types',
  standalone: true,
  imports: [PaginationComponent, RouterModule, CommonModule, FormsModule, NgbModule, ToastrModule],
  templateUrl: './equipment-types.html',
  styles: ``
})
export class EquipmentTypesComponent implements OnInit {
  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage: number = 1;
  
  // Lógica para el alta rápida
  showCreateForm: boolean = false;
  newEquipmentType: EquipmentType = { name: "" };

  constructor(
    private equipmentTypeService: EquipmentTypeService,
    private cdr: ChangeDetectorRef,
    private modalService: NgbModal
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

  openModal(content: any) {
    this.newEquipmentType = { name: "" }; // Limpiamos antes de abrir
    this.modalService.open(content, { centered: true });
  }

  save(modal: any): void { // 3. Recibimos la instancia del modal para cerrarlo
    if (!this.newEquipmentType.name.trim()) return;

    this.equipmentTypeService.save(this.newEquipmentType).subscribe(dataPackage => {
      this.newEquipmentType = { id: 0, name: "" };
      modal.close();
      this.getEquipmentTypes();
    });
  }

  delete(id: number): void {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
          centered: true,
          backdrop: 'static'
        });
    
    
        modalRef.componentInstance.title = 'Eliminar Tipo de Equipo';
        modalRef.componentInstance.message = `¿Estás seguro de eliminar este tipo de equipo? Esta acción no se puede deshacer.`;
        modalRef.componentInstance.btnOkText = 'Sí, Eliminar';
        modalRef.componentInstance.isDelete = true;
    
        modalRef.result.then((result) => {
          if (result) {
            this.equipmentTypeService.delete(id).subscribe(() => {
              this.getEquipmentTypes();
            });
          }
        }).catch(() => {
        });
    
  }
}