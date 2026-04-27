import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Workshop } from './workshop';
import { WorkshopService } from './workshop.service';
import { CommonModule } from '@angular/common';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ConfirmModalComponent } from '../modals/confirm-modal.component';

@Component({
  selector: 'app-workshops',
  imports: [RouterModule, CommonModule],
  templateUrl: './workshops.html',
  styles: ``
})
export class WorkshopsComponent {
  workshops: Workshop[] = [];

  constructor(
    private workshopService: WorkshopService,
    private cdr: ChangeDetectorRef,
    private modalService: NgbModal) { }

  get(): void {
    this.workshopService.all().subscribe(dataPackage => {
      this.workshops = <Workshop[]>dataPackage.data;
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
          this.get();
        });
      }
    }).catch(() => {
    });
  }

  ngOnInit(): void {
    this.get();
  }
}
