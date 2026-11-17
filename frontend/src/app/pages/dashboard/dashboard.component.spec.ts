import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { en_US, provideNzI18n } from 'ng-zorro-antd/i18n';
import { provideNzNativeDateAdapter } from 'ng-zorro-antd/core/time';
import { provideNzIcons } from 'ng-zorro-antd/icon';
import { IconDefinition } from '@ant-design/icons-angular';
import * as AllIcons from '@ant-design/icons-angular/icons';
import { NzMessageService } from 'ng-zorro-antd/message';
import { AuthService } from '../../services/auth.service';
import { LocationService } from '../../services/location.service';
import { signal } from '@angular/core';
import { of } from 'rxjs';
import { LocationItem } from '../../models/location.model';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  const mockLocations: LocationItem[] = [
    {
      id: 1,
      title: 'Bagan Temples',
      city: 'Bagan',
      country: 'Myanmar',
      category: 'HISTORICAL',
      createdAt: '2026-01-01T00:00:00Z'
    },
    {
      id: 2,
      title: 'Eiffel Tower',
      city: 'Paris',
      country: 'France',
      category: 'CULTURE',
      createdAt: '2026-01-02T00:00:00Z'
    }
  ];

  const authServiceStub = {
    currentUser: signal({ id: 1, email: 'traveler@example.com', fullName: 'Alice Traveler', role: 'ROLE_USER' }),
    isAuthenticated: signal(true),
    logout: () => of(undefined)
  };

  const locationServiceStub = {
    locations: signal<LocationItem[]>(mockLocations),
    stats: signal({ totalLocations: 2, totalCountries: 2 }),
    isLoading: signal<boolean>(false),
    loadLocations: () => of(mockLocations),
    loadStats: () => of({ totalLocations: 2, totalCountries: 2 }),
    createLocation: () => of(mockLocations[0]),
    updateLocation: () => of(mockLocations[0]),
    deleteLocation: () => of(undefined)
  };

  const antDesignIcons: IconDefinition[] = Object.values(AllIcons) as IconDefinition[];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
      providers: [
        provideHttpClient(),
        provideRouter([]),
        provideAnimationsAsync(),
        provideNzI18n(en_US),
        provideNzNativeDateAdapter(),
        provideNzIcons(antDesignIcons),
        NzMessageService,
        { provide: AuthService, useValue: authServiceStub },
        { provide: LocationService, useValue: locationServiceStub }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create dashboard component', () => {
    expect(component).toBeTruthy();
  });

  it('should filter locations by search query reactively', () => {
    component.searchQuery.set('bagan');
    const filtered = component.filteredLocations();
    expect(filtered.length).toBe(1);
    expect(filtered[0].title).toBe('Bagan Temples');
  });

  it('should filter locations by category reactively', () => {
    component.selectedCategory.set('CULTURE');
    const filtered = component.filteredLocations();
    expect(filtered.length).toBe(1);
    expect(filtered[0].title).toBe('Eiffel Tower');
  });

  it('should open and close create drawer', () => {
    component.openCreateDrawer();
    expect(component.isDrawerVisible()).toBe(true);
    expect(component.editingLocationId()).toBeNull();

    component.closeDrawer();
    expect(component.isDrawerVisible()).toBe(false);
  });

  it('should open edit drawer with existing location data', () => {
    component.openEditDrawer(mockLocations[0]);
    expect(component.isDrawerVisible()).toBe(true);
    expect(component.editingLocationId()).toBe(1);
    expect(component.locationForm.value.title).toBe('Bagan Temples');
  });
});
