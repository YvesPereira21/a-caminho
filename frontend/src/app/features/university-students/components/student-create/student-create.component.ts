import { Component, inject, OnInit, signal } from '@angular/core';
import { AbstractControl, FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { catchError, distinctUntilChanged, filter, forkJoin, of, switchMap, tap } from 'rxjs';
import { MunicipalityResponse, State, StudentCreate, UniversityResponse } from '../../../../core/models';
import { MunicipalityService } from '../../../municipalities/services/municipality.service';
import { StateService } from '../../../states/services/state.service';
import { UniversityService } from '../../../universities/services/university.service';
import { UniversityStudentService } from '../../services/university-student.service';

const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (password && confirmPassword && password !== confirmPassword) {
    return { passwordsMismatch: true };
  }
  return null;
};

@Component({
  selector: 'app-student-create',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './student-create.component.html',
  styleUrl: './student-create.component.css'
})
export class StudentCreateComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly studentService = inject(UniversityStudentService);
  private readonly stateService = inject(StateService);
  private readonly municipalityService = inject(MunicipalityService);
  private readonly universityService = inject(UniversityService);
  private readonly router = inject(Router);

  public readonly isLoading = signal<boolean>(false);
  public readonly showPassword = signal<boolean>(false);
  public readonly showConfirmPassword = signal<boolean>(false);
  public readonly errorMessage = signal<string | null>(null);
  public readonly successMessage = signal<string | null>(null);

  public readonly states = signal<State[]>([]);
  public readonly availableUniversities = signal<UniversityResponse[]>([]);
  public readonly availableMunicipalities = signal<MunicipalityResponse[]>([]);
  public readonly isLoadingStates = signal<boolean>(false);
  public readonly isLoadingLocations = signal<boolean>(false);

  public readonly form = this.fb.group(
    {
      studentName: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      cpf: ['', [Validators.required, Validators.pattern(/^\d{3}\.?\d{3}\.?\d{3}-?\d{2}$/)]],
      registrationNumber: [''],
      courseName: ['', [Validators.required]],
      currentPeriod: [null as number | null, [Validators.min(1), Validators.max(16)]],
      stateName: ['', [Validators.required]],
      municipalityId: ['', [Validators.required]],
      universityId: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    },
    { validators: [passwordMatchValidator] }
  );

  ngOnInit(): void {
    this.loadStates();
    this.setupStateChangeSubscription();
  }

  private loadStates(): void {
    this.isLoadingStates.set(true);
    this.stateService.getAllState().pipe(
      catchError(() => of([] as State[]))
    ).subscribe((stateList) => {
      this.states.set(stateList);
      this.isLoadingStates.set(false);
    });
  }

  private setupStateChangeSubscription(): void {
    this.form.controls.stateName.valueChanges.pipe(
      distinctUntilChanged(),
      tap((stateName) => {
        this.form.controls.municipalityId.reset('');
        this.form.controls.universityId.reset('');
        this.availableMunicipalities.set([]);
        this.availableUniversities.set([]);

        if (!stateName) {
          this.isLoadingLocations.set(false);
        }
      }),
      filter((stateName): stateName is string => !!stateName),
      tap(() => this.isLoadingLocations.set(true)),
      switchMap((stateName) =>
        forkJoin({
          universities: this.universityService.getAllUniversityByNameFromState('', stateName).pipe(
            catchError(() => of([] as UniversityResponse[]))
          ),
          municipalities: this.municipalityService.getAllMunicipalityFromState(stateName).pipe(
            catchError(() => of([] as MunicipalityResponse[]))
          )
        })
      )
    ).subscribe({
      next: ({ universities, municipalities }) => {
        this.availableUniversities.set(universities);
        this.availableMunicipalities.set(municipalities);
        this.isLoadingLocations.set(false);
      },
      error: () => {
        this.isLoadingLocations.set(false);
      }
    });
  }

  public togglePasswordVisibility(): void {
    this.showPassword.update((val) => !val);
  }

  public toggleConfirmPasswordVisibility(): void {
    this.showConfirmPassword.update((val) => !val);
  }

  public onSubmit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);
    this.successMessage.set(null);

    const val = this.form.getRawValue();
    const cleanCpf = (val.cpf || '').replace(/\D/g, '');

    const request: StudentCreate = {
      studentName: (val.studentName || '').trim(),
      email: (val.email || '').trim().toLowerCase(),
      cpf: cleanCpf,
      password: val.password || '',
      registrationNumber: val.registrationNumber?.trim() || undefined,
      courseName: val.courseName?.trim() || undefined,
      currentPeriod: val.currentPeriod ? Number(val.currentPeriod) : undefined,
      municipalityId: val.municipalityId || '',
      universityId: val.universityId || ''
    };

    this.studentService.registerStudent(request).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.successMessage.set('Cadastro realizado com sucesso! Redirecionando para o login...');
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1800);
      },
      error: (err) => {
        this.isLoading.set(false);
        const serverMsg = err.error?.message;
        if (typeof serverMsg === 'string' && serverMsg.trim()) {
          this.errorMessage.set(serverMsg);
        } else if (err.status === 409) {
          this.errorMessage.set('Já existe um estudante cadastrado com este e-mail ou CPF.');
        } else if (err.status === 400) {
          this.errorMessage.set('Dados inválidos. Verifique os campos informados.');
        } else {
          this.errorMessage.set('Erro ao realizar cadastro. Tente novamente mais tarde.');
        }
      }
    });
  }

  get nameCtrl() { return this.form.controls.studentName; }
  get emailCtrl() { return this.form.controls.email; }
  get cpfCtrl() { return this.form.controls.cpf; }
  get registrationCtrl() { return this.form.controls.registrationNumber; }
  get courseCtrl() { return this.form.controls.courseName; }
  get periodCtrl() { return this.form.controls.currentPeriod; }
  get stateCtrl() { return this.form.controls.stateName; }
  get municipalityCtrl() { return this.form.controls.municipalityId; }
  get universityCtrl() { return this.form.controls.universityId; }
  get passwordCtrl() { return this.form.controls.password; }
  get confirmPasswordCtrl() { return this.form.controls.confirmPassword; }

  get hasPasswordMismatch(): boolean {
    return (
      this.form.hasError('passwordsMismatch') &&
      this.confirmPasswordCtrl.touched &&
      !this.confirmPasswordCtrl.hasError('required')
    );
  }
}
