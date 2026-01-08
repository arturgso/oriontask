import { KarmaType } from '../types';
import { Zap, Users, Brain } from 'lucide-react';

interface KarmaBadgeProps {
  type: KarmaType;
  size?: 'sm' | 'md';
}

export function KarmaBadge({ type, size = 'md' }: KarmaBadgeProps) {
  const config = {
    ACTION: { icon: Zap, label: 'Ação', color: 'bg-yellow-100 text-yellow-700' },
    PEOPLE: { icon: Users, label: 'Pessoas', color: 'bg-blue-100 text-blue-700' },
    THINKING: { icon: Brain, label: 'Reflexão', color: 'bg-purple-100 text-purple-700' },
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
    `flex items-center gap-1.5 ${color} rounded-full ${
      size === 'sm' ? 'px-2 py-0.5 text-xs' : 'px-3 py-1 text-sm'
    } font-medium`,
};
