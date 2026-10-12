import { ChangeDetectionStrategy, Component, computed, inject, input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/auth/services/auth.service';

export type NavTab = 'inicio' | 'rotas' | 'onibus' | 'enquetes' | 'perfil';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class SidebarComponent {
  public readonly authService = inject(AuthService);

  public readonly activeTab = input<NavTab>('inicio');

  public readonly studentName = computed(() => {
    const user = this.authService.currentUser();
    if (!user) return 'Estudante';
    const rawName = user.name || user.email.split('@')[0];
    return rawName.split(' ')[0];
  });

  public logout(): void {
    this.authService.logout();
  }
}
