import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LocationService } from './location.service';
import { LocationItem, LocationStats } from '../models/location.model';

describe('LocationService', () => {
  let service: LocationService;
  let httpTesting: HttpTestingController;

  const mockLocations: LocationItem[] = [
    {
      id: 1,
      title: 'Mount Fuji',
      city: 'Shizuoka',
      country: 'Japan',
      category: 'NATURE',
      createdAt: '2026-01-01T00:00:00Z'
    }
  ];

  const mockStats: LocationStats = {
    totalLocations: 1,
    totalCountries: 1
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        LocationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(LocationService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.locations().length).toBe(0);
  });

  it('should load locations and update signal', () => {
    service.loadLocations().subscribe((res) => {
      expect(res).toEqual(mockLocations);
      expect(service.locations()).toEqual(mockLocations);
      expect(service.isLoading()).toBe(false);
    });

    const req = httpTesting.expectOne('/api/locations');
    expect(req.request.method).toBe('GET');
    expect(req.request.withCredentials).toBe(true);
    req.flush(mockLocations);
  });

  it('should create location and prepend to signal', () => {
    const newLocation: LocationItem = {
      id: 2,
      title: 'Bagan',
      city: 'Mandalay',
      country: 'Myanmar',
      category: 'HISTORICAL',
      createdAt: '2026-02-01T00:00:00Z'
    };

    service.createLocation({
      title: 'Bagan',
      city: 'Mandalay',
      country: 'Myanmar',
      category: 'HISTORICAL'
    }).subscribe((res) => {
      expect(res).toEqual(newLocation);
      expect(service.locations()[0]).toEqual(newLocation);
    });

    const req = httpTesting.expectOne('/api/locations');
    expect(req.request.method).toBe('POST');
    req.flush(newLocation);

    // Should also trigger loadStats
    const statsReq = httpTesting.expectOne('/api/locations/stats');
    statsReq.flush(mockStats);
  });

  it('should delete location and remove from signal', () => {
    service.locations.set(mockLocations);

    service.deleteLocation(1).subscribe(() => {
      expect(service.locations().length).toBe(0);
    });

    const req = httpTesting.expectOne('/api/locations/1');
    expect(req.request.method).toBe('DELETE');
    req.flush(null);

    const statsReq = httpTesting.expectOne('/api/locations/stats');
    statsReq.flush({ totalLocations: 0, totalCountries: 0 });
  });
});
