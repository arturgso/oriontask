import { useNavigate } from 'react-router-dom';
import { useStore } from '../state/store';
import { authService } from '../services/authService';
import { LogOut, Moon, Sun, Eye, EyeOff, Plus, Zap, List, Menu, X, User, CircleHelp, ChevronLeft, ChevronRight } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { Dharma } from '../types';
import { api } from '../api/client';
import { OnboardingModal } from './OnboardingModal';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
  onToggle: () => void;
}

export function Sidebar({ isOpen, onClose, onToggle }: SidebarProps) {
  const navigate = useNavigate();
  const [dharmas, setDharmas] = useState<Dharma[]>([]);
  const logout = useStore((state) => state.logout);
  const theme = useStore((state) => state.theme);
  const toggleTheme = useStore((state) => state.toggleTheme);
  const showHidden = useStore((state) => state.showHidden);
  const toggleShowHidden = useStore((state) => state.toggleShowHidden);
  const loadShowHidden = useStore((state) => state.loadShowHidden);
  const isCollapsed = useStore((state) => state.isSidebarCollapsed);
  const toggleSidebarCollapse = useStore((state) => state.toggleSidebarCollapse);

  const userId = useStore((state) => state.user?.id);

  const [showOnboarding, setShowOnboarding] = useState(false);

  useEffect(() => {
    loadShowHidden();
  }, [loadShowHidden]);

  const handleLogout = () => {
    authService.logout(); // Limpar token
    logout(); // Limpar store
    navigate('/login');
  };

  useEffect(() => {
    if (!userId) return;

    const fetchDharmas = async () => {
      try {
        const response = await api.get<Dharma[]>(`/dharma/user/${userId}`);
        setDharmas(response);

      } catch (error) {
        console.error('Error fetching dharmas:', error);
      }
    }
    fetchDharmas();
  }, [userId, showHidden])

  const handleNavigation = (path: string) => {
    navigate(path);
    onClose();
  };

  return (
    <>
      {/* Mobile Header */}
      <header className={Styles.mobileHeader}>
        <button
          onClick={onToggle}
          className={Styles.hamburger}
          aria-label="Toggle menu"
        >
          {isOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
        <div className={Styles.mobileLogoContainer}>
          <img src="/logo.svg" alt="Orion Task Logo" className={Styles.mobileLogo} />
          <span className={Styles.mobileTitle}>Orion Task</span>
        </div>
      </header>

      {/* Overlay */}
      {isOpen && (
        <div
          className={Styles.overlay}
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside className={`${Styles.sidebar} ${isOpen ? Styles.sidebarOpen : Styles.sidebarClosed} ${isCollapsed ? 'md:w-20' : 'md:w-64'}`}>
        <button
          onClick={toggleSidebarCollapse}
          className={Styles.collapseToggle}
          title={isCollapsed ? "Expandir menu" : "Recolher menu"}
        >
          {isCollapsed ? <ChevronRight size={16} /> : <ChevronLeft size={16} />}
        </button>

        <div className={Styles.container}>
          <div className={`${Styles.header} ${isCollapsed ? 'justify-center' : ''}`}>
            <img src="/logo.svg" alt="Orion Task Logo" className={Styles.logo} />
            {!isCollapsed && <h1 className={Styles.title}>Orion Task</h1>}
          </div>

          <div className={Styles.dharmasSection}>
            {!isCollapsed && <h2 className={Styles.sectionTitle}>Dharmas</h2>}
            <ul className={Styles.dharmaList}>
              {dharmas
                .filter((dharma) => (showHidden ? true : !dharma.hidden))
                .map((dharma) => (
                  <li
                    key={dharma.id}
                    className={`${Styles.dharmaItem} ${isCollapsed ? 'justify-center px-0' : ''}`}
                    onClick={() => handleNavigation(`/tasks/${dharma.id}`)}
                    title={isCollapsed ? dharma.name : undefined}
                  >
                    <div
                      className={Styles.dharmaColor}
                      style={{ backgroundColor: dharma.color }}
                    />
                    {!isCollapsed && <span>{dharma.name}</span>}
                  </li>
                ))}
            </ul>
            <button
              className={`${Styles.manageButton} ${isCollapsed ? 'justify-center' : ''}`}
              onClick={() => handleNavigation('/dharmas')}
              title={isCollapsed ? "Gerenciar Dharmas" : undefined}
            >
              <Plus size={16} />
              {!isCollapsed && <span>Gerenciar Dharmas</span>}
            </button>
          </div>

          <div>
            {!isCollapsed && <h2 className={Styles.sectionTitle}>Navegação</h2>}
            <div className={Styles.navList}>
              <button
                className={`${Styles.navButton} ${isCollapsed ? 'justify-center' : ''}`}
                onClick={() => handleNavigation('/agora')}
                title={isCollapsed ? "Agora" : undefined}
              >
                <Zap size={18} />
                {!isCollapsed && <span>Agora</span>}
              </button>
              <button
                className={`${Styles.navButton} ${isCollapsed ? 'justify-center' : ''}`}
                onClick={() => handleNavigation('/dharmas')}
                title={isCollapsed ? "Todos os Dharmas" : undefined}
              >
                <List size={18} />
                {!isCollapsed && <span>Todos os Dharmas</span>}
              </button>
              <button
                className={`${Styles.navButton} ${isCollapsed ? 'justify-center' : ''}`}
                onClick={() => handleNavigation('/profile')}
                title={isCollapsed ? "Meu Perfil" : undefined}
              >
                <User size={18} />
                {!isCollapsed && <span>Meu Perfil</span>}
              </button>
            </div>
          </div>
        </div>

        <div className={`${Styles.footer} ${isCollapsed ? 'px-2 items-center' : ''}`}>
          <button
            className={`${Styles.footerButton} ${isCollapsed ? 'justify-center w-full' : ''}`}
            onClick={toggleShowHidden}
            title={isCollapsed ? (showHidden ? "Ocultar privados" : "Mostrar privados") : undefined}
          >
            {showHidden ? <EyeOff size={18} /> : <Eye size={18} />}
            {!isCollapsed && <span>Ocultar privados</span>}
          </button>
          <button
            className={`${Styles.footerButton} ${isCollapsed ? 'justify-center w-full' : ''}`}
            onClick={() => setShowOnboarding(true)}
            title={isCollapsed ? "Ajuda" : undefined}
          >
            <CircleHelp size={18} />
            {!isCollapsed && <span>Ajuda</span>}
          </button>
          <button
            className={`${Styles.footerButton} ${isCollapsed ? 'justify-center w-full' : ''}`}
            onClick={toggleTheme}
            title={isCollapsed ? (theme === 'light' ? 'Modo escuro' : 'Modo claro') : undefined}
          >
            {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            {!isCollapsed && <span>{theme === 'light' ? 'Modo escuro' : 'Modo claro'}</span>}
          </button>
          <button
            className={`${Styles.footerButton} ${isCollapsed ? 'justify-center w-full text-rose-400' : ''}`}
            onClick={handleLogout}
            title={isCollapsed ? "Sair" : undefined}
          >
            <LogOut size={18} />
            {!isCollapsed && <span className="text-rose-400">Sair</span>}
          </button>
        </div>
      </aside>

      <OnboardingModal isOpen={showOnboarding} onClose={() => setShowOnboarding(false)} />
    </>
  );
}

const Styles = {
  // Mobile Header
  mobileHeader: 'md:hidden fixed top-0 left-0 right-0 z-40 bg-card border-b border-surface px-4 py-3 flex items-center justify-between',
  hamburger: 'p-2 hover:bg-surface rounded-md transition-colors text-stellar',
  mobileLogoContainer: 'flex items-center gap-2',
  mobileLogo: 'h-6 w-6',
  mobileTitle: 'text-stellar font-bold text-lg',

  // Overlay
  overlay: 'md:hidden fixed inset-0 bg-black/50 z-40',

  // Sidebar
  sidebar: 'bg-card flex flex-col justify-between h-screen border-r border-surface fixed md:static top-0 left-0 z-50 transition-all duration-300 relative',
  sidebarOpen: 'translate-x-0',
  sidebarClosed: '-translate-x-full md:translate-x-0',

  collapseToggle: 'hidden md:flex absolute -right-3 top-20 bg-card border border-surface rounded-full p-1 text-nebula hover:text-stellar shadow-sm z-50 cursor-pointer',

  container: 'flex flex-col p-4 md:p-6 overflow-x-hidden',
  header: 'hidden md:flex items-center gap-3 mb-8 transition-all',
  logo: 'h-8 w-8 flex-shrink-0',
  title: 'text-stellar text-xl font-bold whitespace-nowrap overflow-hidden',
  dharmasSection: 'mb-8',
  sectionTitle: 'text-xs text-nebula uppercase tracking-wider mb-3 font-semibold whitespace-nowrap overflow-hidden',
  dharmaList: 'flex flex-col gap-2',
  dharmaItem: 'cursor-pointer hover:bg-surface/50 rounded-md px-2 py-1.5 flex items-center gap-3 text-stellar text-sm transition-colors whitespace-nowrap overflow-hidden',
  dharmaColor: 'h-2.5 w-2.5 rounded-full flex-shrink-0',
  manageButton: 'flex items-center gap-2 mt-3 text-sm text-nebula hover:text-stellar transition-colors px-2 py-1.5 whitespace-nowrap overflow-hidden',
  navList: 'flex flex-col gap-1',
  navButton: 'flex items-center gap-3 px-2 py-2 text-sm text-stellar hover:bg-surface/50 rounded-md transition-colors whitespace-nowrap overflow-hidden',
  footer: 'border-t border-surface p-4 md:p-6 flex flex-col gap-1 transition-all',
  footerButton: 'flex items-center gap-3 px-2 py-2 text-sm text-nebula hover:text-stellar hover:bg-surface/50 rounded-md transition-colors whitespace-nowrap overflow-hidden',
};
