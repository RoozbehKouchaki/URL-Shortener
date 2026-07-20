import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { CreateLinkComponent } from '../create-link/create-link.component';

/**
 * Dashboard shown after a successful sign-in. Hosts the create-link form; the
 * my-links table is added here later. A route guard that keeps
 * unauthenticated users out is wired in task 16.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CreateLinkComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly username = this.auth.getUsername();

  signOut(): void {
    this.auth.clear();
    this.router.navigate(['/login']);
  }
}
