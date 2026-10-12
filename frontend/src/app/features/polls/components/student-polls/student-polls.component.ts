import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { PollService } from '../../services/poll.service';
import { PollVoteService } from '../../../poll-votes/services/poll-vote.service';
import { Poll, PollList, Shift, VoteResponse } from '../../../../core/models';
import { SidebarComponent } from '../../../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-student-polls',
  imports: [CommonModule, ReactiveFormsModule, RouterModule, SidebarComponent],
  templateUrl: './student-polls.component.html',
  styleUrl: './student-polls.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StudentPollsComponent implements OnInit {
  public readonly authService = inject(AuthService);
  private readonly pollService = inject(PollService);
  private readonly pollVoteService = inject(PollVoteService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  // Layout signals
  public readonly isMobileMenuOpen = signal<boolean>(false);
  public readonly activeNavTab = signal<'inicio' | 'rotas' | 'onibus' | 'enquetes' | 'perfil'>('enquetes');

  // Data signals
  public readonly isLoading = signal<boolean>(true);
  public readonly isSubmitting = signal<boolean>(false);
  public readonly isCanceling = signal<boolean>(false);
  public readonly pollsList = signal<PollList[]>([]);
  public readonly selectedPollId = signal<string | null>(null);
  public readonly selectedPoll = signal<Poll | null>(null);
  public readonly myVote = signal<VoteResponse | null>(null);

  // Modal signal
  public readonly isChangeVoteModalOpen = signal<boolean>(false);

  // Feedback messages
  public readonly successMessage = signal<string | null>(null);
  public readonly errorMessage = signal<string | null>(null);

  // Reactive Form
  public voteForm: FormGroup = this.fb.group({
    optionId: ['', [Validators.required]],
    returnConfirmed: [true]
  });

  public readonly studentName = computed(() => {
    const user = this.authService.currentUser();
    if (!user) return 'Estudante';
    const rawName = user.name || user.email.split('@')[0];
    return rawName.split(' ')[0];
  });

  public readonly activePollItem = computed(() => {
    const polls = this.pollsList();
    const id = this.selectedPollId();
    if (!id) return polls[0] || null;
    return polls.find((p) => p.pollId === id) || polls[0] || null;
  });

  ngOnInit(): void {
    this.loadOpenPolls();
  }

  public loadOpenPolls(): void {
    this.isLoading.set(true);
    this.clearMessages();

    this.pollService.getOpenPollsToStudent().subscribe({
      next: (polls) => {
        this.pollsList.set(polls);
        if (polls.length > 0) {
          const initialId = polls[0].pollId;
          this.selectedPollId.set(initialId);
          this.loadPollDetails(initialId);
        } else {
          this.selectedPollId.set(null);
          this.selectedPoll.set(null);
          this.myVote.set(null);
          this.isLoading.set(false);
        }
      },
      error: () => {
        this.pollsList.set([]);
        this.selectedPoll.set(null);
        this.myVote.set(null);
        this.isLoading.set(false);
        this.errorMessage.set('Não foi possível carregar as enquetes disponíveis no momento.');
      }
    });
  }

  public selectPoll(pollId: string): void {
    if (this.selectedPollId() === pollId) return;
    this.selectedPollId.set(pollId);
    this.clearMessages();
    this.loadPollDetails(pollId);
  }

  private loadPollDetails(pollId: string): void {
    this.isLoading.set(true);
    this.voteForm.reset({ optionId: '', returnConfirmed: true });

    this.pollService.getPoll(pollId).subscribe({
      next: (pollDetails) => {
        this.selectedPoll.set(pollDetails);
        this.checkExistingVote(pollId);
      },
      error: () => {
        this.selectedPoll.set(null);
        this.isLoading.set(false);
        this.errorMessage.set('Erro ao carregar detalhes da enquete.');
      }
    });
  }

  private checkExistingVote(pollId: string): void {
    this.pollVoteService.getMyVote(pollId).subscribe({
      next: (vote) => {
        this.myVote.set(vote);
        this.isLoading.set(false);
      },
      error: (err) => {
        // 404 indicates student hasn't voted yet
        if (err.status === 404) {
          this.myVote.set(null);
        } else {
          this.myVote.set(null);
          this.errorMessage.set('Não foi possível verificar seu voto anterior. Tente recarregar a página.');
        }
        this.isLoading.set(false);
      }
    });
  }

  public selectOption(optionId: string): void {
    this.voteForm.patchValue({ optionId });
    this.voteForm.get('optionId')?.markAsDirty();
  }

  public submitVote(): void {
    if (this.voteForm.invalid) {
      this.voteForm.markAllAsTouched();
      this.errorMessage.set('Selecione um ponto de embarque antes de confirmar seu voto.');
      return;
    }

    const pollId = this.selectedPollId();
    if (!pollId) return;

    this.clearMessages();
    this.isSubmitting.set(true);

    const formVal = this.voteForm.value;
    const request = {
      optionId: formVal.optionId,
      returnConfirmed: formVal.returnConfirmed !== null ? formVal.returnConfirmed : true
    };

    this.pollVoteService.vote(pollId, request).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.successMessage.set('Voto registrado com sucesso! Sua presença foi confirmada para esta rota.');
        this.loadPollDetails(pollId);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const backendMsg = err.error?.message || err.error?.error || 'Erro ao registrar voto. Verifique os dados e tente novamente.';
        this.errorMessage.set(backendMsg);
      }
    });
  }

  public openChangeVoteModal(): void {
    this.isChangeVoteModalOpen.set(true);
  }

  public closeChangeVoteModal(): void {
    this.isChangeVoteModalOpen.set(false);
  }

  public cancelMyVote(): void {
    const pollId = this.selectedPollId();
    if (!pollId) return;

    this.clearMessages();
    this.isCanceling.set(true);

    this.pollVoteService.cancelVote(pollId).subscribe({
      next: () => {
        this.isCanceling.set(false);
        this.isChangeVoteModalOpen.set(false);
        this.myVote.set(null);
        this.voteForm.reset({ optionId: '', returnConfirmed: true });
        this.successMessage.set('Voto cancelado com sucesso. Selecione seu novo ponto de embarque e confirme seu voto.');
        this.loadPollDetails(pollId);
      },
      error: (err) => {
        this.isCanceling.set(false);
        this.isChangeVoteModalOpen.set(false);
        const backendMsg = err.error?.message || err.error?.error || 'Não foi possível cancelar o voto.';
        this.errorMessage.set(backendMsg);
      }
    });
  }

  public getShiftLabel(shift?: Shift): string {
    if (!shift) return 'Integral';
    switch (shift) {
      case Shift.MORNING:
        return 'Manhã';
      case Shift.AFTERNOON:
        return 'Tarde';
      case Shift.NIGHT:
        return 'Noite';
      default:
        return shift;
    }
  }

  public formatTime(timeStr?: string): string {
    if (!timeStr) return '--:--';
    if (timeStr.length >= 16) {
      return timeStr.substring(11, 16);
    }
    return timeStr;
  }

  public formatDate(dateStr?: string): string {
    if (!dateStr) return '';
    const parts = dateStr.split('-');
    if (parts.length === 3) {
      return `${parts[2]}/${parts[1]}/${parts[0]}`;
    }
    return dateStr;
  }

  private clearMessages(): void {
    this.successMessage.set(null);
    this.errorMessage.set(null);
  }

  // Navigation handlers
  public navigateTo(route: string): void {
    this.closeMobileMenu();
    this.router.navigate([route]);
  }

  public toggleMobileMenu(): void {
    this.isMobileMenuOpen.update((open) => !open);
  }

  public closeMobileMenu(): void {
    this.isMobileMenuOpen.set(false);
  }

  public logout(): void {
    this.authService.logout();
  }
}

