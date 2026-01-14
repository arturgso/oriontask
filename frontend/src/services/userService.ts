import { api } from '../api/client';
import type { User, CreateUserDTO } from '../types';

class UserService {
    async signup(data: CreateUserDTO): Promise<User> {
        return api.post<User>('/auth/signup', data);
    }

    async get(id: string): Promise<User> {
        return api.get<User>(`/users/${id}`);
    }

    async getByUsername(username: string): Promise<User> {
        return api.get<User>(`/users/username/${username}`);
    }
}

export const userService = new UserService();
