import { Component, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

/**
 * Sign-in form for the fixed set of Demo User accounts.
 *
 * <p>Basic auth has no dedicated login endpoint, so submitting the form
 * validates the credentials by making an authenticated call to
 * {@code GET /api/links}: HTTP 200 means the credentials are good (store them
 * and route to the dashboard), while HTTP 401 means they are not (stay on the
 * form and show a message). The credentials are stored tentatively before the
 * call so the interceptor attaches them, and cleared again if validation fails.
 */
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  submitting = false;
  error: string | null = null;

  onSubmit(): void {
    if (this.form.invalid || this.submitting) {
      this.form.markAllAsTouched();
      return;
    }

    const { username, password } = this.form.getRawValue();

    // Store tentatively so the interceptor attaches the Basic header to the
    // validation call below; clear again if the credentials turn out invalid.
    this.auth.setCredentials(username, password);
    this.submitting = true;
    this.error = null;

    this.http.get('/api/links').subscribe({
      next: () => {
        this.submitting = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.submitting = false;
        this.auth.clear();
        this.error =
          err.status === 401
            ? 'invalid username or password'
            : 'Unable to sign in right now. Please try again.';
      }
    });
  }
}
