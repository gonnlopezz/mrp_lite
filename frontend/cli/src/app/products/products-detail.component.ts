import { CommonModule, Location } from '@angular/common';
import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Product } from './product';
import { productService } from './product.service';
import { Task } from './task';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { EquipmentType } from '../equipments/equipment-type';
import { EquipmentTypeService } from '../equipments/equipment-type.service';

@Component({
  selector: 'app-products-detail',
  imports: [RouterLink, FormsModule, CommonModule],
  templateUrl: './products-detail.html',
  styles: ``
})
export class ProductsDetailComponent {
  product!: Product;
  showTaskForm: boolean = false;
  equipmentTypes!: EquipmentType[];
  newTask!: Task;

  constructor(
    private productService: productService,
    private equipmentTypeService: EquipmentTypeService,
    private route: ActivatedRoute,
    private location: Location,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private toastr: ToastrService
  ) {}

  getProduct(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if(id === 'new' || !id) {
      this.product = <Product>{ name: "", tasks: <Task[]>[] };
    } else {
      this.productService.get(id).subscribe(dataPackage => {
        this.product = <Product>dataPackage.data;
      });
    }
  }

  getEquipmentTypes(): void {
    this.equipmentTypeService.all().subscribe(dataPackage => {
      this.equipmentTypes = <EquipmentType[]>dataPackage.data;
    });
  }

  openTaskForm(): void {
    this.newTask = {name: "", duration: 0, type: { name: ""}} as Task;
    this.showTaskForm = true;
  }

  addTask(): void {
    this.product.tasks.push(this.newTask);
    this.showTaskForm = false;
  }

  save(): void {
    this.productService.save(this.product).subscribe(dataPackage => {
      this.product = <Product>dataPackage.data;
      this.cdr.markForCheck();

      this.router.navigateByUrl("/", { skipLocationChange: true }).then(() => {
        this.router.navigate(["/products/", + this.product.id]);

        this.toastr.success('Producto guardado con éxito!', 'Éxito');

      });
    });
  }

  goBack(): void {
    this.location.back();
  }

  ngOnInit(): void {
    this.getEquipmentTypes();
    this.getProduct();
  }
}
