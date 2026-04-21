import { Component } from '@angular/core';
import { RouterModule, RouterOutlet } from '@angular/router';
import { NgbDropdownModule } from '@ng-bootstrap/ng-bootstrap'; 
@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NgbDropdownModule, RouterModule],
  templateUrl: './app.html',
  styles: [],
})
export class AppComponent {
  title = 'MRP Lite';
}
