import { TaskStatus, type Task } from '../types';
import { KarmaBadge } from './KarmaBadge';
import { EffortTag } from './EffortTag';
import { Trash2, MoveRight, Clock, Check } from 'lucide-react';

interface TaskCardProps {
  task: Task;
  onComplete?: () => void;
  onMove?: (status: TaskStatus) => void;
  onDelete?: () => void;
}

export function TaskCard({ task, onComplete, onMove, onDelete }: TaskCardProps) {
  const isDone = task.status === TaskStatus.DONE;

  return (
    <div className={Styles.card}>
      <div className={Styles.left}>
        <div className={Styles.dharmaColor} style={{ backgroundColor: task.dharma.color }} />
        <div>
          <h3 className={Styles.title(isDone)}>{task.title}</h3>
          {task.description && <p className={Styles.description}>{task.description}</p>}
          <div className={Styles.badges}>
            <KarmaBadge type={task.karmaType} size="sm" />
            <EffortTag level={task.effortLevel} />
          </div>
        </div>
      </div>

      {!isDone && (
        <div className={Styles.actions}>
          {task.status !== TaskStatus.NOW && onMove && (
            <button
              onClick={() => onMove(TaskStatus.NOW)}
              className={Styles.button + ' ' + Styles.moveButton}
              title="Mover para Agora"
            >
              <MoveRight size={14} />
              <span>Agora</span>
            </button>
          )}

          {task.status === TaskStatus.NOW && onMove && (
            <button
              onClick={() => onMove(TaskStatus.NEXT)}
              className={Styles.button + ' ' + Styles.moveButton}
              title="Mover para Depois"
            >
              <Clock size={14} />
              <span>Depois</span>
            </button>
          )}

          {onComplete && (
            <button
              onClick={onComplete}
              className={Styles.button + ' ' + Styles.completeButton}
              title="Concluir"
            >
              <Check size={14} />
              <span>Concluir</span>
            </button>
          )}

          {onDelete && (
            <button
              onClick={onDelete}
              className={Styles.button + ' ' + Styles.deleteButton}
              title="Remover"
            >
              <Trash2 size={14} />
              <span>Remover</span>
            </button>
          )}
        </div>
      )}
    </div>
  );
}

const Styles = {
  card: 'bg-white border border-gray-300 p-3 flex justify-between items-start hover:bg-gray-50',
  left: 'flex gap-3 flex-1',
  dharmaColor: 'w-3 h-3 mt-1 flex-shrink-0',
  title: (isDone: boolean) => `text-sm ${isDone ? 'line-through text-gray-400' : 'text-gray-900'}`,
  description: 'text-xs text-gray-600 mt-1',
  badges: 'flex gap-1 mt-1',
  actions: 'flex gap-2 flex-shrink-0',
  button: 'px-3 py-2 text-sm border border-gray-300 rounded flex items-center gap-1.5 hover:bg-gray-100',
  moveButton: 'hover:bg-gray-100',
  completeButton: 'hover:bg-gray-100',
  deleteButton: 'hover:bg-red-50 hover:border-red-300 hover:text-red-600',
};
