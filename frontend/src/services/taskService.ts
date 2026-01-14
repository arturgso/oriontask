import { api } from '../api/client';
import type { Task, CreateTaskDTO, TaskStatus, Page } from '../types';

class TaskService {
    async create(dharmaId: number, data: CreateTaskDTO): Promise<Task> {
        return api.post<Task>(`/tasks/${dharmaId}/create`, data);
    }

    async update(taskId: number, data: CreateTaskDTO): Promise<Task> {
        return api.patch<Task>(`/tasks/edit/${taskId}`, data);
    }

    async moveToNow(taskId: number): Promise<Task> {
        return api.patch<Task>(`/tasks/${taskId}/move-to-now`);
    }

    async changeStatus(taskId: number, status: TaskStatus): Promise<Task> {
        return api.patch<Task>(`/tasks/${taskId}/change-status?status=${status}`);
    }

    async markDone(taskId: number): Promise<Task> {
        return api.patch<Task>(`/tasks/${taskId}/mark-done`);
    }

    async getByDharma(dharmaId: number, page: number = 0, size: number = 10): Promise<Page<Task>> {
        return api.get<Page<Task>>(`/tasks/dharma/${dharmaId}?page=${page}&size=${size}`);
    }

    async getByDharmaAndStatus(dharmaId: number, status: TaskStatus, page: number = 0, size: number = 10): Promise<Page<Task>> {
        return api.get<Page<Task>>(`/tasks/dharma/${dharmaId}/status/${status}?page=${page}&size=${size}`);
    }

    async getByUserAndStatus(userId: string, status: TaskStatus, page: number = 0, size: number = 10): Promise<Page<Task>> {
        return api.get<Page<Task>>(`/tasks/user/${userId}/status/${status}?page=${page}&size=${size}`);
    }

    async delete(taskId: number): Promise<void> {
        return api.delete<void>(`/tasks/${taskId}`);
    }
}

export const taskService = new TaskService();
