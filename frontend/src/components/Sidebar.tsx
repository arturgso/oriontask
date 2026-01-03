import { useNavigate } from 'react-router-dom';
import { useStore } from '../state/store';
import { LogOut, Moon, Sun } from 'lucide-react';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  const navigate = useNavigate();
  const dharmas = useStore((state) => state.dharmas);
  const logout = useStore((state) => state.logout);
  const theme = useStore((state) => state.theme);
  const toggleTheme = useStore((state) => state.toggleTheme);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  const handleNavigation = (path: string) => {
    navigate(path);
    onClose();
  };

  return (
    <>
      {/* Overlay - visível quando menu está aberto em mobile */}
      {isOpen && (
        <div
          className="md:hidden fixed inset-0 bg-black/30 z-30 top-16"
          onClick={onClose}
        />
      )}

      {/* Sidebar */}
      <aside
        className={`${Styles.sidebar} ${
          isOpen
            ? 'fixed left-0 top-16 h-[calc(100vh-4rem)] z-40 md:relative md:top-0 md:h-full'
            : 'hidden md:flex'
        }`}
      >
        <div className={Styles.section}>
          <strong className={Styles.sectionTitle}>Dharmas</strong>
          <ul className={Styles.list}>
            {dharmas.map((dharma) => (
              <li
                key={dharma.id}
                onClick={() => handleNavigation(`/tasks/${dharma.id}`)}
                className={Styles.listItem}
              >
                <span style={{ backgroundColor: dharma.color }} className={Styles.dot} />
                {dharma.name}
              </li>
            ))}
            <li
              onClick={() => handleNavigation('/dharmas')}
              className={Styles.listItem + ' ' + Styles.addItem}
            >
              + Gerenciar Dharmas
            </li>
          </ul>
        </div>

        <div className={Styles.section}>
          <strong className={Styles.sectionTitle}>Navegação</strong>
          <ul className={Styles.list}>
            <li
              onClick={() => handleNavigation('/agora')}
              className={Styles.listItem}
            >
              ⚡ Agora
            </li>
            <li
              onClick={() => handleNavigation('/dharmas')}
              className={Styles.listItem}
            >
              📋 Todos os Dharmas
            </li>
          </ul>
        </div>

        <button onClick={toggleTheme} className={Styles.themeButton}>
          {theme === 'dark' ? <Sun size={16} /> : <Moon size={16} />}
          {theme === 'dark' ? 'Modo claro' : 'Modo escuro'}
        </button>

        <button onClick={handleLogout} className={Styles.logoutButton}>
          <LogOut size={16} />
          Sair
        </button>
      </aside>
    </>
  );
}

const Styles = {
  sidebar: 'w-full md:w-56 bg-gray-200 p-3 md:p-5 flex flex-col h-auto md:h-full md:border-r border-gray-300',
  section: 'mb-4 md:mb-6',
  sectionTitle: 'block mb-2 text-xs md:text-sm font-bold',
  list: 'space-y-1',
  listItem: 'text-xs md:text-sm py-2 px-3 hover:bg-gray-300 cursor-pointer flex items-center gap-2 rounded transition-colors',
  addItem: 'text-gray-700 border-t border-gray-400 mt-2 pt-2 font-semibold',
  dot: 'w-2.5 md:w-3 h-2.5 md:h-3 rounded-full flex-shrink-0',
  themeButton: 'mb-2 flex items-center gap-2 text-xs md:text-sm px-3 py-2 hover:bg-gray-300 w-full text-left rounded font-semibold transition-colors',
  logoutButton: 'mt-auto flex items-center gap-2 text-xs md:text-sm px-3 py-2 hover:bg-gray-300 w-full text-left rounded font-semibold transition-colors',
};
