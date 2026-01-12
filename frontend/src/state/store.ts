import { create } from 'zustand';
import type { User, Dharma, Task, CreateDharmaDTO, CreateTaskDTO, TaskStatus } from '../types';
import { usersApi, dharmaApi, tasksApi } from '../api';

interface AppState {
  user: User | null;
  dharmas: Dharma[];
  tasks: Task[];
  theme: 'light' | 'dark';
  showHidden: boolean;
  hydrated: boolean;
  isSidebarCollapsed: boolean;
  setUser: (user: User | null) => void;
  loadUserFromStorage: () => Promise<void>;
  loadTheme: () => void;
  toggleTheme: () => void;
  loadShowHidden: () => void;
  toggleShowHidden: () => Promise<void>;
  logout: () => void;
  toggleSidebarCollapse: () => void;
  loadSidebarState: () => void;

  // Dharma actions
  fetchDharmas: (userId: string) => Promise<void>;
  createDharma: (userId: string, dto: CreateDharmaDTO) => Promise<void>;
  updateDharma: (dharmaId: number, dto: CreateDharmaDTO) => Promise<void>;
  toggleDharmaHidden: (dharmaId: number) => Promise<void>;
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

export const useStore = create<AppState>((set, get) => ({
  user: null,
  dharmas: [],
  tasks: [],
  theme: 'light',
  showHidden: false,
  hydrated: false,
  isSidebarCollapsed: false,

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

  loadShowHidden: () => {
    const saved = localStorage.getItem('showHidden');
    if (saved === 'true' || saved === 'false') {
      set({ showHidden: saved === 'true' });
    }
  },

  toggleShowHidden: async () => {
    const state = get();
    const next = !state.showHidden;
    localStorage.setItem('showHidden', String(next));
    set({ showHidden: next });

    // Refetch dharmas with new showHidden value
    if (state.user) {
      await get().fetchDharmas(state.user.id);
    }
  },

  logout: () => {
    localStorage.removeItem('userId');
    set({ user: null, dharmas: [], tasks: [] });
  },

  toggleSidebarCollapse: () => {
    set((state) => {
      const next = !state.isSidebarCollapsed;
      localStorage.setItem('sidebarCollapsed', String(next));
      return { isSidebarCollapsed: next };
    });
  },

  loadSidebarState: () => {
    const saved = localStorage.getItem('sidebarCollapsed');
    if (saved === 'true') {
      set({ isSidebarCollapsed: true });
    }
  },

  // Dharma actions
  fetchDharmas: async (userId: string) => {
    try {
      const state = get();
      const dharmas = await dharmaApi.getByUser(userId, state.showHidden);
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

  updateDharma: async (dharmaId: number, dto: CreateDharmaDTO) => {
    try {
      const updatedDharma = await dharmaApi.update(dharmaId, dto);
      set((state) => ({
        dharmas: state.dharmas.map((d) => (d.id === dharmaId ? updatedDharma : d)),
      }));
    } catch (error) {
      console.error('Failed to update dharma:', error);
      throw error;
    }
  },

  toggleDharmaHidden: async (dharmaId: number) => {
    try {
      const updatedDharma = await dharmaApi.toggleHidden(dharmaId);
      set((state) => ({
        dharmas: state.dharmas.map((d) => (d.id === dharmaId ? updatedDharma : d)),
        tasks: state.tasks.map((t) =>
          t.dharma.id === dharmaId ? { ...t, hidden: updatedDharma.hidden, dharma: updatedDharma } : t
        ),
      }));
    } catch (error) {
      console.error('Failed to toggle dharma hidden:', error);
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
      const response = await tasksApi.getByDharma(dharmaId);
      set({ tasks: response.content });
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
      const nowResponse = await tasksApi.getByUserAndStatus(userId, 'NOW' as TaskStatus);
      const nextResponse = await tasksApi.getByUserAndStatus(userId, 'NEXT' as TaskStatus);

      const now = nowResponse.content;
      const next = nextResponse.content;

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
