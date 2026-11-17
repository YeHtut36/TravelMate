import { inject, Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { LoginRequest, RegisterRequest, User } from '../models/user.model';

/**
 * Service managing user authentication state, session verification, and API communication.
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/auth';

  readonly currentUser = signal<User | null>(null);
  readonly isAuthenticated = computed(() => this.currentUser() !== null);
  readonly isInitialized = signal<boolean>(false);

  checkAuth(): Observable<User | null> {
    return this.http.get<User>(`${this.baseUrl}/me`, { withCredentials: true }).pipe(
      tap((user) => {
        this.currentUser.set(user);
        this.isInitialized.set(true);
      }),
      catchError(() => {
        this.currentUser.set(null);
        this.isInitialized.set(true);
        return of(null);
      })
    );
  }

  register(data: RegisterRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/register`, data, { withCredentials: true });
  }

  login(credentials: LoginRequest): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/login`, credentials, { withCredentials: true }).pipe(
      tap((user) => {
        this.currentUser.set(user);
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/logout`, {}, { withCredentials: true }).pipe(
      tap(() => {
        this.currentUser.set(null);
      }),
      catchError(() => {
        this.currentUser.set(null);
        return of(undefined);
      })
    );
  }
}
