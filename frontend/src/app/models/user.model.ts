export interface User {
  id: number;
  email: string;
  fullName: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
  createdAt?: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  fullName: string;
}
