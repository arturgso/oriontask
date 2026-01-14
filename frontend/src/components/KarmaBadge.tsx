import { KarmaType } from '../types';
import { Zap, Users, Brain } from 'lucide-react';

interface KarmaBadgeProps {
  type: KarmaType;
  size?: 'sm' | 'md';
}

export function KarmaBadge({ type, size = 'md' }: KarmaBadgeProps) {
  const config = {
    ACTION: { icon: Zap, label: 'Ação', color: 'bg-yellow-500/20 text-yellow-400' },
    PEOPLE: { icon: Users, label: 'Pessoas', color: 'bg-blue-500/20 text-blue-400' },
    THINKING: { icon: Brain, label: 'Reflexão', color: 'bg-purple-500/20 text-purple-400' },
  };

  const { icon: Icon, label, color } = config[type];
  const iconSize = size === 'sm' ? 14 : 18;

  return (
    <div className={Styles.badge(color, size)}>
      <Icon size={iconSize} />
      <span>{label}</span>
    </div>
  );
}

const Styles = {
  badge: (color: string, size: string) =>
    `flex items-center gap-1.5 ${color} rounded-md ${size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-3 py-1 text-sm'
    } font-medium`,
};
