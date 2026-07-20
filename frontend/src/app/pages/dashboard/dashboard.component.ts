import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';

/**
 * Placeholder dashboard shown after a successful sign-in.
 *
 * <p>The create-link form (task 14) and the my-links table (task 15) are added
 * here later. A route guard that keeps unauthenticated users out is wired in
 * task 16; for now this simply confirms the sign-in flow works end to end.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [],
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
