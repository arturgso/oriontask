import { api } from '../api/client';
import type { Dharma, CreateDharmaDTO } from '../types';

class DharmaService {
    async getByUser(userId: string, includeHidden: boolean = false): Promise<Dharma[]> {
        return api.get<Dharma[]>(`/dharma/user/${userId}?includeHidden=${includeHidden}`);
    }

    async create(userId: string, data: CreateDharmaDTO): Promise<Dharma> {
        return api.post<Dharma>(`/dharma/${userId}/create`, data);
    }

    async update(dharmaId: number, data: CreateDharmaDTO): Promise<Dharma> {
        return api.patch<Dharma>(`/dharma/edit/${dharmaId}`, data);
    }

    async toggleHidden(dharmaId: number): Promise<Dharma> {
        return api.patch<Dharma>(`/dharma/${dharmaId}/toggle-hidden`);
    }

    async delete(dharmaId: number): Promise<void> {
        return api.delete<void>(`/dharma/${dharmaId}`);
    }
}

export const dharmaService = new DharmaService();
