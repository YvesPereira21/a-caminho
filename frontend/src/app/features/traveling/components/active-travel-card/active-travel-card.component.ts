import { ChangeDetectionStrategy, Component, computed, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { isOutboundTravel, PollDirection, Travel } from '../../../../core/models';

@Component({
  selector: 'app-active-travel-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './active-travel-card.component.html',
  styleUrl: './active-travel-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ActiveTravelCardComponent {
  public readonly travel = input.required<Travel>();
  public readonly manage = output<string>();

  public readonly isOutbound = computed(() => isOutboundTravel(this.travel().direction));

  public onManage(): void {
    this.manage.emit(this.travel().travelId);
  }
}

