import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { AuthUser, LoginResponse, UserLogin, UserRole } from '../../models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiUrl = `${environment.apiUrl}/auth`;

  private readonly tokenSignal = signal<string | null>(null);
  private readonly userSignal = signal<AuthUser | null>(null);

  public readonly token = computed(() => this.tokenSignal());
  public readonly currentUser = computed(() => this.userSignal());
  public readonly isAuthenticated = computed(() => !!this.tokenSignal());
  public readonly userRole = computed(() => this.userSignal()?.role);

  public readonly isAdmin = computed(() => this.hasRole(UserRole.ADMIN, 'ADMIN', 'ADMINISTRADOR'));
  public readonly isMunicipality = computed(() => this.hasRole(UserRole.MUNICIPALITY, 'PREFEITURA', 'MUNICIPALITY'));
  public readonly isDriver = computed(() => this.hasRole(UserRole.BUS_DRIVER, 'MOTORISTA', 'BUS_DRIVER'));
  public readonly isStudent = computed(() => this.hasRole(UserRole.STUDENT, 'ESTUDANTE', 'STUDENT'));

  login(credentials: UserLogin): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials, {
      withCredentials: true
    }).pipe(
      tap((response) => {
        this.saveToken(response.accessToken, response.user);
      })
    );
  }

  refreshToken(): Observable<{ accessToken: string }> {
    return this.http.post<{ accessToken: string }>(
      `${this.apiUrl}/refresh`,
      {},
      { withCredentials: true }
    ).pipe(
      tap((response) => {
        this.saveToken(response.accessToken);
      })
    );
  }

  restoreSession(): Observable<AuthUser | null> {
    return this.http.post<{ accessToken: string }>(
      `${this.apiUrl}/refresh`,
      {},
      { withCredentials: true }
    ).pipe(
      map((response) => {
        this.saveToken(response.accessToken);
        return this.userSignal();
      }),
      catchError(() => {
        this.clearSession();
        return of(null);
      })
    );
  }

  saveToken(token: string, user?: AuthUser): void {
    this.tokenSignal.set(token);
    if (user) {
      this.userSignal.set(user);
    } else {
      const extracted = this.extractUserFromJwt(token);
      if (extracted) {
        const current = this.userSignal();
        this.userSignal.set({
          ...extracted,
          name: current?.name || extracted.name
        });
      }
    }
  }

  getToken(): string | null {
    return this.tokenSignal();
  }

  getUser(): AuthUser | null {
    return this.userSignal();
  }

  isOwner(targetUserId?: string): boolean {
    const user = this.userSignal();
    if (!user || !targetUserId) return false;
    return user.id === targetUserId;
  }

  logout(): void {
    this.http.post<{ message: string }>(`${this.apiUrl}/logout`, {}, { withCredentials: true }).subscribe({
      next: () => {
        this.clearSession();
        this.router.navigate(['/login']);
      },
      error: () => {
        this.clearSession();
        this.router.navigate(['/login']);
      }
    });
  }

  clearSession(): void {
    this.tokenSignal.set(null);
    this.userSignal.set(null);
  }

  hasRole(...roles: (UserRole | string)[]): boolean {
    const currentRole = this.userRole();
    if (!currentRole) {
      return false;
    }

    const currentUpper = String(currentRole).toUpperCase();

    return roles.some((expected) => {
      if (!expected) return false;
      const expectedUpper = String(expected).toUpperCase();

      if (expectedUpper === currentUpper) {
        return true;
      }

      if ((expectedUpper === 'ADMIN' || expectedUpper === 'ADMINISTRADOR') &&
          (currentUpper === 'ADMIN' || currentUpper === 'ADMINISTRADOR')) {
        return true;
      }
      if ((expectedUpper === 'MUNICIPALITY' || expectedUpper === 'PREFEITURA') &&
          (currentUpper === 'MUNICIPALITY' || currentUpper === 'PREFEITURA')) {
        return true;
      }
      if ((expectedUpper === 'BUS_DRIVER' || expectedUpper === 'MOTORISTA') &&
          (currentUpper === 'BUS_DRIVER' || currentUpper === 'MOTORISTA')) {
        return true;
      }
      if ((expectedUpper === 'STUDENT' || expectedUpper === 'ESTUDANTE') &&
          (currentUpper === 'STUDENT' || currentUpper === 'ESTUDANTE')) {
        return true;
      }

      return false;
    });
  }

  private extractUserFromJwt(token: string): AuthUser | null {
    try {
      const parts = token.split('.');
      if (parts.length < 2) return null;

      const base64Url = parts[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );

      const parsed = JSON.parse(jsonPayload);

      return {
        id: parsed.id ?? '',
        name: parsed.name ?? (parsed.sub ? parsed.sub.split('@')[0] : ''),
        email: parsed.sub ?? '',
        role: (parsed.role as UserRole) ?? UserRole.STUDENT
      };
    } catch {
      return null;
    }
  }
}
