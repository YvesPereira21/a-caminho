export interface StudentCreate {
  studentName: string;
  email: string;
  password: string;
  cpf: string;
  registrationNumber?: string;
  courseName?: string;
  currentPeriod?: number;
  admissionDate?: string;
  municipalityId: string;
  universityId: string;
}

export interface Student {
  studentId: string;
  userId: string;
  studentName: string;
  email: string;
  cpf: string;
  registrationNumber?: string;
  courseName?: string;
  currentPeriod?: number;
  admissionDate?: string;
  municipalityId: string;
  universityId: string;
}

export interface StudentUpdate {
  studentName?: string;
  cpf?: string;
  registrationNumber?: string;
  courseName?: string;
  currentPeriod?: number;
  universityId?: string;
}
