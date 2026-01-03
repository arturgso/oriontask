import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { tasksApi } from '../api';
import { useStore } from '../state/store';
import { TaskCard } from '../components/TaskCard';
import { Sidebar } from '../components/Sidebar';
import toast from 'react-hot-toast';
import { Plus } from 'lucide-react';
import { KarmaType, EffortLevel, TaskStatus, type Task, type CreateTaskDTO } from '../types';

export function TasksPage() {
  const { dharmaId } = useParams<{ dharmaId: string }>();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [karmaType, setKarmaType] = useState<KarmaType>(KarmaType.ENERGY);
  const [effortLevel, setEffortLevel] = useState<EffortLevel>(EffortLevel.LOW);
  const [loading, setLoading] = useState(false);

  const hydrated = useStore((state) => state.hydrated);
  const dharmas = useStore((state) => state.dharmas);
  const dharma = dharmas.find((d) => d.id === Number(dharmaId));

  useEffect(() => {
    if (!hydrated) return;
    if (dharmaId) {
      loadTasks();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [dharmaId, hydrated]);

  const loadTasks = async () => {
    try {
      const data = await tasksApi.getByDharma(Number(dharmaId));
      setTasks(data);
    } catch {
      toast.error('Erro ao carregar tasks');
    }
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!dharmaId) return;

    setLoading(true);
    try {
      const data: CreateTaskDTO = { title, description: description || undefined, karmaType, effortLevel };
      const task = await tasksApi.create(Number(dharmaId), data);
      setTasks([...tasks, task]);
      toast.success('Task criada!');
      setShowForm(false);
      setTitle('');
      setDescription('');
    } catch {
      toast.error('Erro ao criar task');
    } finally {
      setLoading(false);
    }
  };

  const handleComplete = async (taskId: number) => {
    try {
      const updated = await tasksApi.markDone(taskId);
      setTasks(tasks.map((t) => (t.id === taskId ? updated : t)));
      toast.success('Task concluída! 🎉');
    } catch {
      toast.error('Erro ao concluir task');
    }
  };

  const handleMove = async (taskId: number, status: TaskStatus) => {
    try {
      const updated = await tasksApi.changeStatus(taskId, status);
      setTasks(tasks.map((t) => (t.id === taskId ? updated : t)));
      toast.success(status === TaskStatus.NOW ? 'Movida para Agora' : 'Movida para Depois');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Erro ao mover task';
      toast.error(message);
    }
  };

  const handleDelete = async (taskId: number) => {
    try {
      await tasksApi.delete(taskId);
      setTasks(tasks.filter((t) => t.id !== taskId));
      toast.success('Task removida');
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Erro ao deletar task';
      toast.error(message);
    }
  };

  const activeTasks = tasks.filter((t) => t.status !== TaskStatus.DONE);
  const doneTasks = tasks.filter((t) => t.status === TaskStatus.DONE);

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
            <h2 className={Styles.title}>{dharma?.name || 'Tasks'}</h2>
            {dharma?.description && <p className={Styles.subtitle}>{dharma.description}</p>}
          </div>

          <div className={Styles.section}>
        <div className={Styles.sectionHeader}>
          <h2 className={Styles.sectionTitle}>Ativas</h2>
          <button onClick={() => setShowForm(true)} className={Styles.addButton}>
            <Plus size={20} />
            <span>Nova Task</span>
          </button>
        </div>

        <div className={Styles.taskList}>
          {activeTasks.map((task) => (
            <TaskCard
              key={task.id}
              task={task}
              onComplete={() => handleComplete(task.id)}
              onMove={(status) => handleMove(task.id, status)}
              onDelete={() => handleDelete(task.id)}
            />
          ))}
          {activeTasks.length === 0 && (
            <p className={Styles.empty}>Nenhuma task ativa</p>
          )}
        </div>
      </div>

      {doneTasks.length > 0 && (
        <div className={Styles.section}>
          <h2 className={Styles.sectionTitle}>Concluídas</h2>
          <div className={Styles.taskList}>
            {doneTasks.map((task) => (
              <TaskCard key={task.id} task={task} />
            ))}
          </div>
        </div>
      )}

      {showForm && (
        <div className={Styles.modal}>
          <form onSubmit={handleCreate} className={Styles.form}>
            <h2 className={Styles.formTitle}>Nova Task</h2>

            <div className={Styles.field}>
              <label htmlFor="task-title" className={Styles.label}>
                Título *
              </label>
              <input
                id="task-title"
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                className={Styles.input}
                placeholder="Beber água ao acordar"
                required
                minLength={5}
                maxLength={60}
              />
            </div>

            <div className={Styles.field}>
              <label htmlFor="task-desc" className={Styles.label}>
                Descrição
              </label>
              <input
                id="task-desc"
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className={Styles.input}
                placeholder="500ml logo pela manhã"
                maxLength={200}
              />
            </div>

            <div className={Styles.field}>
              <label htmlFor="karma-type" className={Styles.label}>
                Tipo de Karma *
              </label>
              <select
                id="karma-type"
                value={karmaType}
                onChange={(e) => setKarmaType(e.target.value as KarmaType)}
                className={Styles.select}
              >
                <option value={KarmaType.ENERGY}>Energia</option>
                <option value={KarmaType.MOOD}>Humor</option>
                <option value={KarmaType.RELATIONSHIPS}>Relações</option>
                <option value={KarmaType.MONEY}>Dinheiro</option>
                <option value={KarmaType.GROWTH}>Crescimento</option>
              </select>
            </div>

            <div className={Styles.field}>
              <label htmlFor="effort-level" className={Styles.label}>
                Esforço Necessário *
              </label>
              <select
                id="effort-level"
                value={effortLevel}
                onChange={(e) => setEffortLevel(e.target.value as EffortLevel)}
                className={Styles.select}
              >
                <option value={EffortLevel.LOW}>Baixo (~10min)</option>
                <option value={EffortLevel.MEDIUM}>Médio (~25min)</option>
                <option value={EffortLevel.HIGH}>Alto (~50min)</option>
              </select>
            </div>

            <div className={Styles.formActions}>
              <button type="button" onClick={() => setShowForm(false)} className={Styles.cancelButton}>
                Cancelar
              </button>
              <button type="submit" disabled={loading} className={Styles.submitButton}>
                {loading ? 'Criando...' : 'Criar'}
              </button>
            </div>
          </form>
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
  header: 'mb-4',
  title: 'text-lg font-bold',
  subtitle: 'text-sm text-gray-600',
  section: 'mb-6',
  sectionHeader: 'flex justify-between items-center mb-3 border-b pb-2',
  sectionTitle: 'text-base font-bold',
  addButton: 'px-4 py-2 bg-gray-800 text-white text-sm hover:bg-gray-700 flex items-center gap-2 rounded font-semibold',
  taskList: 'space-y-2',
  empty: 'text-center text-gray-500 py-4 text-sm',
  modal: 'fixed inset-0 bg-black/30 flex items-center justify-center p-4',
  form: 'bg-white border border-gray-400 p-6 w-full max-w-md',
  formTitle: 'text-xl font-bold mb-4',
  field: 'mb-3',
  label: 'block text-sm font-semibold mb-1',
  input: 'w-full px-2 py-1 border border-gray-400 focus:outline-none focus:border-gray-600',
  select: 'w-full px-2 py-1 border border-gray-400 focus:outline-none focus:border-gray-600',
  formActions: 'flex gap-3 mt-5',
  cancelButton: 'flex-1 px-4 py-3 bg-gray-300 hover:bg-gray-400 font-semibold',
  submitButton: 'flex-1 px-4 py-3 bg-gray-800 text-white hover:bg-gray-700 disabled:opacity-50 disabled:cursor-not-allowed font-semibold',
};
