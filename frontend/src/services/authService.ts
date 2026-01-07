import { api } from '../api/client';

export interface LoginRequest {
  login: string; // username ou email
  password: string;
}

export interface SignupRequest {
  name: string;
  username: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  id: string;
  username: string;
  name: string;
}

const TOKEN_KEY = 'oriontask_token';
const USER_KEY = 'oriontask_user';

class AuthService {
  async login(request: LoginRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/login', request);
    this.saveAuth(response);
    return response;
  }

  async signup(request: SignupRequest): Promise<AuthResponse> {
    const response = await api.post<AuthResponse>('/auth/signup', request);
    this.saveAuth(response);
    return response;
  }

  private saveAuth(response: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, response.token);
    localStorage.setItem(USER_KEY, JSON.stringify({
      id: response.id,
      username: response.username,
      name: response.name,
    }));
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  getUser(): { id: string; username: string; name: string } | null {
    const userStr = localStorage.getItem(USER_KEY);
    return userStr ? JSON.parse(userStr) : null;
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }
}

export const authService = new AuthService();
