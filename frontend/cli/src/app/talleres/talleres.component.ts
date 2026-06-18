import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Taller } from './taller';
import { TallerService } from './taller.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmModalComponent } from '../modals/confirm-modal.component';
import { AlertModalComponent } from '../modals/alert-modal.component';
import { ResultsPage } from '../results-page';
import { PaginationComponent } from '../pagination/pagination.component';

@Component({
  selector: 'app-talleres',
  imports: [RouterModule, CommonModule, FormsModule, PaginationComponent],
  templateUrl: './talleres.html',
  styles: ``
})
export class TalleresComponent {
  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage: number = 1;
  openedTalleres: Set<number> = new Set();
  searchTerm: string = '';

  constructor(
    private tallerService: TallerService,
    private cdr: ChangeDetectorRef,
    private modalService: NgbModal) { }

  getTalleres(): void {
    if (this.searchTerm && this.searchTerm.trim() !== '') {
      this.tallerService.search(this.searchTerm, this.currentPage, 6).subscribe(dataPackage => {
        this.resultsPage = <ResultsPage>dataPackage.data;
        this.cdr.markForCheck();
      });
    } else {
      this.tallerService.byPage(this.currentPage, 6).subscribe(dataPackage => {
        this.resultsPage = <ResultsPage>dataPackage.data;
        this.cdr.markForCheck();
      });
    }
  }

  onSearch(): void {
    this.currentPage = 1;
    this.getTalleres();
  }

  delete(id: number): void {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      centered: true,
      backdrop: 'static'
    });

    modalRef.componentInstance.title = 'Eliminar Taller';
    modalRef.componentInstance.message = `¿Estás seguro de eliminar este taller? Esta acción no se puede deshacer.`;
    modalRef.componentInstance.btnOkText = 'Sí, Eliminar';
    modalRef.componentInstance.isDelete = true;

    modalRef.result.then((result) => {
      if (result) {
        this.tallerService.delete(id).subscribe({
          next: () => {
            this.getTalleres();
          },
          error: (err) => {
            const errorMsg = err.error?.message || 'Ocurrió un error inesperado al intentar eliminar el taller.';
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

  ngOnInit(): void {
    this.getTalleres();
  }

  onPageChangeRequested(page: number): void {
    this.currentPage = page;
    this.getTalleres();
  }

  togglePerformances(id: number): void {
    if (this.openedTalleres.has(id)) {
      this.openedTalleres.delete(id);
    } else {
      this.openedTalleres.add(id);
    }
  }

  isOpen(id: number): boolean {
    return this.openedTalleres.has(id);
  }


  
}
