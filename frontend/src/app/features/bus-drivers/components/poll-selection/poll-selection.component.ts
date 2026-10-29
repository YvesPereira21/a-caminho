import { ChangeDetectionStrategy, Component, computed, HostListener, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { PollService } from '../../../polls/services/poll.service';
import { Poll } from '../../../../core/models';
import { DriverHeaderComponent } from '../../../../shared/components/driver-header/driver-header.component';
import { PollCardComponent } from '../../../polls/components/poll-card/poll-card.component';

@Component({
  selector: 'app-poll-selection',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    DriverHeaderComponent,
    PollCardComponent
  ],
  templateUrl: './poll-selection.component.html',
  styleUrl: './poll-selection.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PollSelectionComponent implements OnInit {
  public readonly authService = inject(AuthService);
  private readonly pollService = inject(PollService);
  private readonly router = inject(Router);

  public readonly isLoading = signal<boolean>(true);
  public readonly hasError = signal<boolean>(false);
  public readonly errorMessage = signal<string>('Não foi possível carregar as enquetes disponíveis.');
  public readonly polls = signal<Poll[]>([]);
  public readonly selectedPollId = signal<string | null>(null);
  public readonly actionFeedback = signal<string | null>(null);

  public readonly driverName = computed(() => {
    const user = this.authService.currentUser();
    if (!user) return 'Motorista';
    return user.name || user.email.split('@')[0];
  });

  public readonly driverEmail = computed(() => {
    return this.authService.currentUser()?.email || '';
  });

  public readonly selectedPoll = computed(() => {
    const id = this.selectedPollId();
    if (!id) return null;
    return this.polls().find(p => p.pollId === id) || null;
  });

  public readonly hasSelectedPoll = computed(() => {
    return this.selectedPollId() !== null;
  });

  ngOnInit(): void {
    this.loadPolls();
  }

  public loadPolls(): void {
    this.isLoading.set(true);
    this.hasError.set(false);

    this.pollService.getPollsForDriver().subscribe({
      next: (polls) => {
        this.polls.set(polls);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Erro ao carregar enquetes para o motorista:', err);
        const detailedMsg = err?.error?.message || err?.message || 'Não foi possível carregar as enquetes disponíveis.';
        this.errorMessage.set(detailedMsg);
        this.hasError.set(true);
        this.isLoading.set(false);
      }
    });
  }

  public onSelectPoll(poll: Poll): void {
    if (this.selectedPollId() === poll.pollId) {
      // Clicar novamente no botão desmarca a enquete
      this.selectedPollId.set(null);
    } else {
      this.selectedPollId.set(poll.pollId);
    }
  }

  @HostListener('document:click', ['$event'])
  public onDocumentClick(event: MouseEvent): void {
    const target = event.target as HTMLElement;
    // Se o clique foi dentro de um card de enquete ou na barra de continuar, mantém a seleção
    if (target.closest('.poll-card') || target.closest('.poll-action-bar')) {
      return;
    }
    // Ao clicar fora da enquete, ela é desselecionada
    if (this.selectedPollId()) {
      this.selectedPollId.set(null);
    }
  }

  public onContinue(): void {
    const poll = this.selectedPoll();
    if (!poll) return;

    try {
      sessionStorage.setItem('selected_poll_id', poll.pollId);
      sessionStorage.setItem('selected_poll_route', poll.routeName);
    } catch {
      // Ignora erro de storage se indisponível
    }

    this.router.navigate(['/driver/prepare-travel'], {
      queryParams: { pollId: poll.pollId },
      state: { poll }
    });
  }

  public onBackToDashboard(): void {
    this.router.navigate(['/driver']);
  }

  public onLogout(): void {
    this.authService.logout();
  }
}
