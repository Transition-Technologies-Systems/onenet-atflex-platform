import { TestBed } from '@angular/core/testing';
import { MockProviders } from 'ng-mocks';

import { AppComponent } from './app.component';
import { LanguageService, LoadingService, SessionStorageService } from './core';
import { TitleService } from './core/title/title.service';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [MockProviders(TitleService, LoadingService, LanguageService, SessionStorageService)],
      declarations: [AppComponent],
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });
});
