import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PollDirection, Travel } from '../../../../core/models';

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

  public readonly PollDirection = PollDirection;

  public onManage(): void {
    this.manage.emit(this.travel().travelId);
  }
}

