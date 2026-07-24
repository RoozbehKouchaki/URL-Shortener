import { Component, OnInit, inject } from '@angular/core';

import { LinksService } from '../../services/links.service';
import { Link } from '../../models/link';

@Component({
  selector: 'app-my-links',
  standalone: true,
  imports: [],
  templateUrl: './my-links.component.html',
  styleUrl: './my-links.component.css'
})
export class MyLinksComponent implements OnInit {
  private readonly links = inject(LinksService);
  private readonly deactivating = new Set<string>();

  items: Link[] = [];
  loading = false;
  error: string | null = null;

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading = true;
    this.error = null;

    this.links.listMine().subscribe({
      next: (items) => {
        this.loading = false;
        this.items = items;
      },
      error: () => {
        this.loading = false;
        this.error = 'Could not load your links. Please try again.';
      }
    });
  }

  isDeactivating(shortCode: string): boolean {
    return this.deactivating.has(shortCode);
  }

  deactivate(link: Link): void {
    if (this.deactivating.has(link.shortCode)) {
      return;
    }
    this.deactivating.add(link.shortCode);
    this.error = null;

    this.links.deactivate(link.shortCode).subscribe({
      next: () => {
        this.deactivating.delete(link.shortCode);
        this.reload();
      },
      error: () => {
        this.deactivating.delete(link.shortCode);
        this.error = 'Could not deactivate that link. Please try again.';
      }
    });
  }
}
