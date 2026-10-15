import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Poll, Shift } from '../../../../core/models';

@Component({
  selector: 'app-poll-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './poll-card.component.html',
  styleUrl: './poll-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class: 'block w-full'
  }
})
export class PollCardComponent {
  public readonly poll = input.required<Poll>();
  public readonly isSelected = input<boolean>(false);

  public readonly select = output<Poll>();

  public readonly shiftLabel = computed(() => {
    const shift = this.poll().shift;
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
    const raw = this.poll().pollDate;
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

  public readonly targetUniversitiesList = computed(() => {
    return this.poll().targetUniversities || [];
  });

  public readonly optionsList = computed(() => {
    return this.poll().options || [];
  });

  public onSelectButtonClick(event: Event): void {
    event.stopPropagation();
    this.select.emit(this.poll());
  }
}
