import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './signup.component.html',
  // Reuses the login styles: same single-card form layout.
  styleUrl: '../login/login.component.css'
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  /** Mirrors the server-side constraints in SignupRequest. */
  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(30)]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  submitting = false;
  error: string | null = null;

  isInvalid(controlName: 'username' | 'password'): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && control.touched;
  }

  onSubmit(): void {
    this.error = null;

    if (this.form.invalid || this.submitting) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, password } = this.form.getRawValue();
    this.submitting = true;

    this.auth.signup(username, password).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.submitting = false;
        // 409 and 400 carry a caller-safe message from the backend.
        this.error =
          err.status === 409 || err.status === 400
            ? err.error?.message ?? 'Please check your details.'
            : 'Unable to sign up right now. Please try again.';
      }
    });
  }
}
