import {Routes} from '@angular/router';
import {SecurityConfigsComponent} from './components/security-configs/security-configs';

export const routes: Routes = [
    {
        path: '',
        redirectTo: 'security',
        pathMatch: 'full'
    },
    {
        path: 'security',
        component: SecurityConfigsComponent
    }
];
