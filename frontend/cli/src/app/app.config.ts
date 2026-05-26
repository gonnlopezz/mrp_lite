import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideToastr } from 'ngx-toastr';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { LOCALE_ID } from '@angular/core';

import { routes } from './app.routes';
import { provideHttpClient, withFetch } from '@angular/common/http';

registerLocaleData(localeEs);

export const appConfig: ApplicationConfig = {
  providers: [

    { provide: LOCALE_ID, useValue: 'es' },
    provideZoneChangeDetection({ eventCoalescing: true }), 
    provideRouter(routes),
    provideHttpClient(withFetch()),
    provideAnimations(), 
    provideToastr({
      timeOut: 3000,
      positionClass: 'toast-top-right',
      preventDuplicates: true,
      progressBar: true,
      toastClass: 'ngx-toastr mrp-custom-toast' // Nuestra clase personalizada
    }),
  ]
};
