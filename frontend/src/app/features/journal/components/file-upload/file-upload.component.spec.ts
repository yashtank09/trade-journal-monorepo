import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FileUploadComponent } from './file-upload.component';
import { FileUploadService } from '../../services/file-upload.service';
import { MessageService } from 'primeng/api';
import { vi } from 'vitest';

describe('FileUploadComponent', () => {
  let component: FileUploadComponent;
  let fixture: ComponentFixture<FileUploadComponent>;
  let mockFileUploadService: any;

  beforeEach(async () => {
    const spy = {
      uploadFile: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [FileUploadComponent, ReactiveFormsModule, HttpClientTestingModule],
      providers: [
        { provide: FileUploadService, useValue: spy },
        { provide: MessageService, useValue: { add: vi.fn() } }
      ]
    }).compileComponents();

    mockFileUploadService = TestBed.inject(FileUploadService);
    fixture = TestBed.createComponent(FileUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.uploadForm).toBeDefined();
    expect(component.uploadForm.get('fileType')?.value).toBe('CSV');
    expect(component.uploadForm.get('fileCategory')?.value).toBe('TRADE_BOOK');
  });

  it('should be invalid when no file is selected', () => {
    expect(component.uploadForm.invalid).toBeTruthy();
  });

  it('should update selected file when file is chosen', () => {
    const mockFile = new File(['test'], 'test.csv', { type: 'text/csv' });
    const mockEvent = {
      target: {
        files: [mockFile]
      }
    } as unknown as Event;

    component.onFileSelect(mockEvent);

    expect(component.selectedFile).toBe(mockFile);
    expect(component.fileName).toBe('test.csv');
    expect(component.uploadForm.get('file')?.value).toBe(mockFile);
  });

  it('should reset form correctly', () => {
    // Set some values
    component.selectedFile = new File(['test'], 'test.csv', { type: 'text/csv' });
    component.uploadForm.patchValue({
      fileType: 'EXCEL',
      description: 'Test description',
      fileCategory: 'PORTFOLIO'
    });

    component.resetForm();

    expect(component.selectedFile).toBeNull();
    expect(component.uploadForm.get('fileType')?.value).toBe('CSV');
    expect(component.uploadForm.get('description')?.value).toBe('');
    expect(component.uploadForm.get('fileCategory')?.value).toBe('TRADE_BOOK');
  });

  it('should not submit when form is invalid', () => {
    vi.spyOn(component, 'onSubmit');
    
    component.uploadForm.markAllAsTouched();
    component.onSubmit();

    expect(mockFileUploadService.uploadFile).not.toHaveBeenCalled();
  });

  it('should submit when form is valid', () => {
    const mockFile = new File(['test'], 'test.csv', { type: 'text/csv' });
    component.selectedFile = mockFile;
    component.uploadForm.patchValue({
      file: mockFile,
      fileType: 'CSV',
      description: 'Test Upload',
      fileCategory: 'TRADE_BOOK'
    });

    mockFileUploadService.uploadFile.mockReturnValue({
      subscribe: () => {}
    } as any);

    component.onSubmit();

    expect(mockFileUploadService.uploadFile).toHaveBeenCalledWith(mockFile, {
      'file-type': 'CSV',
      'source-system': 'USER_INTERFACE',
      description: 'Test Upload',
      'file-category': 'TRADE_BOOK'
    });
  });
});
