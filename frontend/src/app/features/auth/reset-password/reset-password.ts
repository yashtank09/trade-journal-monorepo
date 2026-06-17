import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../auth.service';

interface ResetPasswordForm {
    newPassword: FormControl<string | null>;
    confirmPassword: FormControl<string | null>;
}

export const passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const newPassword = control.get('newPassword');
    const confirmPassword = control.get('confirmPassword');

    if (!newPassword || !confirmPassword) return null;
    return newPassword.value === confirmPassword.value ? null : { passwordMismatch: true };
};

@Component({
    selector: 'app-reset-password',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './reset-password.html',
    styleUrl: './reset-password.scss'
})
export class ResetPasswordComponent implements OnInit {
    loading = signal(false);
    apiError = signal('');
    success = signal(false);
    showPassword = false;
    showConfirmPassword = false;
    token = '';

    resetForm: FormGroup<ResetPasswordForm>;

    constructor(
        private readonly fb: FormBuilder,
        private readonly route: ActivatedRoute,
        private readonly router: Router,
        private readonly authService: AuthService
    ) {
        this.resetForm = this.fb.group({
            newPassword: ['', [Validators.required, Validators.minLength(8)]],
            confirmPassword: ['', [Validators.required, Validators.minLength(8)]]
        }, { validators: passwordMatchValidator }) as FormGroup<ResetPasswordForm>;
    }

    ngOnInit() {
        this.token = this.route.snapshot.queryParams['token'] || '';
        if (!this.token) {
            this.apiError.set('Reset token is missing. Please request a new password reset link.');
            this.resetForm.disable();
        }
    }

    getControl(field: string) {
        return this.resetForm.get(field);
    }

    isFieldInvalid(field: string): boolean {
        const control = this.getControl(field);
        return !!(control?.invalid && control?.touched);
    }

    getErrorMessage(field: string): string {
        const control = this.getControl(field);

        if (!control?.errors || (!control.touched && !control.dirty)) {
            return '';
        }

        if (control.errors['required']) {
            return 'Please enter your ' + (field === 'newPassword' ? 'new password' : 'password confirmation');
        }

        if (control.errors['minlength']) {
            return `Password must be at least ${control.errors['minlength'].requiredLength} characters`;
        }

        return '';
    }

    isMismatchError(): boolean {
        return this.resetForm.hasError('passwordMismatch') && 
               !!(this.getControl('confirmPassword')?.touched || this.getControl('confirmPassword')?.dirty);
    }

    submit() {
        if (this.resetForm.invalid) {
            this.resetForm.markAllAsTouched();
            return;
        }

        if (!this.token) {
            this.apiError.set('Cannot reset password without a valid token.');
            return;
        }

        this.loading.set(true);
        this.apiError.set('');

        const newPassword = this.resetForm.value.newPassword!;

        this.authService.resetPassword(this.token, newPassword).subscribe({
            next: (result) => {
                if (result.status === 'success') {
                    this.success.set(true);
                } else {
                    this.apiError.set(result['status-message'] || 'Failed to reset password. The link may have expired.');
                }
            },
            error: (error) => {
                console.error('Reset password error:', error);
                this.apiError.set(error.error?.message || 'An error occurred. Please request a new link.');
                this.loading.set(false);
            },
            complete: () => {
                this.loading.set(false);
            }
        });
    }

    goToLogin() {
        this.router.navigate(['/auth/login']);
    }
}
