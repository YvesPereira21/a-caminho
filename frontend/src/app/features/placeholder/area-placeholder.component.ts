import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/services/auth.service';

@Component({
  selector: 'app-area-placeholder',
  imports: [],
  template: `
    <div class="min-h-screen bg-slate-50 flex flex-col items-center justify-center p-6 text-center">
      <div class="max-w-md w-full bg-white p-8 rounded-2xl shadow-sm border border-slate-200 space-y-4">
        <div class="w-16 h-16 rounded-2xl bg-emerald-100 text-emerald-700 flex items-center justify-center mx-auto">
          <svg class="w-8 h-8 text-emerald-700" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M4 6a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6z" />
            <path d="M4 11h16" />
            <circle cx="8" cy="15" r="1" fill="currentColor" />
            <circle cx="16" cy="15" r="1" fill="currentColor" />
            <path d="M6 18v2" />
            <path d="M18 18v2" />
          </svg>
        </div>
        <h1 class="text-2xl font-bold text-slate-800">A caminho!</h1>
        <p class="text-slate-600 text-sm">
          Bem-vindo, <strong>{{ authService.currentUser()?.name || authService.currentUser()?.email }}</strong>!
        </p>
        <div class="inline-block px-3 py-1 bg-emerald-50 text-emerald-700 rounded-full text-xs font-semibold uppercase tracking-wider">
          Perfil: {{ authService.userRole() || 'Autenticado' }}
        </div>
        <p class="text-slate-500 text-xs">
          Esta área específica está pronta para receber os módulos e funcionalidades do seu perfil.
        </p>
        <div class="pt-4 flex flex-col gap-2">
          <button
            type="button"
            (click)="logout()"
            class="w-full py-2.5 px-4 bg-slate-100 hover:bg-slate-200 text-slate-700 rounded-xl font-medium text-sm transition-colors"
          >
            Sair da Conta (Logout)
          </button>
        </div>
      </div>
    </div>
  `
})
export class AreaPlaceholderComponent {
  public readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  logout(): void {
    this.authService.logout();
  }
}

