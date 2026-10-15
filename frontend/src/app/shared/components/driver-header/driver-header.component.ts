import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-driver-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './driver-header.component.html',
  styleUrl: './driver-header.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DriverHeaderComponent {
  public readonly driverName = input<string>('Motorista');
  public readonly email = input<string>('');

  public readonly logout = output<void>();

  public onLogout(): void {
    this.logout.emit();
  }
}

