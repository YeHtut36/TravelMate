import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withFetch } from '@angular/common/http';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { en_US, provideNzI18n } from 'ng-zorro-antd/i18n';
import { provideNzNativeDateAdapter } from 'ng-zorro-antd/core/time';
import { provideNzIcons } from 'ng-zorro-antd/icon';
import {
  ApartmentOutline,
  AppstoreOutline,
  BankOutline,
  BarsOutline,
  CalendarOutline,
  CameraOutline,
  CheckOutline,
  CloseCircleOutline,
  CoffeeOutline,
  CompassOutline,
  DeleteOutline,
  EditOutline,
  EnvironmentOutline,
  GlobalOutline,
  LockOutline,
  LogoutOutline,
  MailOutline,
  PlusOutline,
  SearchOutline,
  ThunderboltOutline,
  UserOutline
} from '@ant-design/icons-angular/icons';
import { registerLocaleData } from '@angular/common';
import en from '@angular/common/locales/en';

registerLocaleData(en);

const icons = [
  UserOutline,
  CompassOutline,
  SearchOutline,
  CloseCircleOutline,
  AppstoreOutline,
  BarsOutline,
  EnvironmentOutline,
  CalendarOutline,
  EditOutline,
  DeleteOutline,
  PlusOutline,
  CheckOutline,
  BankOutline,
  CameraOutline,
  ThunderboltOutline,
  CoffeeOutline,
  ApartmentOutline,
  GlobalOutline,
  LogoutOutline,
  MailOutline,
  LockOutline
];

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withFetch()),
    provideAnimationsAsync(),
    provideNzI18n(en_US),
    provideNzNativeDateAdapter(),
    provideNzIcons(icons)
  ]
};
