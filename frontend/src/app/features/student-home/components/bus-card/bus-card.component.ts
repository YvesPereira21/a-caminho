import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { StatusBadgeComponent } from '../status-badge/status-badge.component';

export interface BusCardItem {
  id: string;
  routeName: string;
  shift: string;
  pollDate: string;
  startTime: string;
  endTime: string;
  confirmedVotes: number;
}

@Component({
  selector: 'app-bus-card',
  imports: [CommonModule, StatusBadgeComponent],
  templateUrl: './bus-card.component.html',
  styleUrl: './bus-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class BusCardComponent {
  public readonly item = input.required<BusCardItem>();
  public readonly isSelected = input<boolean>(false);
  public readonly selectCard = output<BusCardItem>();

  onCardClick(): void {
    this.selectCard.emit(this.item());
  }
}
