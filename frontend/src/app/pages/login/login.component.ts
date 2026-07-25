import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  submitting = false;
  error: string | null = null;

  /** True once a control is invalid and the user has interacted with it. */
  isInvalid(controlName: 'username' | 'password'): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && control.touched;
  }

  onSubmit(): void {
    this.error = null;

    if (this.form.invalid || this.submitting) {
      // Reveal the per-field messages when required fields are still empty.
      this.form.markAllAsTouched();
      return;
    }

    const { username, password } = this.form.getRawValue();
    this.submitting = true;

    this.auth.login(username, password).subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.submitting = false;
        this.error =
          err.status === 401
            ? 'invalid username or password'
            : 'Unable to sign in right now. Please try again.';
      }
    });
  }
}
