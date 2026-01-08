import { api } from './client';
import type {
  User,
  Dharma,
  Task,
  CreateUserDTO,
  CreateDharmaDTO,
  CreateTaskDTO,
  TaskStatus,
  Page,
} from '../types';

export const usersApi = {
  signup: (data: CreateUserDTO) => api.post<User>('/auth/signup', data),
  get: (id: string) => api.get<User>(`/users/${id}`),
  getByUsername: (username: string) => api.get<User>(`/users/username/${username}`),
};

export const dharmaApi = {
  getByUser: (userId: string, includeHidden: boolean = false) =>
    api.get<Dharma[]>(`/dharma/user/${userId}?includeHidden=${includeHidden}`),
  create: (userId: string, data: CreateDharmaDTO) =>
    api.post<Dharma>(`/dharma/${userId}/create`, data),
  update: (dharmaId: number, data: CreateDharmaDTO) =>
    api.patch<Dharma>(`/dharma/edit/${dharmaId}`, data),
  toggleHidden: (dharmaId: number) =>
    api.patch<Dharma>(`/dharma/${dharmaId}/toggle-hidden`),
  delete: (dharmaId: number) => api.delete<void>(`/dharma/${dharmaId}`),
};

export const tasksApi = {
  create: (dharmaId: number, data: CreateTaskDTO) =>
    api.post<Task>(`/tasks/${dharmaId}/create`, data),
  update: (taskId: number, data: CreateTaskDTO) =>
    api.patch<Task>(`/tasks/edit/${taskId}`, data),
  moveToNow: (taskId: number) => api.patch<Task>(`/tasks/${taskId}/move-to-now`),
  changeStatus: (taskId: number, status: TaskStatus) =>
    api.patch<Task>(`/tasks/${taskId}/change-status?status=${status}`),
  markDone: (taskId: number) => api.patch<Task>(`/tasks/${taskId}/mark-done`),
  getByDharma: (dharmaId: number, page: number = 0, size: number = 10) =>
    api.get<Page<Task>>(`/tasks/dharma/${dharmaId}?page=${page}&size=${size}`),
  getByDharmaAndStatus: (dharmaId: number, status: TaskStatus, page: number = 0, size: number = 10) =>
    api.get<Page<Task>>(`/tasks/dharma/${dharmaId}/status/${status}?page=${page}&size=${size}`),
  getByUserAndStatus: (userId: string, status: TaskStatus, page: number = 0, size: number = 10) =>
    api.get<Page<Task>>(`/tasks/user/${userId}/status/${status}?page=${page}&size=${size}`),
  delete: (taskId: number) => api.delete<void>(`/tasks/${taskId}`),
};
