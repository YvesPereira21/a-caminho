import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type StatusVariant = 'active' | 'warning' | 'info' | 'error' | 'neutral';

@Component({
  selector: 'app-status-badge',
  imports: [CommonModule],
  templateUrl: './status-badge.component.html',
  styleUrl: './status-badge.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StatusBadgeComponent {
  public readonly text = input.required<string>();
  public readonly variant = input<StatusVariant>('active');
  public readonly pulse = input<boolean>(false);

  public readonly badgeClasses = computed(() => {
    switch (this.variant()) {
      case 'active':
        return 'bg-emerald-50 text-emerald-700 border-emerald-200';
      case 'warning':
        return 'bg-amber-50 text-amber-700 border-amber-200';
      case 'error':
        return 'bg-rose-50 text-rose-700 border-rose-200';
      case 'info':
        return 'bg-blue-50 text-blue-700 border-blue-200';
      case 'neutral':
      default:
        return 'bg-slate-100 text-slate-700 border-slate-200';
    }
  });

  public readonly dotClasses = computed(() => {
    switch (this.variant()) {
      case 'active':
        return 'bg-emerald-500';
      case 'warning':
        return 'bg-amber-500';
      case 'error':
        return 'bg-rose-500';
      case 'info':
        return 'bg-blue-500';
      case 'neutral':
      default:
        return 'bg-slate-400';
    }
  });
}
