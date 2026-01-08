import { useState } from 'react';
import { X } from 'lucide-react';
import type { Dharma } from '../types';

interface DharmaEditModalProps {
  dharma: Dharma;
  onClose: () => void;
  onSave: (data: { name: string; description?: string; color: string }) => Promise<void>;
}

export function DharmaEditModal({ dharma, onClose, onSave }: DharmaEditModalProps) {
  const [name, setName] = useState(dharma.name);
  const [description, setDescription] = useState(dharma.description || '');
  const [color, setColor] = useState(dharma.color);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSave({
      name,
      description: description || undefined,
      color,
    });
    onClose();
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div className="bg-card border border-surface rounded-lg shadow-xl max-w-md w-full p-6">
        <div className="flex items-center justify-between mb-6">
          <h3 className="text-xl font-semibold text-stellar">
            Editar Dharma
          </h3>
          <button
            onClick={onClose}
            className="text-nebula hover:text-stellar"
          >
            <X size={24} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-stellar mb-2">
              Nome
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-4 py-2 border border-surface bg-surface rounded-lg focus:ring-2 focus:ring-primary text-stellar"
              required
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-stellar mb-2">
              Descrição (opcional)
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-4 py-2 border border-surface bg-surface rounded-lg focus:ring-2 focus:ring-primary text-stellar"
              rows={3}
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-stellar mb-2">
              Cor
            </label>
            <div className="flex gap-2 flex-wrap mb-3">
              {suggestedColors.map((c) => (
                <button
                  key={c}
                  type="button"
                  onClick={() => setColor(c)}
                  className={`w-10 h-10 rounded-full border-2 transition-all ${color === c ? 'border-stellar scale-110' : 'border-transparent'
                    }`}
                  style={{ backgroundColor: c }}
                />
              ))}
            </div>
            <input
              type="color"
              value={color}
              onChange={(e) => setColor(e.target.value)}
              className="w-full h-10 rounded cursor-pointer"
            />
          </div>

          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-2 border border-surface rounded-lg text-stellar hover:bg-surface transition-colors"
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="flex-1 px-4 py-2 bg-primary text-stellar rounded-lg hover:bg-primary/80 transition-colors"
            >
              Salvar
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
