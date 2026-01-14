import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para adicionar o token de autorização
apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('oriontask_token');
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para tratamento de erros
apiClient.interceptors.response.use(
  (response) => response.data,
  (error: AxiosError) => {
    if (error.response) {
      // O servidor respondeu com um status fora do range 2xx
      if (error.response.status === 401) {
        localStorage.removeItem('oriontask_token');
        localStorage.removeItem('oriontask_user');
        if (window.location.pathname !== '/login') {
          window.location.href = '/login';
        }
      }

      const message = (error.response.data as { message?: string })?.message || `Erro ${error.response.status}`;
      console.error(`API Error ${error.response.status}:`, error.response.data);

      return Promise.reject({
        message,
        status: error.response.status,
      });
    } else if (error.request) {
      // A requisição foi feita mas não houve resposta
      console.error('API No Response:', error.request);
      return Promise.reject({
        message: 'Sem resposta do servidor',
        status: 503,
      });
    } else {
      // Algo aconteceu ao configurar a requisição
      console.error('API Request Error:', error.message);
      return Promise.reject({
        message: error.message,
        status: 500,
      });
    }
  }
);

export const api = {
  get: <T>(url: string) => apiClient.get<unknown, T>(url),
  post: <T>(url: string, data?: unknown) => apiClient.post<unknown, T>(url, data),
  put: <T>(url: string, data?: unknown) => apiClient.put<unknown, T>(url, data),
  patch: <T>(url: string, data?: unknown) => apiClient.patch<unknown, T>(url, data),
  delete: <T>(url: string) => apiClient.delete<unknown, T>(url),
};

