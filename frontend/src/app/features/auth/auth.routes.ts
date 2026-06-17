// Auth routes
import {Routes} from '@angular/router';

export const routes: Routes = [
    {
        path: 'login',
        loadComponent: () => import('./login/login').then(c => c.LoginComponent)
    },
    {
        path: 'register',
        loadComponent: () => import('./register/register').then(c => c.RegisterComponent)
    },
    {
        path: 'forgot-password',
        loadComponent: () => import('./forgot-password/forgot-password').then(c => c.ForgotPasswordComponent)
    },
    {
        path: 'reset-password',
        loadComponent: () => import('./reset-password/reset-password').then(c => c.ResetPasswordComponent)
    }
];
