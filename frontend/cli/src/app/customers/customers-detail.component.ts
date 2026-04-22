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
  templateUrl: "./customers-detail.html",
  styles: ``
})
export class CustomersDetailComponent {

  customer!: Customer;


  constructor(
    private customerService: CustomerService,
    private location: Location,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef  
  ) { }

  goBack(): void {
    this.location.back();
  }

  ngOnInit(): void {
    this.get();
  }

  get(): void {
    const id = this.route.snapshot.paramMap.get('id');
    
    if (id === 'new' || !id) {
      this.customer = <Customer>{ cuit: null as unknown as number, companyName: "", observations: "" };
    } else {
      this.customerService.get(id).subscribe(dataPackage => {
        this.customer = <Customer>dataPackage.data;
        this.cdr.markForCheck();
      });
    }
  }

  save(): void {
    this.customerService.save(this.customer).subscribe(dataPackage => {
      this.customer = <Customer>dataPackage.data;
      
      this.cdr.markForCheck();
      
      alert('¡Cliente guardado con éxito!'); 
    
    });
  }

  

}