import { useNavigate } from 'react-router-dom';
import { useStore } from '../state/store';
import { authService } from '../services/authService';
import { LogOut, Moon, Sun, Eye, EyeOff, Plus, Zap, List, Menu, X, User, CircleHelp } from 'lucide-react';
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
      <aside className={`${Styles.sidebar} ${isOpen ? Styles.sidebarOpen : Styles.sidebarClosed}`}>
        <div className={Styles.container}>
          <div className={Styles.header}>
            <img src="/logo.svg" alt="Orion Task Logo" className={Styles.logo} />
            <h1 className={Styles.title}>Orion Task</h1>
          </div>

          <div className={Styles.dharmasSection}>
            <h2 className={Styles.sectionTitle}>Dharmas</h2>
            <ul className={Styles.dharmaList}>
              {dharmas
                .filter((dharma) => (showHidden ? true : !dharma.hidden))
                .map((dharma) => (
                  <li
                    key={dharma.id}
                    className={Styles.dharmaItem}
                    onClick={() => handleNavigation(`/tasks/${dharma.id}`)}
                  >
                    <div
                      className={Styles.dharmaColor}
                      style={{ backgroundColor: dharma.color }}
                    />
                    <span>{dharma.name}</span>
                  </li>
                ))}
            </ul>
            <button
              className={Styles.manageButton}
              onClick={() => handleNavigation('/dharmas')}
            >
              <Plus size={16} />
              <span>Gerenciar Dharmas</span>
            </button>
          </div>

          <div>
            <h2 className={Styles.sectionTitle}>Navegação</h2>
            <div className={Styles.navList}>
              <button
                className={Styles.navButton}
                onClick={() => handleNavigation('/agora')}
              >
                <Zap size={18} />
                <span>Agora</span>
              </button>
              <button
                className={Styles.navButton}
                onClick={() => handleNavigation('/dharmas')}
              >
                <List size={18} />
                <span>Todos os Dharmas</span>
              </button>
              <button
                className={Styles.navButton}
                onClick={() => handleNavigation('/profile')}
              >
                <User size={18} />
                <span>Meu Perfil</span>
              </button>
            </div>
          </div>
        </div>

        <div className={Styles.footer}>
          <button
            className={Styles.footerButton}
            onClick={toggleShowHidden}
          >
            {showHidden ? <EyeOff size={18} /> : <Eye size={18} />}
            <span>Ocultar privados</span>
          </button>
          <button
            className={Styles.footerButton}
            onClick={() => setShowOnboarding(true)}
          >
            <CircleHelp size={18} />
            <span>Ajuda</span>
          </button>
          <button
            className={Styles.footerButton}
            onClick={toggleTheme}
          >
            {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            <span>{theme === 'light' ? 'Modo escuro' : 'Modo claro'}</span>
          </button>
          <button
            className={Styles.footerButton}
            onClick={handleLogout}
          >
            <LogOut size={18} />
            <span>Sair</span>
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
  sidebar: 'bg-card flex flex-col justify-between h-screen border-r border-surface fixed md:static top-0 left-0 z-50 w-64 transition-transform duration-300',
  sidebarOpen: 'translate-x-0',
  sidebarClosed: '-translate-x-full md:translate-x-0',

  container: 'flex flex-col p-6',
  header: 'hidden md:flex items-center gap-3 mb-8',
  logo: 'h-8 w-8',
  title: 'text-stellar text-xl font-bold',
  dharmasSection: 'mb-8',
  sectionTitle: 'text-xs text-nebula uppercase tracking-wider mb-3 font-semibold',
  dharmaList: 'flex flex-col gap-2',
  dharmaItem: 'cursor-pointer hover:bg-surface/50 rounded-md px-2 py-1.5 flex items-center gap-2 text-stellar text-sm transition-colors',
  dharmaColor: 'h-2.5 w-2.5 rounded-full flex-shrink-0',
  manageButton: 'flex items-center gap-2 mt-3 text-sm text-nebula hover:text-stellar transition-colors px-2 py-1.5',
  navList: 'flex flex-col gap-1',
  navButton: 'flex items-center gap-3 px-2 py-2 text-sm text-stellar hover:bg-surface/50 rounded-md transition-colors',
  footer: 'border-t border-surface p-6 flex flex-col gap-1',
  footerButton: 'flex items-center gap-3 px-2 py-2 text-sm text-nebula hover:text-stellar hover:bg-surface/50 rounded-md transition-colors',
};
