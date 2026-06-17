import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

interface ForgotPasswordForm {
    email: FormControl<string | null>;
}

@Component({
    selector: 'app-forgot-password',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './forgot-password.html',
    styleUrl: './forgot-password.scss'
})
export class ForgotPasswordComponent {
    loading = signal(false);
    apiError = signal('');
    submitted = signal(false);

    forgotForm: FormGroup<ForgotPasswordForm>;

    constructor(
        private readonly fb: FormBuilder,
        private readonly router: Router,
        private readonly authService: AuthService
    ) {
        this.forgotForm = this.fb.group({
            email: ['', [Validators.required, Validators.email]]
        });
    }

    getControl(field: string) {
        return this.forgotForm.get(field);
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
            return 'Please enter your email';
        }

        if (control.errors['email']) {
            return 'Please enter a valid email address';
        }

        return '';
    }

    submit() {
        if (this.forgotForm.invalid) {
            this.forgotForm.markAllAsTouched();
            return;
        }

        this.loading.set(true);
        this.apiError.set('');

        const email = this.forgotForm.value.email!;

        this.authService.forgotPassword(email).subscribe({
            next: (result) => {
                if (result.status === 'success') {
                    this.submitted.set(true);
                } else {
                    this.apiError.set(result['status-message'] || 'An error occurred. Please try again.');
                }
            },
            error: (error) => {
                console.error('Forgot password error:', error);
                this.apiError.set(error.error?.message || 'An error occurred. Please try again.');
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
