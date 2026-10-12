import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { Student, StudentUpdate, UniversityResponse } from '../../../../core/models';
import { SidebarComponent } from '../../../../shared/components/sidebar/sidebar.component';
import { UniversityService } from '../../../universities/services/university.service';
import { UniversityStudentService } from '../../services/university-student.service';

@Component({
  selector: 'app-student-profile',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, SidebarComponent],
  templateUrl: './student-profile.component.html',
  styleUrl: './student-profile.component.css',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class StudentProfileComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly studentService = inject(UniversityStudentService);
  private readonly universityService = inject(UniversityService);

  // Estados com Signals
  public readonly student = signal<Student | null>(null);
  public readonly isLoading = signal<boolean>(true);
  public readonly isEditing = signal<boolean>(false);
  public readonly isSubmitting = signal<boolean>(false);
  public readonly errorMessage = signal<string | null>(null);
  public readonly successMessage = signal<string | null>(null);
  public readonly isMobileMenuOpen = signal<boolean>(false);

  // Lista de universidades disponíveis para caso o estudante queira alterar sua instituição
  public readonly availableUniversities = signal<UniversityResponse[]>([]);

  // Formulário Reativo para Edição
  public readonly profileForm = this.fb.group({
    studentName: ['', [Validators.required, Validators.minLength(3)]],
    cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.?\d{3}\.?\d{3}-?\d{2}$/)]],
    registrationNumber: [''],
    courseName: ['', [Validators.required]],
    currentPeriod: [null as number | null, [Validators.min(1), Validators.max(16)]],
    universityId: ['', [Validators.required]]
  });

  // Nome da universidade vinculada calculado
  public readonly currentUniversityName = computed(() => {
    const st = this.student();
    if (!st || !st.universityId) return 'Não informada';
    const univ = this.availableUniversities().find((u) => u.id === st.universityId);
    if (univ) {
      return univ.campus ? `${univ.name} (${univ.campus})` : univ.name;
    }
    return 'Universidade conveniada';
  });

  // Iniciais do estudante para o Avatar circular
  public readonly studentInitials = computed(() => {
    const st = this.student();
    const name = st?.studentName || this.authService.currentUser()?.name || '';
    if (!name.trim()) return 'ES';
    const parts = name.trim().split(/\s+/);
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
  });

  // Data de ingresso formatada
  public readonly formattedAdmissionDate = computed(() => {
    const dateStr = this.student()?.admissionDate;
    if (!dateStr) return 'Não informada';
    try {
      const [year, month, day] = dateStr.split('-');
      if (year && month && day) {
        return `${day}/${month}/${year}`;
      }
      return dateStr;
    } catch {
      return dateStr;
    }
  });

  // Período formatado
  public readonly formattedPeriod = computed(() => {
    const period = this.student()?.currentPeriod;
    if (!period) return 'Não informado';
    return `${period}º período`;
  });

  ngOnInit(): void {
    this.loadProfile();
    this.loadUniversities();
  }

  public loadProfile(): void {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    this.studentService.getMyProfile().subscribe({
      next: (studentData) => {
        this.student.set(studentData);
        this.populateForm(studentData);
        this.isLoading.set(false);
      },
      error: (err) => {
        const errorMsg =
          err?.error?.message ||
          'Não foi possível carregar os dados do seu perfil. Tente novamente mais tarde.';
        this.errorMessage.set(errorMsg);
        this.isLoading.set(false);
      }
    });
  }

  private loadUniversities(): void {
    this.universityService.getAllUniversityByNameFromState('', 'Paraíba').subscribe({
      next: (universities) => {
        this.availableUniversities.set(universities);
      },
      error: () => {
        this.universityService.getAllUniversityByNameFromState('', '').subscribe({
          next: (universities) => this.availableUniversities.set(universities),
          error: () => this.availableUniversities.set([])
        });
      }
    });
  }

  private populateForm(st: Student): void {
    this.profileForm.patchValue({
      studentName: st.studentName || '',
      cpf: st.cpf || '',
      registrationNumber: st.registrationNumber || '',
      courseName: st.courseName || '',
      currentPeriod: st.currentPeriod || null,
      universityId: st.universityId || ''
    });
  }

  public startEditing(): void {
    const current = this.student();
    if (current) {
      this.populateForm(current);
    }
    this.errorMessage.set(null);
    this.successMessage.set(null);
    this.isEditing.set(true);
  }

  public cancelEditing(): void {
    const current = this.student();
    if (current) {
      this.populateForm(current);
    }
    this.errorMessage.set(null);
    this.isEditing.set(false);
  }

  public onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    const currentStudent = this.student();
    if (!currentStudent?.studentId) {
      this.errorMessage.set('Identificador do estudante não encontrado.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const formValues = this.profileForm.getRawValue();
    const cleanCpf = (formValues.cpf || '').replace(/\D/g, '');

    const updateDTO: StudentUpdate = {
      studentName: (formValues.studentName || '').trim(),
      cpf: cleanCpf,
      registrationNumber: formValues.registrationNumber ? formValues.registrationNumber.trim() : undefined,
      courseName: (formValues.courseName || '').trim(),
      currentPeriod: formValues.currentPeriod || undefined,
      universityId: formValues.universityId || undefined
    };

    this.studentService.updateStudent(currentStudent.studentId, updateDTO).subscribe({
      next: () => {
        // Atualiza estado local
        this.student.update((prev) => {
          if (!prev) return null;
          return {
            ...prev,
            studentName: updateDTO.studentName || prev.studentName,
            cpf: updateDTO.cpf || prev.cpf,
            registrationNumber: updateDTO.registrationNumber,
            courseName: updateDTO.courseName,
            currentPeriod: updateDTO.currentPeriod,
            universityId: updateDTO.universityId || prev.universityId
          };
        });

        this.isSubmitting.set(false);
        this.isEditing.set(false);
        this.successMessage.set('Perfil atualizado com sucesso!');

        setTimeout(() => {
          this.successMessage.set(null);
        }, 5000);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const errorMsg =
          err?.error?.message ||
          'Erro ao salvar as alterações do perfil. Verifique os dados e tente novamente.';
        this.errorMessage.set(errorMsg);
      }
    });
  }

  public toggleMobileMenu(): void {
    this.isMobileMenuOpen.update((v) => !v);
  }

  public closeMobileMenu(): void {
    this.isMobileMenuOpen.set(false);
  }

  public logout(): void {
    this.authService.logout();
  }
}

