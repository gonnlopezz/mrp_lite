import { ChangeDetectorRef, Component } from '@angular/core';
import { CustomerService } from './customer.service';
import { ResultsPage } from '../results-page';
import { PaginationComponent } from '../pagination/pagination.component';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-customers',
  imports: [PaginationComponent, RouterModule, CommonModule],
  templateUrl: './customers.html',
  styles: ``
})
export class CustomersComponent {
  resultsPage: ResultsPage = <ResultsPage>{};
  currentPage: number = 1;

  constructor(
    private customerService: CustomerService,
    private cdr: ChangeDetectorRef
  ) { }

  getCustomers(): void {
    this.customerService.byPage(this.currentPage, 6).subscribe(dataPackage => {
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
    if (confirm("¿Confirma que desea eliminar este cliente?")) {
      this.customerService.delete(id).subscribe(() => {
        this.getCustomers();
      });
    }
  }
}
