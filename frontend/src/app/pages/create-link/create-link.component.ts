import { Component, EventEmitter, Output, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { LinksService } from '../../services/links.service';
import { Link } from '../../models/link';
import { ApiError } from '../../models/api-error';

/**
 * Create-link form: a single Long URL input plus a submit button.
 *
 * <p>On success it shows the returned Short URL (with a copy button) and emits a
 * {@code created} event so the parent can refresh its list. On an HTTP 400 it
 * shows the backend's human-readable validation message (for example a
 * malformed address or a blocked host); other failures show a generic message.
 *
 * <p>Format and blocked-host checks are left to the backend so its validation
 * message is the single source of truth; only presence is
 * enforced on the client.
 */
@Component({
  selector: 'app-create-link',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './create-link.component.html',
  styleUrl: './create-link.component.css'
})
export class CreateLinkComponent {
  private readonly fb = inject(FormBuilder);
  private readonly links = inject(LinksService);

  /** Emitted after a successful create so the parent can refresh its list. */
  @Output() created = new EventEmitter<Link>();

  readonly form = this.fb.nonNullable.group({
    longUrl: ['', Validators.required]
  });

  submitting = false;
  result: Link | null = null;
  error: string | null = null;
  copied = false;

  onSubmit(): void {
    if (this.form.invalid || this.submitting) {
      this.form.markAllAsTouched();
      return;
    }

    const longUrl = this.form.getRawValue().longUrl.trim();
    this.submitting = true;
    this.error = null;
    this.result = null;
    this.copied = false;

    this.links.create(longUrl).subscribe({
      next: (link) => {
        this.submitting = false;
        this.result = link;
        this.form.reset({ longUrl: '' });
        this.created.emit(link);
      },
      error: (err: HttpErrorResponse) => {
        this.submitting = false;
        const apiError = err.error as ApiError | null;
        this.error =
          err.status === 400 && apiError?.message
            ? apiError.message
            : 'Could not shorten that link. Please try again.';
      }
    });
  }

  async copy(): Promise<void> {
    if (!this.result) {
      return;
    }
    try {
      await navigator.clipboard.writeText(this.result.shortUrl);
      this.copied = true;
    } catch {
      // Clipboard access can be blocked (for example a non-secure context);
      // the Short URL is still shown for manual copying.
    }
  }
}
