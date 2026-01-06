import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
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
  const [showOverflow, setShowOverflow] = useState(false);
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const user = useStore((state) => state.user);
  const hydrated = useStore((state) => state.hydrated);
  const fillNowWithNext = useStore((state) => state.fillNowWithNext);
  const navigate = useNavigate();

  useEffect(() => {
    if (!hydrated) return;

    if (!user) {
      navigate('/login');
      return;
    }
    loadNowTasks();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user, hydrated]);

  const loadNowTasks = async () => {
    if (!user) return;

    try {
      const data = await tasksApi.getByUserAndStatus(user.id, TaskStatus.NOW);
      if (data.length < 5) {
        const filled = await fillNowWithNext(user.id);
        setTasks(filled);
      } else {
        setTasks(data);
      }
    } catch {
      toast.error('Erro ao carregar tasks');
    } finally {
      setLoading(false);
    }
  };

  const primaryTasks = tasks.slice(0, 5);
  const overflowTasks = tasks.slice(5, 8);

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
        <main className={Styles.main}>
          <Sidebar 
            isOpen={sidebarOpen} 
            onClose={() => setSidebarOpen(false)}
            onToggle={() => setSidebarOpen(!sidebarOpen)}
          />
          <section className={Styles.content}>
            <p className={Styles.loading}>Carregando...</p>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className={Styles.page}>
      <main className={Styles.main}>
        <Sidebar 
          isOpen={sidebarOpen} 
          onClose={() => setSidebarOpen(false)}
          onToggle={() => setSidebarOpen(!sidebarOpen)}
        />
        
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
        {(showOverflow ? tasks : primaryTasks).map((task) => (
          <TaskCard
            key={task.id}
            task={task}
            onComplete={() => handleComplete(task.id)}
            onMove={(status) => handleMove(task.id, status)}
          />
        ))}
      </div>

      {!showOverflow && overflowTasks.length > 0 && (
        <button className={Styles.showMore} onClick={() => setShowOverflow(true)}>
          Mostrar mais ({overflowTasks.length})
        </button>
      )}

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
  page: 'min-h-screen flex flex-col bg-base pt-14 md:pt-0',
  main: 'flex flex-col md:flex-row flex-1 gap-0 md:gap-0',
  content: 'flex-1 p-3 md:p-4 bg-card md:border-l border-surface',
  header: 'flex items-center gap-2 mb-2',
  icon: 'text-accent w-5 md:w-6 h-5 md:h-6',
  title: 'text-text-primary md:text-lg font-bold',
  subtitle: 'text-xs md:text-sm text-text-muted mb-3',
  counter: 'bg-surface w-fit border border-surface p-2 md:p-3 mb-3 md:mb-4 rounded',
  counterText: 'text-xs md:text-sm font-semibold text-text-primary',
  taskList: 'space-y-2 md:space-y-3',
  showMore: 'mt-3 px-3 md:px-4 py-2 border border-surface hover:bg-surface text-xs md:text-sm rounded transition-colors text-text-primary',
  empty: 'text-center py-6 md:py-8 text-text-muted',
  emptyText: 'text-xs md:text-sm font-semibold mb-1',
  emptyHint: 'text-xs text-text-muted',
  loading: 'text-center py-6 md:py-8 text-text-muted text-sm',
};
