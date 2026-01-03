import { create } from 'zustand';
import type { User, Dharma, Task, CreateDharmaDTO, CreateTaskDTO, TaskStatus } from '../types';
import { usersApi, dharmaApi, tasksApi } from '../api';

interface AppState {
  user: User | null;
  dharmas: Dharma[];
  tasks: Task[];
  theme: 'light' | 'dark';
  hydrated: boolean;
  setUser: (user: User | null) => void;
  loadUserFromStorage: () => Promise<void>;
  loadTheme: () => void;
  toggleTheme: () => void;
  logout: () => void;
  
  // Dharma actions
  fetchDharmas: (userId: string) => Promise<void>;
  createDharma: (userId: string, dto: CreateDharmaDTO) => Promise<void>;
  deleteDharma: (dharmaId: number) => Promise<void>;
  
  // Task actions
  fetchTasks: (dharmaId: number) => Promise<void>;
  createTask: (dharmaId: number, dto: CreateTaskDTO) => Promise<void>;
  moveTaskToNow: (taskId: number) => Promise<void>;
  fillNowWithNext: (userId: string) => Promise<Task[]>;
  changeTaskStatus: (taskId: number, status: TaskStatus) => Promise<void>;
  markTaskDone: (taskId: number) => Promise<void>;
  deleteTask: (taskId: number) => Promise<void>;
}

export const useStore = create<AppState>((set) => ({
  user: null,
  dharmas: [],
  tasks: [],
  theme: 'light',
  hydrated: false,
  
  setUser: (user) => {
    set({ user });
    if (user) {
      localStorage.setItem('userId', user.id);
    } else {
      localStorage.removeItem('userId');
    }
  },
  
  loadUserFromStorage: async () => {
    const userId = localStorage.getItem('userId');
    if (userId) {
      try {
        const user = await usersApi.get(userId);
        set({ user });
      } catch {
        localStorage.removeItem('userId');
        set({ user: null });
      }
    }
    set({ hydrated: true });
  },

  loadTheme: () => {
    const saved = localStorage.getItem('theme');
    if (saved === 'dark' || saved === 'light') {
      set({ theme: saved });
    }
  },

  toggleTheme: () => {
    set((state) => {
      const next = state.theme === 'light' ? 'dark' : 'light';
      localStorage.setItem('theme', next);
      return { theme: next };
    });
  },
  
  logout: () => {
    localStorage.removeItem('userId');
    set({ user: null, dharmas: [], tasks: [] });
  },

  // Dharma actions
  fetchDharmas: async (userId: string) => {
    try {
      const dharmas = await dharmaApi.getByUser(userId);
      set({ dharmas });
    } catch (error) {
      console.error('Failed to fetch dharmas:', error);
    }
  },

  createDharma: async (userId: string, dto: CreateDharmaDTO) => {
    try {
      const newDharma = await dharmaApi.create(userId, dto);
      set((state) => ({ dharmas: [...state.dharmas, newDharma] }));
    } catch (error) {
      console.error('Failed to create dharma:', error);
      throw error;
    }
  },

  deleteDharma: async (dharmaId: number) => {
    try {
      await dharmaApi.delete(dharmaId);
      set((state) => ({ 
        dharmas: state.dharmas.filter((d) => d.id !== dharmaId),
        tasks: state.tasks.filter((t) => t.dharma.id !== dharmaId)
      }));
    } catch (error) {
      console.error('Failed to delete dharma:', error);
      throw error;
    }
  },

  // Task actions
  fetchTasks: async (dharmaId: number) => {
    try {
      const tasks = await tasksApi.getByDharma(dharmaId);
      set({ tasks });
    } catch (error) {
      console.error('Failed to fetch tasks:', error);
    }
  },

  createTask: async (dharmaId: number, dto: CreateTaskDTO) => {
    try {
      const newTask = await tasksApi.create(dharmaId, dto);
      set((state) => ({ tasks: [...state.tasks, newTask] }));
    } catch (error) {
      console.error('Failed to create task:', error);
      throw error;
    }
  },

  moveTaskToNow: async (taskId: number) => {
    try {
      const updatedTask = await tasksApi.moveToNow(taskId);
      set((state) => ({
        tasks: state.tasks.map((t) => (t.id === taskId ? updatedTask : t)),
      }));
    } catch (error) {
      console.error('Failed to move task to now:', error);
      throw error;
    }
  },

  fillNowWithNext: async (userId: string) => {
    try {
      // Fetch current NOW and NEXT tasks for the user; prioritize NEXT oldest first
      const now = await tasksApi.getByUserAndStatus(userId, 'NOW' as TaskStatus);
      const next = await tasksApi.getByUserAndStatus(userId, 'NEXT' as TaskStatus);

      if (now.length >= 5) {
        set({ tasks: now });
        return now;
      }

      const needed = 5 - now.length;
      const candidates = next.slice(0, needed);

      const promoted: Task[] = [];
      for (const t of candidates) {
        const updated = await tasksApi.changeStatus(t.id, 'NOW' as TaskStatus);
        promoted.push(updated);
      }

      const combined = [...now, ...promoted];
      set({ tasks: combined });
      return combined;
    } catch (error) {
      console.error('Failed to fill NOW with NEXT:', error);
      throw error;
    }
  },

  changeTaskStatus: async (taskId: number, status: TaskStatus) => {
    try {
      const updatedTask = await tasksApi.changeStatus(taskId, status);
      set((state) => ({
        tasks: state.tasks.map((t) => (t.id === taskId ? updatedTask : t)),
      }));
    } catch (error) {
      console.error('Failed to change task status:', error);
      throw error;
    }
  },

  markTaskDone: async (taskId: number) => {
    try {
      const updatedTask = await tasksApi.markDone(taskId);
      set((state) => ({
        tasks: state.tasks.map((t) => (t.id === taskId ? updatedTask : t)),
      }));
    } catch (error) {
      console.error('Failed to mark task done:', error);
      throw error;
    }
  },

  deleteTask: async (taskId: number) => {
    try {
      await tasksApi.delete(taskId);
      set((state) => ({ tasks: state.tasks.filter((t) => t.id !== taskId) }));
    } catch (error) {
      console.error('Failed to delete task:', error);
      throw error;
    }
  },
}));
