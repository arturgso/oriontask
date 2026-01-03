import { EffortLevel } from '../types';
import { Timer } from 'lucide-react';

interface EffortTagProps {
  level: EffortLevel;
}

export function EffortTag({ level }: EffortTagProps) {
  const config = {
    LOW: { label: '~10min', color: 'text-green-600' },
    MEDIUM: { label: '~25min', color: 'text-orange-600' },
    HIGH: { label: '~50min', color: 'text-red-600' },
  };

  const { label, color } = config[level];

  return (
    <div className={Styles.tag}>
      <Timer size={14} className={color} />
      <span className={color}>{label}</span>
    </div>
  );
}

const Styles = {
  tag: 'flex items-center gap-1 text-xs font-medium',
};
