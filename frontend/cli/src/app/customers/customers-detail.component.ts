import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Cliente } from './customer';
import { CommonModule, Location, UpperCasePipe } from '@angular/common';
import { ClienteService } from './cliente.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-customers-create',
  imports: [CommonModule, FormsModule, NgbTypeaheadModule, RouterLink],
  templateUrl: "./customers-detail.html",
  styles: ``
})
export class CustomersDetailComponent {

  cliente!: Cliente;


  constructor(
    private clienteService: ClienteService,
    private location: Location,
    private route: ActivatedRoute,
    private router: Router,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) { }

  goBack(): void {
    this.router.navigate(['/customers']);
  }

  ngOnInit(): void {
    this.get();
  }

  get(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id === 'new' || !id) {
      this.cliente = <Cliente>{ cuit: null as unknown as number, razónSocial: "", observaciones: "" };
    } else {
      this.clienteService.get(id).subscribe(dataPackage => {
        this.cliente = <Cliente>dataPackage.data;
        this.cdr.markForCheck();
      });
    }
  }


  save(): void {
    this.clienteService.save(this.cliente).subscribe(dataPackage => {
      this.cliente = <Cliente>dataPackage.data;

      this.cdr.markForCheck();

      this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
        this.router.navigate(["/customers/", + this.cliente.id]);

        this.toastr.success('¡Cliente guardado con éxito!', 'Éxito');

      });
    })
  }

  formatCuit(value: number | string): string {
    if (!value) return '';

    let s = value.toString().replace(/\D/g, '');

    if (s.length > 2 && s.length <= 10) {
      return `${s.slice(0, 2)}-${s.slice(2)}`;
    } else if (s.length > 10) {
      return `${s.slice(0, 2)}-${s.slice(2, 10)}-${s.slice(10, 11)}`;
    }
    return s;
  }

  onCuitChange(value: string): void {

    const soloNumeros = value.replace(/\D/g, '');

    this.cliente.cuit = Number(soloNumeros.slice(0, 11));

    this.cdr.markForCheck();
  }

}