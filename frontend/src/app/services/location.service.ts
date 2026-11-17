import { inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import {
  CreateLocationRequest,
  LocationItem,
  LocationStats,
  UpdateLocationRequest
} from '../models/location.model';

/**
 * Service managing user-saved travel locations and stats with reactive signals.
 */
@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/locations';

  readonly locations = signal<LocationItem[]>([]);
  readonly stats = signal<LocationStats>({ totalLocations: 0, totalCountries: 0 });
  readonly isLoading = signal<boolean>(false);

  loadLocations(): Observable<LocationItem[]> {
    this.isLoading.set(true);
    return this.http.get<LocationItem[]>(this.baseUrl, { withCredentials: true }).pipe(
      tap({
        next: (items) => {
          this.locations.set(items);
          this.isLoading.set(false);
        },
        error: () => {
          this.isLoading.set(false);
        }
      })
    );
  }

  loadStats(): Observable<LocationStats> {
    return this.http.get<LocationStats>(`${this.baseUrl}/stats`, { withCredentials: true }).pipe(
      tap((stats) => {
        this.stats.set(stats);
      })
    );
  }

  createLocation(request: CreateLocationRequest): Observable<LocationItem> {
    return this.http.post<LocationItem>(this.baseUrl, request, { withCredentials: true }).pipe(
      tap((created) => {
        this.locations.update((prev) => [created, ...prev]);
        this.loadStats().subscribe();
      })
    );
  }

  updateLocation(id: number, request: UpdateLocationRequest): Observable<LocationItem> {
    return this.http.put<LocationItem>(`${this.baseUrl}/${id}`, request, { withCredentials: true }).pipe(
      tap((updated) => {
        this.locations.update((prev) =>
          prev.map((item) => (item.id === id ? updated : item))
        );
        this.loadStats().subscribe();
      })
    );
  }

  deleteLocation(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`, { withCredentials: true }).pipe(
      tap(() => {
        this.locations.update((prev) => prev.filter((item) => item.id !== id));
        this.loadStats().subscribe();
      })
    );
  }
}
