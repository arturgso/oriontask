import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Trash2, Plus, ArrowRight, Edit, Eye, EyeOff } from 'lucide-react';
import { useStore } from '../state/store';

import { Sidebar } from '../components/Sidebar';
import { DharmaEditModal } from '../components/DharmaEditModal';
import toast from 'react-hot-toast';

export function DharmasPage() {
  const navigate = useNavigate();
  const user = useStore((state) => state.user);
  const hydrated = useStore((state) => state.hydrated);
  const dharmas = useStore((state) => state.dharmas);
  const fetchDharmas = useStore((state) => state.fetchDharmas);
  const createDharma = useStore((state) => state.createDharma);
  const updateDharma = useStore((state) => state.updateDharma);
  const toggleDharmaHidden = useStore((state) => state.toggleDharmaHidden);
  const deleteDharma = useStore((state) => state.deleteDharma);

  const [showForm, setShowForm] = useState(false);
  const [editingDharma, setEditingDharma] = useState<typeof dharmas[0] | null>(null);
  const [name, setName] = useState('');
  const [color, setColor] = useState('#3B82F6');
  const [sidebarOpen, setSidebarOpen] = useState(false);

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

  const handleEdit = async (data: { name: string; description?: string; color: string }) => {
    if (!editingDharma) return;
    await updateDharma(editingDharma.id, data);
    toast.success('Dharma atualizado');
  };

  const handleToggleHidden = async (id: number, currentHidden: boolean) => {
    await toggleDharmaHidden(id);
    toast.success(currentHidden ? 'Dharma visível' : 'Dharma oculto');
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
      <main className={Styles.main}>
        <Sidebar
          isOpen={sidebarOpen}
          onClose={() => setSidebarOpen(false)}
          onToggle={() => setSidebarOpen(!sidebarOpen)}
        />

        <section className={Styles.content}>
          <h2 className={Styles.pageTitle}>Meus Dharmas</h2>
          <p className={Styles.pageSubtitle}>Áreas de vida que importam (áximo 8)</p>

          <div className={Styles.grid}>
            {dharmas.map((dharma) => (
              <div
                key={dharma.id}
                onClick={() => navigate(`/tasks/${dharma.id}`)}
                className={`${Styles.dharmaItem} ${dharma.hidden ? 'opacity-60' : ''}`}
              >
                <div className={Styles.dharmaContent}>
                  <span style={{ backgroundColor: dharma.color }} className={Styles.colorDot} />
                  <span className={Styles.dharmaName}>{dharma.name}</span>
                  {dharma.hidden && <EyeOff size={14} className="text-gray-500 ml-2" />}
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setEditingDharma(dharma);
                    }}
                    className={Styles.editBtn}
                    title="Editar Dharma"
                  >
                    <Edit size={14} />
                    <span className="hidden md:inline">Editar</span>
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleToggleHidden(dharma.id, dharma.hidden);
                    }}
                    className={Styles.toggleBtn}
                    title={dharma.hidden ? 'Tornar visível' : 'Ocultar'}
                  >
                    {dharma.hidden ? <Eye size={14} /> : <EyeOff size={14} />}
                    <span className="hidden md:inline">{dharma.hidden ? 'Mostrar' : 'Ocultar'}</span>
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete(dharma.id);
                    }}
                    className={Styles.deleteBtn}
                    title="Remover Dharma"
                  >
                    <Trash2 size={14} />
                    <span className="hidden md:inline">Remover</span>
                  </button>
                </div>
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

          {editingDharma && (
            <DharmaEditModal
              dharma={editingDharma}
              onClose={() => setEditingDharma(null)}
              onSave={handleEdit}
            />
          )}
        </section>
      </main>
    </div>
  );
}

const Styles = {
  page: 'h-screen flex flex-col bg-canvas pt-14 md:pt-0 overflow-hidden',
  main: 'flex flex-row flex-1 overflow-hidden',
  content: 'flex-1 p-3 md:p-4 bg-card md:border-l border-surface pb-24 md:pb-8 overflow-y-auto custom-scrollbar',
  pageTitle: 'text-base md:text-lg font-bold mb-1 text-stellar',
  pageSubtitle: 'text-xs md:text-sm text-nebula mb-4',
  grid: 'space-y-2 mb-4',
  dharmaItem: 'flex flex-col md:flex-row items-start md:items-center justify-between bg-card border border-surface p-2 md:p-3 hover:bg-surface/50 gap-2 rounded transition-colors cursor-pointer',
  dharmaContent: 'flex items-center gap-2 md:gap-3 flex-1',
  colorDot: 'w-3 md:w-4 h-3 md:h-4 rounded-full flex-shrink-0',
  dharmaName: 'text-xs md:text-sm text-stellar truncate',
  editBtn: 'text-nebula hover:text-primary px-2 md:px-3 py-1.5 md:py-2 text-xs border border-surface rounded flex items-center gap-1 hover:bg-surface/50 transition-colors whitespace-nowrap',
  toggleBtn: 'text-nebula hover:text-accent px-2 md:px-3 py-1.5 md:py-2 text-xs border border-surface rounded flex items-center gap-1 hover:bg-surface/50 transition-colors whitespace-nowrap',
  deleteBtn: 'text-nebula hover:text-red-500 px-2 md:px-3 py-1.5 md:py-2 text-xs border border-surface rounded flex items-center gap-1 hover:bg-surface/50 transition-colors whitespace-nowrap',
  addButton: 'w-full border-2 border-dashed border-surface p-2 md:p-3 text-xs md:text-sm text-nebula hover:bg-surface hover:text-stellar flex items-center justify-center gap-2 font-medium transition-colors',
  modal: 'fixed inset-0 bg-black/60 backdrop-blur-sm flex items-center justify-center p-3 md:p-4 z-50',
  form: 'bg-card border border-surface p-4 md:p-6 max-w-md w-full max-h-[90vh] overflow-y-auto rounded-lg shadow-2xl',
  formTitle: 'text-lg md:text-xl font-bold mb-4 text-stellar',
  field: 'mb-3',
  label: 'block text-xs md:text-sm font-semibold mb-1.5 text-stellar',
  input: 'w-full px-2 py-1.5 md:py-2 border border-surface bg-surface focus:outline-none focus:border-primary text-sm text-stellar rounded',
  colorGrid: 'grid grid-cols-4 gap-2',
  colorOption: 'w-8 md:w-10 h-8 md:h-10 cursor-pointer border-2 rounded-full transition-transform hover:scale-110',
  actions: 'flex gap-2 md:gap-3 mt-4 md:mt-4',
  submitBtn: 'flex-1 bg-primary text-stellar py-2 md:py-3 px-3 md:px-4 hover:bg-primary/80 flex items-center justify-center gap-2 text-xs md:text-sm font-semibold transition-colors rounded',
  cancelBtn: 'flex-1 bg-surface text-stellar py-2 md:py-3 px-3 md:px-4 hover:bg-surface/80 text-xs md:text-sm font-semibold transition-colors rounded',
  agoraButton: 'fixed bottom-4 right-4 bg-primary text-stellar px-3 md:px-4 py-2 md:py-3 hover:bg-primary/80 flex items-center gap-2 text-xs md:text-sm font-semibold rounded-full shadow-lg transition-colors border border-primary/20',
};
