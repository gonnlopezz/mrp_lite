import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Taller } from './workshop';
import { WorkshopService } from './workshop.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmModalComponent } from '../modals/confirm-modal.component';
import { ResultsPage } from '../results-page';
import { PaginationComponent } from '../pagination/pagination.component';

@Component({
  selector: 'app-workshops',
  imports: [RouterModule, CommonModule, FormsModule, PaginationComponent],
  templateUrl: './workshops.html',
  styles: ``
})
export class WorkshopsComponent {
  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage: number = 1;
  openedWorkshops: Set<number> = new Set();
  searchTerm: string = '';

  constructor(
    private workshopService: TallerService,
    private cdr: ChangeDetectorRef,
    private modalService: NgbModal) { }

  getWorkshops(): void {
    this.workshopService.byPage(this.currentPage, 6).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) {
      this.currentPage = 1;
      this.getWorkshops();
      return;
    }

    this.currentPage = 1;
    this.workshopService.search(this.searchTerm, this.currentPage, 6).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
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
        this.workshopService.delete(id).subscribe(() => {
          this.getWorkshops();
        });
      }
    }).catch(() => {
    });
  }

  ngOnInit(): void {
    this.getWorkshops();
  }

  onPageChangeRequested(page: number): void {
    this.currentPage = page;
    this.getWorkshops();
  }

  togglePerformances(id: number): void {
    if (this.openedWorkshops.has(id)) {
      this.openedWorkshops.delete(id);
    } else {
      this.openedWorkshops.add(id);
    }
  }

  isOpen(id: number): boolean {
    return this.openedWorkshops.has(id);
  }


  
}
