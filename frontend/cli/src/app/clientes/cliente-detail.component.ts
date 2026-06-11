import { ChangeDetectorRef, Component, OnInit, ElementRef, ViewChild } from '@angular/core';
import { Cliente } from './cliente';
import { CommonModule, Location, UpperCasePipe } from '@angular/common';
import { ClienteService } from './cliente.service';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { NgbTypeaheadModule } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-cliente-detail',
  imports: [CommonModule, FormsModule, NgbTypeaheadModule, RouterLink],
  templateUrl: "./cliente-detail.html",
  styles: ``
})
export class ClientesDetailComponent implements OnInit {

  cliente!: Cliente;

  @ViewChild('customerForm') customerForm!: NgForm;
  @ViewChild('firstInput') firstInputEl!: ElementRef;

  constructor(
    private clienteService: ClienteService,
    private location: Location,
    private route: ActivatedRoute,
    private router: Router,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) { }

  goBack(): void {
    this.router.navigate(['/clientes']);
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.get(params.get('id'));
    });
  }

  get(id: string | null): void {
    if (id === 'new' || !id) {
      this.cliente = <Cliente>{ cuit: null as unknown as number, razonSocial: "", observaciones: "" };
    } else {
      this.clienteService.get(id).subscribe(dataPackage => {
        this.cliente = <Cliente>dataPackage.data;
        this.cdr.markForCheck();
      });
    }
  }


  recentlyCreated: { item: Cliente, time: string }[] = [];

  save(andNew: boolean = false): void {
    this.clienteService.save(this.cliente).subscribe(dataPackage => {
      const saved = <Cliente>dataPackage.data;
      this.toastr.success('¡Cliente guardado con éxito!', 'Éxito');

      if (andNew) {
        this.recentlyCreated.unshift({
          item: saved,
          time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })
        });
        if (this.recentlyCreated.length > 5) {
          this.recentlyCreated.pop();
        }
        if (this.customerForm) {
          this.customerForm.resetForm({
            cuit: "",
            companyName: "",
            observations: ""
          });
        }
        this.cliente = <Cliente>{ cuit: null as unknown as number, razonSocial: "", observaciones: "" };
        
        setTimeout(() => {
          if (this.firstInputEl) {
            this.firstInputEl.nativeElement.focus();
          }
        });

        this.cdr.markForCheck();
      } else {
        this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
          this.router.navigate(["/clientes/", + saved.id]);
        });
      }
    });
  }

  deleteRecentlyCreated(id: number): void {
    this.clienteService.delete(id).subscribe(() => {
      this.recentlyCreated = this.recentlyCreated.filter(c => c.item.id !== id);
      this.toastr.success('¡Cliente eliminado con éxito!', 'Éxito');
      this.cdr.markForCheck();
    });
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