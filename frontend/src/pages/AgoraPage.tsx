import { useState, useEffect } from 'react';
import { tasksApi } from '../api';
import { useStore } from '../state/store';
import { TaskCard } from '../components/TaskCard';
import { Sidebar } from '../components/Sidebar';
import toast from 'react-hot-toast';
import { Zap } from 'lucide-react';
import { TaskStatus, type Task } from '../types';

export function AgoraPage() {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const user = useStore((state) => state.user);

  useEffect(() => {
    if (user) {
      loadNowTasks();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user]);

  const loadNowTasks = async () => {
    if (!user) return;

    try {
      const data = await tasksApi.getByUserAndStatus(user.id, TaskStatus.NOW);
      setTasks(data);
    } catch {
      toast.error('Erro ao carregar tasks');
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async (taskId: number) => {
    try {
      await tasksApi.markDone(taskId);
      setTasks(tasks.filter((t) => t.id !== taskId));
      
      const task = tasks.find((t) => t.id === taskId);
      if (task) {
        const karmaLabels = {
          ENERGY: '⚡ Energia',
          MOOD: '💖 Humor',
          RELATIONSHIPS: '👥 Relações',
          MONEY: '💰 Dinheiro',
          GROWTH: '📈 Crescimento',
        };
        toast.success(`Task concluída! +${karmaLabels[task.karmaType]} 🎉`, {
          duration: 4000,
        });
      }
    } catch {
      toast.error('Erro ao concluir task');
    }
  };

  const handleMove = async (taskId: number, status: TaskStatus) => {
    try {
      await tasksApi.changeStatus(taskId, status);
      setTasks(tasks.filter((t) => t.id !== taskId));
      toast.success('Movida para Depois');
    } catch {
      toast.error('Erro ao mover task');
    }
  };

  if (loading) {
    return (
      <div className={Styles.page}>
        <header className={Styles.pageHeader}>
          <h1 className={Styles.headerTitle}>Orion Task</h1>
        </header>
        <main className={Styles.main}>
          <Sidebar />
          <section className={Styles.content}>
            <p className={Styles.loading}>Carregando...</p>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className={Styles.page}>
      <header className={Styles.pageHeader}>
        <h1 className={Styles.headerTitle}>Orion Task</h1>
        <p className={Styles.headerSubtitle}>Gerenciador de tarefas focado em clareza</p>
      </header>

      <main className={Styles.main}>
        <Sidebar />
        
        <section className={Styles.content}>
          <div className={Styles.header}>
            <Zap size={24} className={Styles.icon} />
            <h2 className={Styles.title}>Agora</h2>
          </div>

          <p className={Styles.subtitle}>Máximo 5 tarefas para ação imediata</p>

          <div className={Styles.counter}>
            <span className={Styles.counterText}>
              {tasks.length} / 5 tasks
            </span>
          </div>

          <div className={Styles.taskList}>
        {tasks.map((task) => (
          <TaskCard
            key={task.id}
            task={task}
            onComplete={() => handleComplete(task.id)}
            onMove={(status) => handleMove(task.id, status)}
          />
        ))}
      </div>

      {tasks.length === 0 && (
        <div className={Styles.empty}>
          <p className={Styles.emptyText}>Nenhuma task para agora</p>
          <p className={Styles.emptyHint}>
            Vá até seus Dharmas e mova tasks para cá
          </p>
        </div>
      )}
        </section>
      </main>
    </div>
  );
}

const Styles = {
  page: 'min-h-screen flex flex-col bg-gray-100',
  pageHeader: 'bg-gray-800 text-white p-4',
  headerTitle: 'text-xl font-bold',
  headerSubtitle: 'text-sm text-gray-300',
  main: 'flex flex-1',
  content: 'flex-1 p-4 bg-white',
  header: 'flex items-center gap-2 mb-2',
  icon: 'text-yellow-500',
  title: 'text-lg font-bold',
  subtitle: 'text-sm text-gray-600 mb-3',
  counter: 'bg-gray-100 border border-gray-300 p-3 mb-4',
  counterText: 'text-sm font-semibold',
  taskList: 'space-y-2',
  empty: 'text-center py-8 text-gray-500',
  emptyText: 'text-sm font-semibold mb-1',
  emptyHint: 'text-xs text-gray-400',
  loading: 'text-center py-8 text-gray-500',
};
