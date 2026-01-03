import { KarmaType } from '../types';
import { Heart, Zap, Users, DollarSign, TrendingUp } from 'lucide-react';

interface KarmaBadgeProps {
  type: KarmaType;
  size?: 'sm' | 'md';
}

export function KarmaBadge({ type, size = 'md' }: KarmaBadgeProps) {
  const config = {
    ENERGY: { icon: Zap, label: 'Energia', color: 'bg-yellow-100 text-yellow-700' },
    MOOD: { icon: Heart, label: 'Humor', color: 'bg-pink-100 text-pink-700' },
    RELATIONSHIPS: { icon: Users, label: 'Relações', color: 'bg-blue-100 text-blue-700' },
    MONEY: { icon: DollarSign, label: 'Dinheiro', color: 'bg-green-100 text-green-700' },
    GROWTH: { icon: TrendingUp, label: 'Crescimento', color: 'bg-purple-100 text-purple-700' },
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
