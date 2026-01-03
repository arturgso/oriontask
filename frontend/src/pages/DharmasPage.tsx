import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trash2, Plus, ArrowRight } from 'lucide-react';
import { useStore } from '../state/store';
import { Sidebar } from '../components/Sidebar';
import toast from 'react-hot-toast';

export function DharmasPage() {
  const navigate = useNavigate();
  const user = useStore((state) => state.user);
  const hydrated = useStore((state) => state.hydrated);
  const dharmas = useStore((state) => state.dharmas);
  const fetchDharmas = useStore((state) => state.fetchDharmas);
  const createDharma = useStore((state) => state.createDharma);
  const deleteDharma = useStore((state) => state.deleteDharma);

  const [showForm, setShowForm] = useState(false);
  const [name, setName] = useState('');
  const [color, setColor] = useState('#3B82F6');

  useEffect(() => {
    if (!hydrated) return;

    if (!user) {
      navigate('/');
      return;
    }
    fetchDharmas(user.id);
  }, [user, hydrated, fetchDharmas, navigate]);

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user) return;

    if (dharmas.length >= 8) {
      toast.error('Limite de 8 Dharmas atingido');
      return;
    }

    await createDharma(user.id, {
      name,
      color,
    });

    setShowForm(false);
    setName('');
    setColor('#3B82F6');
    toast.success('Dharma criado');
  };

  const handleDelete = async (id: number) => {
    await deleteDharma(id);
    toast.success('Dharma removido');
  };

  const suggestedColors = [
    '#3B82F6',
    '#10B981',
    '#F59E0B',
    '#EF4444',
    '#8B5CF6',
    '#EC4899',
    '#14B8A6',
    '#6366F1',
  ];

  return (
    <div className={Styles.page}>
      <header className={Styles.header}>
        <div>
          <h1 className={Styles.title}>Orion Task</h1>
          <p className={Styles.subtitle}>Gerenciador de tarefas focado em clareza</p>
        </div>
      </header>

      <main className={Styles.main}>
        <Sidebar />
        
        <section className={Styles.content}>
          <h2 className={Styles.pageTitle}>Meus Dharmas</h2>
          <p className={Styles.pageSubtitle}>Áreas de vida que importam (áximo 8)</p>

          <div className={Styles.grid}>
        {dharmas.map((dharma) => (
          <div key={dharma.id} className={Styles.dharmaItem}>
            <div 
              onClick={() => navigate(`/tasks/${dharma.id}`)}
              className={Styles.dharmaContent}
            >
              <span style={{ backgroundColor: dharma.color }} className={Styles.colorDot} />
              <span className={Styles.dharmaName}>{dharma.name}</span>
            </div>
            <button
              onClick={() => handleDelete(dharma.id)}
              className={Styles.deleteBtn}
              title="Remover Dharma"
            >
              <Trash2 size={14} />
              <span>Remover</span>
            </button>
          </div>
        ))}

        {dharmas.length < 8 && (
          <button onClick={() => setShowForm(true)} className={Styles.addButton}>
            <Plus size={16} />
            <span>Adicionar Dharma</span>
          </button>
        )}
      </div>

      {showForm && (
        <div className={Styles.modal}>
          <form onSubmit={handleCreate} className={Styles.form}>
            <h2 className={Styles.formTitle}>Criar Dharma</h2>

            <div className={Styles.field}>
              <label className={Styles.label}>Nome</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className={Styles.input}
                maxLength={50}
                required
              />
            </div>

            <div className={Styles.field}>
              <label className={Styles.label}>Cor</label>
              <div className={Styles.colorGrid}>
                {suggestedColors.map((c) => (
                  <button
                    key={c}
                    type="button"
                    onClick={() => setColor(c)}
                    className={Styles.colorOption}
                    style={{
                      backgroundColor: c,
                      border: color === c ? '3px solid #000' : '2px solid #ddd',
                    }}
                  />
                ))}
              </div>
            </div>

            <div className={Styles.actions}>
              <button type="submit" className={Styles.submitBtn}>
                <Plus size={16} />
                <span>Criar Dharma</span>
              </button>
              <button
                type="button"
                onClick={() => setShowForm(false)}
                className={Styles.cancelBtn}
              >
                Cancelar
              </button>
            </div>
          </form>
        </div>
      )}

      <button onClick={() => navigate('/agora')} className={Styles.agoraButton}>
        <ArrowRight size={16} />
        <span>Ver Agora</span>
      </button>
        </section>
      </main>
    </div>
  );
}

const Styles = {
  page: 'min-h-screen flex flex-col bg-gray-100',
  header: 'bg-gray-800 text-white p-4',
  title: 'text-xl font-bold',
  subtitle: 'text-sm text-gray-300',
  main: 'flex flex-1',
  content: 'flex-1 p-4 bg-white',
  pageTitle: 'text-lg font-bold mb-1',
  pageSubtitle: 'text-sm text-gray-600 mb-4',
  grid: 'space-y-2 mb-4',
  dharmaItem: 'flex items-center justify-between bg-white border border-gray-300 p-3 hover:bg-gray-50',
  dharmaContent: 'flex items-center gap-3 flex-1 cursor-pointer',
  colorDot: 'w-4 h-4 rounded-full',
  dharmaName: 'text-gray-800',
  deleteBtn: 'text-gray-600 hover:text-red-600 px-3 py-2 text-sm border border-gray-300 rounded flex items-center gap-1 hover:bg-red-50',
  addButton: 'w-full border-2 border-dashed border-gray-400 p-3 text-gray-700 hover:bg-gray-50 hover:border-gray-500 flex items-center justify-center gap-2 text-sm font-medium',
  modal: 'fixed inset-0 bg-black/30 flex items-center justify-center',
  form: 'bg-white border border-gray-400 p-6 max-w-md w-full m-4',
  formTitle: 'text-xl font-bold mb-4',
  field: 'mb-3',
  label: 'block text-sm font-semibold mb-1',
  input: 'w-full px-2 py-1 border border-gray-400 focus:outline-none focus:border-gray-600',
  colorGrid: 'grid grid-cols-4 gap-2',
  colorOption: 'w-10 h-10 cursor-pointer border-2',
  actions: 'flex gap-2 mt-4',
  submitBtn: 'flex-1 bg-gray-800 text-white py-3 px-4 hover:bg-gray-700 flex items-center justify-center gap-2 text-sm font-semibold',
  cancelBtn: 'flex-1 bg-gray-300 text-gray-800 py-3 px-4 hover:bg-gray-400 text-sm font-semibold',
  agoraButton: 'fixed bottom-4 right-4 bg-gray-800 text-white px-4 py-3 hover:bg-gray-700 flex items-center gap-2 text-sm font-semibold rounded shadow',
};
