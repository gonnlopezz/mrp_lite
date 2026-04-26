import { ChangeDetectorRef, Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { Workshop } from './workshop';
import { WorkshopService } from './workshop.service';
import { CommonModule } from '@angular/common';

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
    private cdr: ChangeDetectorRef) { }

  get(): void {
    this.workshopService.all().subscribe(dataPackage => {
      this.workshops = <Workshop[]>dataPackage.data;
      this.cdr.markForCheck();
    });
  }

  ngOnInit(): void {
    this.get();
  }
}
