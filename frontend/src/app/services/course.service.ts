import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Course {
  id?: string;
  title: string;
  description: string;
  imageUrl?: string;
  thumbnail?: string; // Maps to thumbnail field
  category: string;
  duration: string;
  rating?: number;
  instructor?: string;
  instructorId?: string;
  detailDescription?: string;
  price: number;
  status?: 'PUBLISHED' | 'DRAFT';
  createdDate?: string;
  updatedDate?: string;
}

export interface PaginatedCourseResponse {
  courses: Course[];
  currentPage: number;
  totalItems: number;
  totalPages: number;
}

@Injectable({
  providedIn: 'root'
})
export class CourseService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/courses';

  getCourses(queryParams?: {
    search?: string;
    category?: string;
    instructorId?: string;
    minPrice?: number;
    maxPrice?: number;
    minRating?: number;
    duration?: string;
    sortBy?: string;
    page?: number;
    size?: number;
  }): Observable<PaginatedCourseResponse> {
    let params = new HttpParams();
    
    if (queryParams) {
      Object.entries(queryParams).forEach(([key, value]) => {
        if (value !== undefined && value !== null && value !== '') {
          params = params.set(key, value.toString());
        }
      });
    }

    return this.http.get<PaginatedCourseResponse>(this.apiUrl, { params });
  }

  getCourseById(id: string): Observable<Course> {
    return this.http.get<Course>(`${this.apiUrl}/${id}`);
  }

  createCourse(course: Course): Observable<Course> {
    return this.http.post<Course>(this.apiUrl, course);
  }

  updateCourse(id: string, course: Course): Observable<Course> {
    return this.http.put<Course>(`${this.apiUrl}/${id}`, course);
  }

  deleteCourse(id: string): Observable<{ message: string }> {
    return this.http.delete<{ message: string }>(`${this.apiUrl}/${id}`);
  }
}
