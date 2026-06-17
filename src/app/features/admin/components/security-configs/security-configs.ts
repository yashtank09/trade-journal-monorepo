import { Component, signal, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TableModule } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { AdminSecurityService, SecurityEndpointConfig } from '../../services/admin-security.service';

@Component({
  selector: 'app-security-configs',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, TableModule],
  templateUrl: './security-configs.html',
  styleUrl: './security-configs.scss'
})
export class SecurityConfigsComponent implements OnInit {
  private readonly adminService = inject(AdminSecurityService);
  private readonly messageService = inject(MessageService);
  private readonly fb = inject(FormBuilder);

  configs = signal<SecurityEndpointConfig[]>([]);
  isLoading = signal<boolean>(false);
  isReloading = signal<boolean>(false);
  showModal = signal<boolean>(false);
  editingConfig = signal<SecurityEndpointConfig | null>(null);

  configForm!: FormGroup;

  readonly C = {
    bg: '#F7F5F0',
    surface: '#FFFFFF',
    border: '#E8E4DC',
    ink: '#1A1A1A',
    green: '#2D6A4F',
    red: '#C0392B',
    amber: '#D97706',
    indigo: '#4338CA'
  };

  ngOnInit() {
    this.initForm();
    this.loadConfigs();
  }

  private initForm() {
    this.configForm = this.fb.group({
      path: ['', [Validators.required, Validators.maxLength(500)]],
      accessLevel: ['PUBLIC', Validators.required],
      isActive: [true],
      description: ['', Validators.required]
    });
  }

  loadConfigs() {
    this.isLoading.set(true);
    this.adminService.getAll().subscribe({
      next: (res) => {
        if (res.status === 'success') {
          this.configs.set(res.data || []);
        } else {
          this.showError('Failed to fetch configurations.');
        }
        this.isLoading.set(false);
      },
      error: (err) => {
        this.showError(err.error?.message || 'Error loading configurations');
        this.isLoading.set(false);
      }
    });
  }

  openAddModal() {
    this.editingConfig.set(null);
    this.configForm.reset({
      path: '',
      accessLevel: 'PUBLIC',
      isActive: true,
      description: ''
    });
    this.showModal.set(true);
  }

  openEditModal(config: SecurityEndpointConfig) {
    this.editingConfig.set(config);
    this.configForm.patchValue({
      path: config.path,
      accessLevel: config.accessLevel,
      isActive: config.isActive,
      description: config.description
    });
    this.showModal.set(true);
  }

  closeModal() {
    this.showModal.set(false);
    this.editingConfig.set(null);
  }

  saveConfig() {
    if (this.configForm.invalid) {
      this.configForm.markAllAsTouched();
      return;
    }

    const payload = this.configForm.value as SecurityEndpointConfig;
    const editing = this.editingConfig();

    if (editing && editing.id) {
      this.adminService.update(editing.id, payload).subscribe({
        next: (res) => {
          if (res.status === 'success') {
            this.showSuccess('Configuration updated successfully.');
            this.loadConfigs();
            this.closeModal();
          } else {
            this.showError('Failed to update configuration.');
          }
        },
        error: (err) => this.showError(err.error?.message || 'Error updating configuration')
      });
    } else {
      this.adminService.create(payload).subscribe({
        next: (res) => {
          if (res.status === 'success') {
            this.showSuccess('Configuration created successfully.');
            this.loadConfigs();
            this.closeModal();
          } else {
            this.showError('Failed to create configuration.');
          }
        },
        error: (err) => this.showError(err.error?.message || 'Error creating configuration')
      });
    }
  }

  toggleActive(config: SecurityEndpointConfig) {
    if (!config.id) return;
    this.adminService.toggle(config.id).subscribe({
      next: (res) => {
        if (res.status === 'success') {
          this.showSuccess(`Endpoint ${config.path} is now ${res.data.isActive ? 'active' : 'inactive'}.`);
          this.loadConfigs();
        } else {
          this.showError('Failed to toggle status.');
        }
      },
      error: (err) => this.showError(err.error?.message || 'Error toggling status')
    });
  }

  deleteConfig(config: SecurityEndpointConfig) {
    if (!config.id || !confirm(`Are you sure you want to permanently delete rule for: ${config.path}?`)) return;
    this.adminService.delete(config.id).subscribe({
      next: (res) => {
        if (res.status === 'success') {
          this.showSuccess('Configuration deleted successfully.');
          this.loadConfigs();
        } else {
          this.showError('Failed to delete configuration.');
        }
      },
      error: (err) => this.showError(err.error?.message || 'Error deleting configuration')
    });
  }

  triggerReload() {
    this.isReloading.set(true);
    this.adminService.reload().subscribe({
      next: (res) => {
        if (res.status === 'success') {
          this.showSuccess('Dynamic endpoint security cache reloaded successfully on the server.');
        } else {
          this.showError('Failed to reload cache.');
        }
        this.isReloading.set(false);
      },
      error: (err) => {
        this.showError(err.error?.message || 'Error reloading security configurations');
        this.isReloading.set(false);
      }
    });
  }

  getAccessLevelCount(level: string): number {
    return this.configs().filter(c => c.accessLevel === level).length;
  }

  private showSuccess(msg: string) {
    this.messageService.add({
      severity: 'success',
      summary: 'Success',
      detail: msg
    });
  }

  private showError(msg: string) {
    this.messageService.add({
      severity: 'error',
      summary: 'Error',
      detail: msg
    });
  }
}
