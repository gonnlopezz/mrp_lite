import { ChangeDetectorRef, Component } from '@angular/core';
import { ClienteService } from './cliente.service';
import { ResultsPage } from '../results-page';
import { PaginationComponent } from '../pagination/pagination.component';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmModalComponent } from '../modals/confirm-modal.component';
import { Cliente } from './customer';

@Component({
  selector: 'app-customers',
  imports: [PaginationComponent, RouterModule, CommonModule, FormsModule],
  templateUrl: './customers.html',
  styles: ``
})
export class CustomersComponent {
  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage: number = 1;
  searchTerm: string = '';

  constructor(
    private clienteService: ClienteService,
    private modalService: NgbModal,
    private cdr: ChangeDetectorRef
  ) { }

  getCustomers(): void {
    this.clienteService.byPage(this.currentPage, 10).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

  onSearch(): void {
    if (!this.searchTerm.trim()) {
      this.currentPage = 1;
      this.getCustomers();
      return;
    }

    this.currentPage = 1;
    this.clienteService.search(this.searchTerm, this.currentPage, 10).subscribe(dataPackage => {
      this.resultsPage = <ResultsPage>dataPackage.data;
      this.cdr.markForCheck();
    });
  }


  ngOnInit(): void {
    this.getCustomers();
  }

  onPageChangeRequested(page: number): void {
    this.currentPage = page;
    this.getCustomers();
  }

  delete(id: number): void {
    const modalRef = this.modalService.open(ConfirmModalComponent, {
      centered: true,
      backdrop: 'static'
    });


    modalRef.componentInstance.title = 'Eliminar Cliente';
    modalRef.componentInstance.message = `¿Estás seguro de eliminar a este cliente? Esta acción no se puede deshacer.`;
    modalRef.componentInstance.btnOkText = 'Sí, Eliminar';
    modalRef.componentInstance.isDelete = true;

    modalRef.result.then((result) => {
      if (result) {
        this.clienteService.delete(id).subscribe(() => {
          this.getCustomers();
        });
      }
    }).catch(() => {
    });


  }
}
