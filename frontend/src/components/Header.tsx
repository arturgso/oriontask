import { Menu, X } from 'lucide-react';

interface HeaderProps {
  title: string;
  subtitle?: string;
  sidebarOpen: boolean;
  onToggleSidebar: () => void;
}

export function Header({ title, subtitle, sidebarOpen, onToggleSidebar }: HeaderProps) {
  return (
    <header className="bg-card text-text-primary p-3 md:p-4 flex items-center gap-3 border-b border-surface">
      {/* Botão hambúrguer - visível apenas em mobile */}
      <button
        onClick={onToggleSidebar}
        className="md:hidden p-2 hover:bg-surface rounded transition-colors flex-shrink-0"
        aria-label="Abrir menu"
      >
        {sidebarOpen ? <X size={20} /> : <Menu size={20} />}
      </button>

      <div>
        <h1 className="text-lg md:text-xl font-bold">{title}</h1>
        {subtitle && <p className="text-xs md:text-sm text-text-muted">{subtitle}</p>}
      </div>
    </header>
  );
}
