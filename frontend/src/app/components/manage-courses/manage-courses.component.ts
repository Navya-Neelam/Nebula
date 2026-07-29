import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CourseService, Course } from '../../services/course.service';

@Component({
  selector: 'app-manage-courses',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './manage-courses.component.html'
})
export class ManageCoursesComponent implements OnInit {
  authService = inject(AuthService);
  private courseService = inject(CourseService);
  private fb = inject(FormBuilder);

  courses = signal<Course[]>([]);
  isLoading = signal(true);
  toastMessage = signal<string | null>(null);

  // Modal Control Signals
  showCourseModal = signal(false);
  isEditMode = signal(false);
  currentCourseId = signal<string | null>(null);
  isSubmitting = signal(false);

  courseForm = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(100)]],
    description: ['', [Validators.required, Validators.maxLength(300)]],
    detailDescription: ['', [Validators.required, Validators.maxLength(2000)]],
    category: ['Generative AI', [Validators.required]],
    price: [29.99, [Validators.required, Validators.min(0)]],
    duration: ['', [Validators.required, Validators.pattern(/^\d+\s+(hours|mins|days)$/)]],
    thumbnail: ['', [Validators.required]],
    status: ['PUBLISHED' as 'PUBLISHED' | 'DRAFT', [Validators.required]]
  });

  categories = [
    'Generative AI',
    'Data Science',
    'Web Development',
    'Backend Development',
    'Cloud Computing',
    'Cybersecurity',
    'Mobile Development',
    'Digital Marketing'
  ];

  ngOnInit() {
    this.fetchCourses();
  }

  fetchCourses() {
    this.isLoading.set(true);
    const user = this.authService.currentUser();
    const queryParams: any = {};
    
    // Instructors only see their own courses
    if (user && user.role === 'INSTRUCTOR') {
      queryParams.instructorId = user.id;
    }

    this.courseService.getCourses(queryParams).subscribe({
      next: (res) => {
        this.courses.set(res.courses);
        this.isLoading.set(false);
      },
      error: () => {
        this.showToast('Failed to load courses.');
        this.isLoading.set(false);
      }
    });
  }

  openAddModal() {
    this.isEditMode.set(false);
    this.currentCourseId.set(null);
    this.courseForm.reset({
      category: 'Generative AI',
      price: 29.99,
      status: 'PUBLISHED'
    });
    this.showCourseModal.set(true);
  }

  openEditModal(course: Course) {
    this.isEditMode.set(true);
    this.currentCourseId.set(course.id || null);
    this.courseForm.patchValue({
      title: course.title,
      description: course.description,
      detailDescription: course.detailDescription,
      category: course.category,
      price: course.price,
      duration: course.duration,
      thumbnail: course.thumbnail || course.imageUrl,
      status: course.status || 'PUBLISHED'
    });
    this.showCourseModal.set(true);
  }

  onSubmit() {
    if (this.courseForm.invalid) {
      this.courseForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const formValue = this.courseForm.value as any;
    const coursePayload: Course = {
      title: formValue.title,
      description: formValue.description,
      detailDescription: formValue.detailDescription,
      category: formValue.category,
      price: formValue.price,
      duration: formValue.duration,
      thumbnail: formValue.thumbnail,
      imageUrl: formValue.thumbnail, // keep both fields in sync
      status: formValue.status
    };

    if (this.isEditMode()) {
      const id = this.currentCourseId();
      if (id) {
        this.courseService.updateCourse(id, coursePayload).subscribe({
          next: (updated) => {
            this.isSubmitting.set(false);
            this.showCourseModal.set(false);
            this.showToast('Course updated successfully!');
            this.fetchCourses();
          },
          error: (err) => {
            this.isSubmitting.set(false);
            this.showToast(err.error?.message || 'Failed to update course.');
          }
        });
      }
    } else {
      this.courseService.createCourse(coursePayload).subscribe({
        next: (created) => {
          this.isSubmitting.set(false);
          this.showCourseModal.set(false);
          this.showToast('Course created successfully!');
          this.fetchCourses();
        },
        error: (err) => {
          this.isSubmitting.set(false);
          this.showToast(err.error?.message || 'Failed to create course.');
        }
      });
    }
  }

  deleteCourse(course: Course) {
    if (!course.id) return;
    if (!confirm(`Are you sure you want to delete course "${course.title}"?`)) {
      return;
    }

    this.courseService.deleteCourse(course.id).subscribe({
      next: () => {
        this.showToast('Course deleted successfully.');
        this.fetchCourses();
      },
      error: (err) => {
        this.showToast(err.error?.message || 'Failed to delete course.');
      }
    });
  }

  isFieldInvalid(fieldName: string): boolean {
    const control = this.courseForm.get(fieldName);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  private showToast(msg: string) {
    this.toastMessage.set(msg);
    setTimeout(() => this.toastMessage.set(null), 3500);
  }
}
