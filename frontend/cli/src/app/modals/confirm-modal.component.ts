import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  templateUrl: './confirm-modal.html'
})
export class ConfirmModalComponent {
  @Input() title: string = 'Confirmar Acción';
  @Input() message: string = '¿Estás seguro de realizar esta operación?';
  @Input() btnOkText: string = 'Confirmar';
  @Input() btnCancelText: string = 'Cancelar';
  @Input() isDelete: boolean = false;

  constructor(public activeModal: NgbActiveModal) {}
}