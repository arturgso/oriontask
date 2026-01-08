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
  card: 'bg-card border border-surface p-3 md:p-4 flex flex-col md:flex-row justify-between items-start md:items-center gap-3 md:gap-4 hover:bg-surface/60 transition-all rounded-xl text-stellar shadow-sm active:scale-[0.99]',
  left: 'flex gap-3 md:gap-4 flex-1 w-full',
  dharmaColor: 'w-3 md:w-3.5 h-3 md:h-3.5 mt-1.5 flex-shrink-0 rounded-full shadow-[0_0_8px_rgba(var(--color-primary),0.3)]',
  title: (isDone: boolean) => `text-sm md:text-base ${isDone ? 'line-through text-gray-400 dark:text-gray-500 opacity-60' : 'text-stellar'} font-semibold tracking-tight`,
  description: 'text-xs md:text-sm text-nebula mt-1.5 leading-relaxed',
  badges: 'flex gap-2 mt-2 flex-wrap',
  actions: 'flex gap-2 md:gap-3 flex-shrink-0 w-full md:w-auto flex-wrap md:flex-nowrap mt-3 md:mt-0',
  button: 'px-3 md:px-4 py-2 md:py-2.5 text-xs md:text-sm border border-surface rounded-lg flex items-center gap-2 hover:bg-surface transition-all whitespace-nowrap text-stellar font-medium',
  moveButton: 'hover:border-primary/50 hover:text-primary active:bg-primary/5',
  completeButton: 'hover:border-accent/50 hover:text-accent active:bg-accent/5',
  deleteButton: 'hover:border-rose-500/50 hover:text-rose-400 active:bg-rose-500/5',
};
