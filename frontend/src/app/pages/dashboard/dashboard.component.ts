import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { LocationService } from '../../services/location.service';
import {
  CATEGORY_METADATA,
  CategoryMeta,
  CreateLocationRequest,
  LocationCategory,
  LocationItem,
  UpdateLocationRequest
} from '../../models/location.model';

import { NzCardModule } from 'ng-zorro-antd/card';
import { NzGridModule } from 'ng-zorro-antd/grid';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzAvatarModule } from 'ng-zorro-antd/avatar';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzIconModule } from 'ng-zorro-antd/icon';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzDatePickerModule } from 'ng-zorro-antd/date-picker';
import { NzDrawerModule } from 'ng-zorro-antd/drawer';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzEmptyModule } from 'ng-zorro-antd/empty';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTooltipModule } from 'ng-zorro-antd/tooltip';
import { NzMessageService } from 'ng-zorro-antd/message';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    ReactiveFormsModule,
    NzCardModule,
    NzGridModule,
    NzButtonModule,
    NzAvatarModule,
    NzTagModule,
    NzIconModule,
    NzInputModule,
    NzSelectModule,
    NzDatePickerModule,
    NzDrawerModule,
    NzFormModule,
    NzPopconfirmModule,
    NzEmptyModule,
    NzSpinModule,
    NzTableModule,
    NzTooltipModule
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  readonly authService = inject(AuthService);
  readonly locationService = inject(LocationService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly message = inject(NzMessageService);

  readonly categoryMeta = CATEGORY_METADATA;
  readonly categories: LocationCategory[] = [
    'HISTORICAL',
    'NATURE',
    'BEACH',
    'CITY_LIFE',
    'FOOD_DINING',
    'ADVENTURE',
    'CULTURE',
    'OTHER'
  ];

  // UI state signals
  readonly searchQuery = signal<string>('');
  readonly selectedCategory = signal<string>('ALL');
  readonly viewMode = signal<'grid' | 'table'>('grid');
  readonly isDrawerVisible = signal<boolean>(false);
  readonly editingLocationId = signal<number | null>(null);
  readonly isSubmitting = signal<boolean>(false);

  // Form for Add/Edit
  locationForm: FormGroup = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(120)]],
    city: ['', [Validators.required, Validators.maxLength(100)]],
    country: ['', [Validators.required, Validators.maxLength(100)]],
    category: ['HISTORICAL' as LocationCategory, [Validators.required]],
    latitude: [null, [Validators.min(-90), Validators.max(90)]],
    longitude: [null, [Validators.min(-180), Validators.max(180)]],
    description: ['', [Validators.maxLength(2000)]],
    visitedAt: [null]
  });

  // Filtered locations derived reactively
  readonly filteredLocations = computed(() => {
    const list = this.locationService.locations();
    const query = this.searchQuery().toLowerCase().trim();
    const category = this.selectedCategory();

    return list.filter((item) => {
      const matchesCategory = category === 'ALL' || item.category === category;
      const matchesQuery =
        !query ||
        item.title.toLowerCase().includes(query) ||
        item.city.toLowerCase().includes(query) ||
        item.country.toLowerCase().includes(query) ||
        (item.description && item.description.toLowerCase().includes(query));

      return matchesCategory && matchesQuery;
    });
  });

  ngOnInit(): void {
    this.locationService.loadLocations().subscribe();
    this.locationService.loadStats().subscribe();
  }

  onSearchChange(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchQuery.set(target.value);
  }

  setCategoryFilter(category: string): void {
    this.selectedCategory.set(category);
  }

  setViewMode(mode: 'grid' | 'table'): void {
    this.viewMode.set(mode);
  }

  openCreateDrawer(): void {
    this.editingLocationId.set(null);
    this.locationForm.reset({
      title: '',
      city: '',
      country: '',
      category: 'HISTORICAL',
      latitude: null,
      longitude: null,
      description: '',
      visitedAt: null
    });
    this.isDrawerVisible.set(true);
  }

  openEditDrawer(location: LocationItem): void {
    this.editingLocationId.set(location.id);
    this.locationForm.patchValue({
      title: location.title,
      city: location.city,
      country: location.country,
      category: location.category,
      latitude: location.latitude,
      longitude: location.longitude,
      description: location.description || '',
      visitedAt: location.visitedAt ? new Date(location.visitedAt) : null
    });
    this.isDrawerVisible.set(true);
  }

  closeDrawer(): void {
    this.isDrawerVisible.set(false);
    this.locationForm.reset();
  }

  submitLocation(): void {
    if (this.locationForm.invalid) {
      Object.values(this.locationForm.controls).forEach((ctrl) => {
        if (ctrl.invalid) {
          ctrl.markAsDirty();
          ctrl.updateValueAndValidity({ onlySelf: true });
        }
      });
      return;
    }

    this.isSubmitting.set(true);
    const formVal = this.locationForm.value;

    let visitedAtStr: string | null = null;
    if (formVal.visitedAt) {
      const d = new Date(formVal.visitedAt);
      visitedAtStr = d.toISOString().split('T')[0];
    }

    const payload: CreateLocationRequest | UpdateLocationRequest = {
      title: formVal.title,
      city: formVal.city,
      country: formVal.country,
      category: formVal.category,
      latitude: formVal.latitude !== null && formVal.latitude !== '' ? Number(formVal.latitude) : null,
      longitude: formVal.longitude !== null && formVal.longitude !== '' ? Number(formVal.longitude) : null,
      description: formVal.description || null,
      visitedAt: visitedAtStr
    };

    const id = this.editingLocationId();
    if (id) {
      this.locationService.updateLocation(id, payload).subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.message.success('Destination updated successfully!');
          this.closeDrawer();
        },
        error: () => {
          this.isSubmitting.set(false);
          this.message.error('Failed to update destination. Please try again.');
        }
      });
    } else {
      this.locationService.createLocation(payload).subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.message.success('Destination added successfully!');
          this.closeDrawer();
        },
        error: () => {
          this.isSubmitting.set(false);
          this.message.error('Failed to save destination. Please try again.');
        }
      });
    }
  }

  deleteLocation(id: number): void {
    this.locationService.deleteLocation(id).subscribe({
      next: () => {
        this.message.success('Destination removed');
      },
      error: () => {
        this.message.error('Failed to delete destination');
      }
    });
  }

  getMeta(category: LocationCategory): CategoryMeta {
    return this.categoryMeta[category] || this.categoryMeta.OTHER;
  }

  onLogout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      }
    });
  }
}
