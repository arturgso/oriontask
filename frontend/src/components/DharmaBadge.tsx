interface DharmaBadgeProps {
  name: string;
  color: string;
  onClick?: () => void;
}

export function DharmaBadge({ name, color, onClick }: DharmaBadgeProps) {
  return (
    <div className={Styles.badge} style={{ backgroundColor: color }} onClick={onClick}>
      <span className={Styles.name}>{name}</span>
    </div>
  );
}

const Styles = {
  badge: 'px-4 py-2 rounded-lg text-white font-medium cursor-pointer hover:opacity-90 transition-opacity',
  name: 'text-sm',
};
