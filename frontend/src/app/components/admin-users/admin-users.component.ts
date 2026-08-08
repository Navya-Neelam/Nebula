import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService, User, UserRole } from '../../services/auth.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-users.component.html'
})
export class AdminUsersComponent implements OnInit {
  private http = inject(HttpClient);
  authService = inject(AuthService);

  users = signal<User[]>([]);
  searchQuery = signal<string>('');
  selectedRoleFilter = signal<string>('ALL');
  isLoading = signal(true);
  toastMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  private apiUrl = 'http://localhost:8080/api/auth/users';

  filteredUsers = computed(() => {
    let list = this.users();
    const query = this.searchQuery().toLowerCase().trim();
    const roleFilter = this.selectedRoleFilter();

    if (roleFilter !== 'ALL') {
      list = list.filter(u => u.role === roleFilter);
    }

    if (query) {
      list = list.filter(u =>
        u.fullName?.toLowerCase().includes(query) ||
        u.email?.toLowerCase().includes(query)
      );
    }

    return list;
  });

  ngOnInit() {
    this.fetchUsers();
  }

  fetchUsers() {
    this.isLoading.set(true);
    this.http.get<User[]>(this.apiUrl).subscribe({
      next: (data) => {
        this.users.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        this.errorMessage.set(err.error?.message || 'Failed to load users.');
        this.isLoading.set(false);
      }
    });
  }

  onSearchChange(event: Event) {
    const val = (event.target as HTMLInputElement).value;
    this.searchQuery.set(val);
  }

  onRoleFilterChange(event: Event) {
    const val = (event.target as HTMLSelectElement).value;
    this.selectedRoleFilter.set(val);
  }

  updateUserRole(user: User, event: Event) {
    const newRole = (event.target as HTMLSelectElement).value as UserRole;
    if (!newRole || newRole === user.role) return;

    this.http.put(`${this.apiUrl}/${user.id}/role`, { role: newRole }).subscribe({
      next: () => {
        this.users.update(allUsers =>
          allUsers.map(u => u.id === user.id ? { ...u, role: newRole } : u)
        );
        this.showToast(`User "${user.fullName}" role updated to ${newRole}.`);
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Failed to update user role.');
        // Refresh to reset UI dropdown state if error occurred
        this.fetchUsers();
      }
    });
  }

  toggleUserStatus(user: User) {
    const nextStatus = !user.isActive;
    this.http.put(`${this.apiUrl}/${user.id}/status`, { active: nextStatus }).subscribe({
      next: () => {
        this.users.update(allUsers =>
          allUsers.map(u => u.id === user.id ? { ...u, isActive: nextStatus } : u)
        );
        this.showToast(`User "${user.fullName}" has been ${nextStatus ? 'activated' : 'deactivated'}.`);
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Failed to change user status.');
      }
    });
  }

  deleteUser(user: User) {
    if (!confirm(`Are you sure you want to delete user "${user.fullName}"? This action cannot be undone.`)) {
      return;
    }

    this.http.delete(`${this.apiUrl}/${user.id}`).subscribe({
      next: () => {
        this.users.update(allUsers => allUsers.filter(u => u.id !== user.id));
        this.showToast(`User "${user.fullName}" has been deleted.`);
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Failed to delete user.');
      }
    });
  }

  private showToast(msg: string) {
    this.toastMessage.set(msg);
    setTimeout(() => this.toastMessage.set(null), 3500);
  }
}
