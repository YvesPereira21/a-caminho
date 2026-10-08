import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Student, StudentCreate, StudentUpdate } from '../../../core/models';

@Injectable({
  providedIn: 'root'
})
export class UniversityStudentService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/university-students';

  registerStudent(request: StudentCreate): Observable<Student> {
    return this.http.post<Student>(this.apiUrl, request);
  }

  getStudent(studentId: string): Observable<Student> {
    return this.http.get<Student>(`${this.apiUrl}/${studentId}`);
  }

  updateStudent(studentId: string, updateDTO: StudentUpdate): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${studentId}`, updateDTO);
  }

  deleteStudentAccount(studentId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${studentId}`);
  }
}
