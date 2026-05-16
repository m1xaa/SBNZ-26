import { Routes } from '@angular/router';
import { CardAdminPage } from './pages/card-admin/card-admin.page';
import { DeckRecommendationPage } from './pages/deck-recommendation/deck-recommendation.page';
import { PlayerProfilePage } from './pages/player-profile/player-profile.page';
import { StaticKnowledgePage } from './pages/static-knowledge/static-knowledge.page';

export const routes: Routes = [
  {
    path: '',
    component: PlayerProfilePage
  },
  {
    path: 'recommend',
    component: DeckRecommendationPage
  },
  {
    path: 'static',
    component: StaticKnowledgePage
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
