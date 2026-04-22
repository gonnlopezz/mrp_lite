import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [CommonModule, RouterModule],
  templateUrl: "./home.html",
  styles: ``
})
export class HomeComponent {
  today: Date = new Date();
}
