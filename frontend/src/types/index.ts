export interface User {
  id: string;
  name: string;
  username: string;
  createdAt: string;
  updatedAt: string;
}

export interface Profile {
  id: string;
  name: string;
  username: string;
  email: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateProfileRequest {
  name?: string;
  username?: string;
  email?: string;
  newPassword?: string;
}

export interface Dharma {
  id: number;
  user: User;
  name: string;
  description: string | null;
  color: string;
  hidden: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Task {
  id: number;
  dharma: Dharma;
  title: string;
  description: string | null;
  karmaType: KarmaType;
  effortLevel: EffortLevel;
  status: TaskStatus;
  hidden: boolean;
  completedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export const KarmaType = {
  ACTION: 'ACTION',
  PEOPLE: 'PEOPLE',
  THINKING: 'THINKING',
} as const;

export type KarmaType = typeof KarmaType[keyof typeof KarmaType];

export const EffortLevel = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
} as const;

export type EffortLevel = typeof EffortLevel[keyof typeof EffortLevel];

export const TaskStatus = {
  NOW: 'NOW',
  NEXT: 'NEXT',
  WAITING: 'WAITING',
  DONE: 'DONE',
} as const;

export type TaskStatus = typeof TaskStatus[keyof typeof TaskStatus];

export interface CreateUserDTO {
  name: string;
  username: string;
}

export interface CreateDharmaDTO {
  name: string;
  description?: string;
  color?: string;
  hidden?: boolean;
}

export interface CreateTaskDTO {
  title: string;
  description?: string;
  karmaType: KarmaType;
  effortLevel: EffortLevel;
}
