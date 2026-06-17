import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { UserProfileComponent } from './profile.component';
import { AuthService } from '../auth/auth.service';
import { ProfileService } from '../../core/services/profile.service';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { of } from 'rxjs';

describe('UserProfileComponent', () => {
  let component: UserProfileComponent;
  let fixture: ComponentFixture<UserProfileComponent>;
  let mockAuthService: any;
  let mockProfileService: any;

  beforeEach(async () => {
    mockAuthService = {
      getDecodedToken: vi.fn().mockReturnValue(null),
      getUserRole: vi.fn().mockReturnValue('ROLE_USER')
    };

    const mockUser = {
      username: 'trader_yash',
      email: 'yash@tradejournal.com',
      firstName: 'Yash',
      lastName: 'Sharma',
      mobileNumber: '9876543210',
      residentialAddress: '123 Wealth Avenue, Financial District',
      pan: 'ABCDE1234F',
      currency: 'INR',
      tradingStyle: 'Swing Trader',
      role: 'PRO_TRADER',
      joinedDate: 'June 2025'
    };

    mockProfileService = {
      getProfile: vi.fn().mockReturnValue(of({
        status: 'success',
        'status-code': 200,
        'status-message': 'Fetched successfully',
        data: mockUser
      })),
      updateProfile: vi.fn().mockImplementation((updatedData) => of({
        status: 'success',
        'status-code': 200,
        'status-message': 'Updated successfully',
        data: { ...mockUser, ...updatedData }
      }))
    };

    await TestBed.configureTestingModule({
      imports: [UserProfileComponent, ReactiveFormsModule],
      providers: [
        { provide: AuthService, useValue: mockAuthService },
        { provide: ProfileService, useValue: mockProfileService }
      ]
    }).compileComponents();

    // Clear local storage for clean test runs
    localStorage.clear();

    fixture = TestBed.createComponent(UserProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default user values', () => {
    expect(component.profileForm).toBeDefined();
    expect(component.profileForm.get('firstName')?.value).toBe('Yash');
    expect(component.profileForm.get('lastName')?.value).toBe('Sharma');
    expect(component.profileForm.get('email')?.value).toBe('yash@tradejournal.com');
    expect(component.profileForm.get('mobileNumber')?.value).toBe('9876543210');
    expect(component.profileForm.get('pan')?.value).toBe('ABCDE1234F');
    expect(component.profileForm.get('currency')?.value).toBe('INR');
    expect(component.profileForm.get('tradingStyle')?.value).toBe('Swing Trader');
    expect(component.profileForm.get('residentialAddress')?.value).toBe('123 Wealth Avenue, Financial District');
  });

  it('should have a disabled email field', () => {
    const emailControl = component.profileForm.get('email');
    expect(emailControl?.disabled).toBe(true);
  });

  it('should validate PAN format correctly', () => {
    const panControl = component.profileForm.get('pan');
    
    // Invalid PAN formats
    panControl?.setValue('ABCDE1234'); // Less than 10 characters
    panControl?.markAsTouched();
    expect(panControl?.valid).toBe(false);
    expect(component.getFieldError('pan')).toContain('Format must be ABCDE1234F');

    panControl?.setValue('12345ABCDE'); // Incorrect characters ordering
    expect(panControl?.valid).toBe(false);

    panControl?.setValue('ABCD12345F'); // Incorrect number of letters/digits
    expect(panControl?.valid).toBe(false);

    // Valid PAN formats
    panControl?.setValue('XYZPQ9876M');
    expect(panControl?.valid).toBe(true);

    panControl?.setValue('xyzpq9876m'); // Case-insensitivity support
    expect(panControl?.valid).toBe(true);
  });

  it('should validate Mobile Number correctly', () => {
    const mobileControl = component.profileForm.get('mobileNumber');

    // Invalid mobile numbers
    mobileControl?.setValue('12345'); // Too short
    expect(mobileControl?.valid).toBe(false);

    mobileControl?.setValue('5876543210'); // Doesn't start with 6-9
    expect(mobileControl?.valid).toBe(false);

    mobileControl?.setValue('abcde12345'); // Non-numeric
    expect(mobileControl?.valid).toBe(false);

    // Valid mobile numbers
    mobileControl?.setValue('9876543210');
    expect(mobileControl?.valid).toBe(true);

    mobileControl?.setValue('7890123456');
    expect(mobileControl?.valid).toBe(true);
  });

  it('should reset form back to original state on cancelEdit', () => {
    component.profileForm.patchValue({
      firstName: 'NewFirst',
      lastName: 'NewLast',
      mobileNumber: '8888888888',
      tradingStyle: 'Scalper'
    });

    component.cancelEdit();

    expect(component.profileForm.get('firstName')?.value).toBe('Yash');
    expect(component.profileForm.get('lastName')?.value).toBe('Sharma');
    expect(component.profileForm.get('mobileNumber')?.value).toBe('9876543210');
    expect(component.profileForm.get('tradingStyle')?.value).toBe('Swing Trader');
  });

  it('should save changes and update signal using ProfileService', () => {
    component.profileForm.patchValue({
      firstName: 'Harsh',
      lastName: 'Patel',
      mobileNumber: '9999999999',
      tradingStyle: 'Scalper',
      pan: 'ABCDE5678G',
      currency: 'USD',
      residentialAddress: '456 Bull Street, Trading Floor'
    });

    // Make the form dirty so button would be enabled
    component.profileForm.markAsDirty();

    component.saveChanges();

    // Since mock profile service returns synchronously via of(), the state updates immediately
    expect(component.isSaving()).toBe(false);
    expect(component.userProfile().firstName).toBe('Harsh');
    expect(component.userProfile().lastName).toBe('Patel');
    expect(component.userProfile().mobileNumber).toBe('9999999999');
    expect(component.userProfile().tradingStyle).toBe('Scalper');
    expect(component.userProfile().pan).toBe('ABCDE5678G');
    expect(component.userProfile().currency).toBe('USD');

    // Confirm it's stored in local storage
    const saved = localStorage.getItem('userProfile');
    expect(saved).toBeDefined();
    expect(JSON.parse(saved!).firstName).toBe('Harsh');
  });
});
