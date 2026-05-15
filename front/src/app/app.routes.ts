import { Routes } from '@angular/router';
import { CardAdminPage } from './pages/card-admin/card-admin.page';
import { PlayerProfilePage } from './pages/player-profile/player-profile.page';

export const routes: Routes = [
  {
    path: '',
    component: PlayerProfilePage
  },
  {
    path: 'cards',
    component: CardAdminPage
  },
  {
    path: '**',
    redirectTo: ''
  }
];
