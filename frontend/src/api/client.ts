const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

interface ApiError {
  message: string;
  status: number;
}

function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('oriontask_token');
  const headers: HeadersInit = {
    'Content-Type': 'application/json',
  };
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  
  return headers;
}

async function handleResponse<T>(response: Response): Promise<T> {
  const text = await response.text();
  
  if (!response.ok) {
    // Se 401, redirecionar para login
    if (response.status === 401) {
      localStorage.removeItem('oriontask_token');
      localStorage.removeItem('oriontask_user');
      window.location.href = '/login';
    }
    
    console.error(`API Error ${response.status}:`, text);
    const error: ApiError = {
      message: text || `Erro ${response.status}`,
      status: response.status,
    };
    throw error;
  }

  if (response.status === 204) {
    return null as T;
  }

  if (!text) {
    throw new Error('Resposta vazia do servidor');
  }

  try {
    return JSON.parse(text);
  } catch (e) {
    console.error('Erro ao parsear JSON:', text);
    throw new Error(`Resposta inválida do servidor: ${text.substring(0, 100)}`);
  }
}

export const api = {
  async get<T>(endpoint: string): Promise<T> {
    try {
      const response = await fetch(`${BASE_URL}${endpoint}`, {
        headers: getAuthHeaders(),
      });
      return handleResponse<T>(response);
    } catch (error) {
      console.error(`GET ${endpoint}:`, error);
      throw error;
    }
  },

  async post<T>(endpoint: string, data?: unknown): Promise<T> {
    try {
      console.log(`POST ${endpoint} com dados:`, data);
      const response = await fetch(`${BASE_URL}${endpoint}`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: data ? JSON.stringify(data) : undefined,
      });
      return handleResponse<T>(response);
    } catch (error) {
      console.error(`POST ${endpoint}:`, error);
      throw error;
    }
  },

  async patch<T>(endpoint: string, data?: unknown): Promise<T> {
    try {
      const response = await fetch(`${BASE_URL}${endpoint}`, {
        method: 'PATCH',
        headers: getAuthHeaders(),
        body: data ? JSON.stringify(data) : undefined,
      });
      return handleResponse<T>(response);
    } catch (error) {
      console.error(`PATCH ${endpoint}:`, error);
      throw error;
    }
  },

  async delete<T>(endpoint: string): Promise<T> {
    try {
      const response = await fetch(`${BASE_URL}${endpoint}`, {
        method: 'DELETE',
        headers: getAuthHeaders(),
      });
      return handleResponse<T>(response);
    } catch (error) {
      console.error(`DELETE ${endpoint}:`, error);
      throw error;
    }
  },
};
