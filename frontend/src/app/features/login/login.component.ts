import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/services/auth.service';
import { UserRole } from '../../core/models';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  public readonly isLoading = signal<boolean>(false);
  public readonly showPassword = signal<boolean>(false);
  public readonly errorMessage = signal<string | null>(null);

  public readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  public togglePasswordVisibility(): void {
    this.showPassword.update((val) => !val);
  }

  public onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const credentials = this.form.getRawValue();

    this.authService.login(credentials).subscribe({
      next: (response) => {
        this.isLoading.set(false);
        this.redirectByUserRole(response.user?.role);
      },
      error: (err) => {
        this.isLoading.set(false);
        const serverMsg = err.error?.message;
        if (typeof serverMsg === 'string' && serverMsg.trim()) {
          this.errorMessage.set(serverMsg);
        } else if (err.status === 401) {
          this.errorMessage.set('E-mail ou senha incorretos.');
        } else {
          this.errorMessage.set('Não foi possível conectar ao servidor. Tente novamente mais tarde.');
        }
      }
    });
  }

  private redirectByUserRole(role?: UserRole | string): void {
    const roleUpper = role ? String(role).toUpperCase() : '';

    if (roleUpper === 'STUDENT' || roleUpper === 'ESTUDANTE') {
      this.router.navigate(['/student']);
    } else if (roleUpper === 'BUS_DRIVER' || roleUpper === 'MOTORISTA') {
      this.router.navigate(['/driver']);
    } else if (
      roleUpper === 'ADMIN' ||
      roleUpper === 'ADMINISTRADOR' ||
      roleUpper === 'MUNICIPALITY' ||
      roleUpper === 'PREFEITURA'
    ) {
      this.router.navigate(['/admin']);
    } else {
      this.router.navigate(['/']);
    }
  }

  get emailControl() {
    return this.form.controls.email;
  }

  get passwordControl() {
    return this.form.controls.password;
  }
}
