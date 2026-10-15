import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-prepare-travel-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './prepare-travel-card.component.html',
  styleUrl: './prepare-travel-card.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PrepareTravelCardComponent {
  public readonly hasActiveTravel = input<boolean>(false);
  public readonly availableBusesCount = input<number>(0);

  public readonly prepare = output<void>();

  public onPrepare(): void {
    if (!this.hasActiveTravel()) {
      this.prepare.emit();
    }
  }
}

