import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Customer } from './customer';
import { CommonModule, Location, UpperCasePipe } from '@angular/common';
import { CustomerService } from './customer.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-customers-create',
  imports: [CommonModule, UpperCasePipe, FormsModule, NgbTypeaheadModule, RouterLink],
  templateUrl: "./customers-create.html",
  styles: ``
})
export class CustomersCreateComponent {
  
  customer: Customer = <Customer>{ cuit: null as unknown as number, razonSocial: "", observaciones: "" };
  

  constructor(
    private customerService: CustomerService,
    private location: Location,
  ) {}

  goBack(): void {
    this.location.back();
  }

  save(): void {
    this.customerService.save(this.customer).subscribe(dataPackage => {
      this.customer = <Customer>dataPackage.data;
      this.goBack();
    });
  }

}