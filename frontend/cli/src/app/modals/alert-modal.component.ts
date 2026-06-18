import { Component, Input } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-alert-modal',
  standalone: true,
  templateUrl: './alert-modal.html'
})
export class AlertModalComponent {
  @Input() title: string = 'Atención';
  @Input() message: string = 'Ocurrió un error al procesar la solicitud.';

  constructor(public activeModal: NgbActiveModal) {}
}
