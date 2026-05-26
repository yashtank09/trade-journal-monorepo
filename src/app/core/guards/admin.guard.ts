import {Injectable} from '@angular/core';
import {CanActivate, Router} from '@angular/router';
import {AuthService} from '../../features/auth/auth.service';

@Injectable({
    providedIn: 'root'
})
export class AdminGuard implements CanActivate {
    constructor(
        private readonly authService: AuthService,
        private readonly router: Router
    ) {
    }

    canActivate(): boolean {
        const token = this.authService.getToken();

        if (!token) {
            this.router.navigate(['/auth/login']);
            return false;
        }

        // Check if role is admin
        const role = this.authService.getUserRole();
        if (role !== 'ROLE_ADMIN') {
            // Redirect unauthorized users to standard dashboard
            this.router.navigate(['/journal']);
            return false;
        }

        return true;
    }
}
