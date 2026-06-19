import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { TipoEquipoService } from './tipo-equipo.service';
import { ResultsPage } from '../results-page';
import { PaginationComponent } from '../pagination/pagination.component';
import { RouterModule } from '@angular/router';
import { CommonModule, UpperCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TipoEquipo } from './tipo-equipo';
import { NgbModal, NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmModalComponent } from '../modals/confirm-modal.component';
import { AlertModalComponent } from '../modals/alert-modal.component';
import { ToastrModule } from 'ngx-toastr';

@Component({
  selector: 'app-tipos-equipo',
  standalone: true,
  imports: [PaginationComponent, RouterModule, CommonModule, FormsModule, NgbModule, ToastrModule],
  templateUrl: './tipos-equipo.html',
  styles: ``
})
export class TiposEquipoComponent implements OnInit {
  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage: number = 1;
  
  showCreateForm: boolean = false;
  newEquipmentType: TipoEquipo = { nombre: "" };

  constructor(
    private tipoEquipoService: TipoEquipoService,
    private cdr: ChangeDetectorRef,
    private modalService: NgbModal
  ) { }

  ngOnInit(): void {
    this.getTiposEquipo();
  }

  getTiposEquipo(): void {
    this.tipoEquipoService.byPage(this.currentPage, 6).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

  onPageChangeRequested(page: number): void {
    this.currentPage = page;
    this.getTiposEquipo();
  }

  openModal(content: any) {
    this.newEquipmentType = { nombre: "" };
    this.modalService.open(content, { centered: true });
  }

  save(modal: any): void {
    if (!this.newEquipmentType.nombre.trim()) return;

    this.tipoEquipoService.save(this.newEquipmentType).subscribe(dataPackage => {
      this.newEquipmentType = { id: 0, nombre: "" };
      modal.close();
      this.getTiposEquipo();
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
            this.tipoEquipoService.delete(id).subscribe({
              next: () => {
                this.getTiposEquipo();
              },
              error: (err) => {
                const errorMsg = err.error?.message || 'Ocurrió un error inesperado al intentar eliminar el tipo de equipo.';
                const alertRef = this.modalService.open(AlertModalComponent, {
                  centered: true,
                  backdrop: 'static'
                });
                alertRef.componentInstance.title = 'Error al eliminar';
                alertRef.componentInstance.message = errorMsg;
              }
            });
          }
        }).catch(() => {
        });
    
  }
}