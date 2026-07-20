import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../services/auth.service';
import { CreateLinkComponent } from '../create-link/create-link.component';
import { MyLinksComponent } from '../my-links/my-links.component';

/**
 * Dashboard shown after a successful sign-in. Hosts the create-link form and
 * the my-links table, and lets the user sign out.
 */
@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CreateLinkComponent, MyLinksComponent],
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
