import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { PollService } from '../../../polls/services/poll.service';
import { BusService } from '../../../bus/services/bus.service';
import { TravelService } from '../../../traveling/services/travel.service';
import { Bus, isOutboundTravel, Poll, PollDirection, Shift, TravelCreate, TravelStatus } from '../../../../core/models';
import { DriverHeaderComponent } from '../../../../shared/components/driver-header/driver-header.component';

@Component({
  selector: 'app-prepare-travel',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    DriverHeaderComponent
  ],
  templateUrl: './prepare-travel.component.html',
  styleUrl: './prepare-travel.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PrepareTravelComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  public readonly authService = inject(AuthService);
  private readonly pollService = inject(PollService);
  private readonly busService = inject(BusService);
  private readonly travelService = inject(TravelService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  public readonly isLoading = signal<boolean>(true);
  public readonly isSubmitting = signal<boolean>(false);
  public readonly hasError = signal<boolean>(false);
  public readonly errorMessage = signal<string | null>(null);

  public readonly poll = signal<Poll | null>(null);
  public readonly buses = signal<Bus[]>([]);
  public readonly selectedBusId = signal<string | null>(null);
  public readonly formSubmitted = signal<boolean>(false);
  public readonly direction = signal<PollDirection>(PollDirection.OUTBOUND);

  public readonly isOutbound = computed<boolean>(() => {
    return isOutboundTravel(this.direction());
  });

  public readonly travelForm: FormGroup = this.fb.group({
    returnTime: ['', [Validators.required]]
  });

  public readonly driverName = computed(() => {
    const user = this.authService.currentUser();
    if (!user) return 'Motorista';
    return user.name || user.email.split('@')[0];
  });

  public readonly driverEmail = computed(() => {
    return this.authService.currentUser()?.email || '';
  });

  public readonly shiftLabel = computed(() => {
    const shift = this.poll()?.shift;
    switch (shift) {
      case Shift.MORNING:
      case 'MANHA' as any:
      case 'MORNING' as any:
        return 'Manhã';
      case Shift.AFTERNOON:
      case 'TARDE' as any:
      case 'AFTERNOON' as any:
        return 'Tarde';
      case Shift.NIGHT:
      case 'NOITE' as any:
      case 'NIGHT' as any:
        return 'Noite';
      default:
        return shift || 'Geral';
    }
  });

  public readonly formattedDate = computed(() => {
    const raw = this.poll()?.pollDate;
    if (!raw) return '';
    try {
      const parts = raw.split('-');
      if (parts.length === 3) {
        return `${parts[2]}/${parts[1]}/${parts[0]}`;
      }
      return new Date(raw).toLocaleDateString('pt-BR');
    } catch {
      return raw;
    }
  });

  public readonly departureTime = computed(() => {
    const raw = this.poll()?.startTime;
    if (!raw) return '';
    try {
      if (raw.includes('T')) {
        const timePart = raw.split('T')[1];
        return timePart.substring(0, 5);
      }
      if (raw.includes(':')) {
        return raw.substring(0, 5);
      }
      return '';
    } catch {
      return '';
    }
  });

  public readonly targetUniversities = computed(() => {
    return this.poll()?.targetUniversities || [];
  });

  public readonly options = computed(() => {
    return this.poll()?.options || [];
  });

  public readonly totalConfirmedVotes = computed(() => {
    return this.poll()?.confirmedVotesCount ?? 0;
  });

  public readonly maxStopVotes = computed(() => {
    const counts = this.options().map(o => o.voteCount || 0);
    return Math.max(1, ...counts);
  });

  public readonly maxUniVotes = computed(() => {
    const counts = this.targetUniversities().map(u => u.voteCount || 0);
    return Math.max(1, ...counts);
  });

  public readonly selectedBus = computed(() => {
    const id = this.selectedBusId();
    if (!id) return null;
    return this.buses().find(b => b.busId === id) || null;
  });

  ngOnInit(): void {
    this.loadData();
  }

  public loadData(): void {
    this.isLoading.set(true);
    this.hasError.set(false);
    this.errorMessage.set(null);

    // 1. Tenta recuperar enquete do estado da rota
    const navStatePoll = (history.state?.poll as Poll) || null;
    const queryPollId = this.route.snapshot.queryParamMap.get('pollId');
    const storagePollId = sessionStorage.getItem('selected_poll_id');
    const targetPollId = queryPollId || storagePollId || navStatePoll?.pollId;

    // Recupera direção (Ida ou Volta) e ônibus pré-selecionado (se houver)
    const stateDirection = history.state?.direction;
    const queryDirection = this.route.snapshot.queryParamMap.get('direction');
    if (stateDirection === PollDirection.INBOUND || queryDirection === 'INBOUND' || queryDirection === 'VOLTA') {
      this.direction.set(PollDirection.INBOUND);
    } else {
      this.direction.set(PollDirection.OUTBOUND);
    }

    const preselectedBus = history.state?.preselectedBusId;
    if (preselectedBus) {
      this.selectedBusId.set(preselectedBus);
    }

    // Carrega os ônibus disponíveis
    this.busService.getAllBusesForDriver().subscribe({
      next: (buses) => {
        this.buses.set(buses || []);
      },
      error: (err) => {
        console.warn('Não foi possível carregar os ônibus:', err);
      }
    });

    // Se temos a enquete em memória com opções, podemos utilizá-la ou buscar atualizada do endpoint de motorista
    this.pollService.getPollsForDriver().subscribe({
      next: (polls) => {
        const found = targetPollId ? polls.find(p => p.pollId === targetPollId) : null;
        if (found) {
          this.poll.set(found);
        } else if (navStatePoll) {
          this.poll.set(navStatePoll);
        } else if (polls.length > 0) {
          this.poll.set(polls[0]);
        } else {
          this.hasError.set(true);
          this.errorMessage.set('Nenhuma enquete selecionada foi encontrada.');
        }
        this.isLoading.set(false);
      },
      error: (err) => {
        if (navStatePoll) {
          this.poll.set(navStatePoll);
          this.isLoading.set(false);
        } else {
          console.error('Erro ao buscar dados da enquete para preparação:', err);
          this.hasError.set(true);
          this.errorMessage.set(err?.error?.message || 'Erro ao carregar informações da enquete.');
          this.isLoading.set(false);
        }
      }
    });
  }

  public selectBus(busId: string): void {
    this.selectedBusId.set(busId);
  }

  public onBackToPolls(): void {
    this.router.navigate(['/driver/polls']);
  }

  public onStartTravel(): void {
    this.formSubmitted.set(true);

    const poll = this.poll();
    if (!poll) {
      this.errorMessage.set('Nenhuma enquete selecionada.');
      return;
    }

    const busId = this.selectedBusId();
    if (!busId) {
      this.errorMessage.set('Por favor, selecione um ônibus antes de iniciar a viagem.');
      return;
    }

    if (this.travelForm.invalid) {
      this.errorMessage.set('Por favor, informe um horário previsto de retorno válido.');
      return;
    }

    const rawReturnTime: string = this.travelForm.get('returnTime')?.value;
    const formattedReturnTime = rawReturnTime.length === 5 ? `${rawReturnTime}:00` : rawReturnTime;

    const payload: TravelCreate = {
      pollId: poll.pollId,
      busId: busId,
      status: TravelStatus.IN_PROGRESS,
      returnTime: formattedReturnTime,
      direction: this.direction()
    };

    this.isSubmitting.set(true);
    this.errorMessage.set(null);

    this.travelService.createTravel(payload).subscribe({
      next: (created) => {
        this.isSubmitting.set(false);
        sessionStorage.removeItem('selected_poll_id');
        sessionStorage.removeItem('selected_poll_route');
        this.router.navigate(['/driver/travel-in-progress'], {
          state: {
            travel: created,
            poll: poll,
            bus: this.selectedBus()
          }
        });
      },
      error: (err) => {
        this.isSubmitting.set(false);
        console.error('Erro ao iniciar viagem:', err);
        const detailedMsg = err?.error?.message || err?.message || 'Falha ao iniciar viagem. Verifique se você já possui uma viagem em andamento.';
        this.errorMessage.set(detailedMsg);
      }
    });
  }

  public onLogout(): void {
    this.authService.logout();
  }
}
