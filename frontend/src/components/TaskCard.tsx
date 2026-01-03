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
  card: 'bg-white border border-gray-300 p-2 md:p-3 flex flex-col md:flex-row justify-between items-start md:items-center gap-2 md:gap-3 hover:bg-gray-50 transition-colors rounded',
  left: 'flex gap-2 md:gap-3 flex-1 w-full',
  dharmaColor: 'w-2.5 md:w-3 h-2.5 md:h-3 mt-1 md:mt-1 flex-shrink-0 rounded-full',
  title: (isDone: boolean) => `text-xs md:text-sm ${isDone ? 'line-through text-gray-400' : 'text-gray-900'} font-medium`,
  description: 'text-xs text-gray-600 mt-1',
  badges: 'flex gap-1 mt-1 flex-wrap',
  actions: 'flex gap-1 md:gap-2 flex-shrink-0 w-full md:w-auto flex-wrap md:flex-nowrap',
  button: 'px-2 md:px-3 py-1.5 md:py-2 text-xs md:text-sm border border-gray-300 rounded flex items-center gap-1 hover:bg-gray-100 transition-colors whitespace-nowrap',
  moveButton: 'hover:bg-blue-50 hover:border-blue-300 hover:text-blue-600',
  completeButton: 'hover:bg-green-50 hover:border-green-300 hover:text-green-600',
  deleteButton: 'hover:bg-red-50 hover:border-red-300 hover:text-red-600',
};
