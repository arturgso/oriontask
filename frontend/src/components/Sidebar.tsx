import { useNavigate } from 'react-router-dom';
import { useStore } from '../state/store';
import { LogOut, Moon, Sun, Eye, EyeOff, Plus, Zap, List } from 'lucide-react';
import { useEffect, useState } from 'react';
import type { Dharma } from '../types';
import { api } from '../api/client';

interface SidebarProps {
  isOpen: boolean;
  onClose: () => void;
}

export function Sidebar({ isOpen, onClose }: SidebarProps) {
  const navigate = useNavigate();
  const [dharmas, setDharmas] = useState<Dharma[]>([]);
  const logout = useStore((state) => state.logout);
  const theme = useStore((state) => state.theme);
  const toggleTheme = useStore((state) => state.toggleTheme);
  const showHidden = useStore((state) => state.showHidden);
  const toggleShowHidden = useStore((state) => state.toggleShowHidden);
  const loadShowHidden = useStore((state) => state.loadShowHidden);

  const userId = useStore((state) => state.user?.id);

  useEffect(() => {
    loadShowHidden();
  }, [loadShowHidden]);

  const handleLogout = () => {
    logout();
    navigate('/');
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
      {isOpen && (
        <div
          className="md:hidden fixed inset-0 bg-black/30 z-30 top-16"
          onClick={onClose}
        />
      )}
      <aside className='bg-card flex flex-col justify-between h-screen border-r border-surface'>
        <div className='flex flex-col p-6'>
          <div className='flex items-center gap-3 mb-8'>
            <img src="/logo.svg" alt="Orion Task Logo" className='h-8 w-8 dark:invert' />
            <h1 className='text-text-primary text-xl font-bold'>Orion Task</h1>
          </div>
          
          <div className='mb-8'>
            <h2 className='text-xs text-text-muted uppercase tracking-wider mb-3 font-semibold'>Dharmas</h2>
            <ul className='flex flex-col gap-2'>
              {dharmas
                .filter((dharma) => (showHidden ? true : !dharma.hidden))
                .map((dharma) => (
                  <li
                    key={dharma.id}
                    className='cursor-pointer hover:bg-surface/50 rounded-md px-2 py-1.5 flex items-center gap-2 text-text-primary text-sm transition-colors'
                    onClick={() => handleNavigation(`/tasks/${dharma.id}`)}
                  >
                    <div 
                      className='h-2.5 w-2.5 rounded-full flex-shrink-0' 
                      style={{ backgroundColor: dharma.color }}
                    />
                    <span>{dharma.name}</span>
                  </li>
                ))}
            </ul>
            <button 
              className='flex items-center gap-2 mt-3 text-sm text-text-muted hover:text-text-primary transition-colors px-2 py-1.5'
              onClick={() => handleNavigation('/manage-dharmas')}
            >
              <Plus size={16} />
              <span>Gerenciar Dharmas</span>
            </button>
          </div>

          <div>
            <h2 className='text-xs text-text-muted uppercase tracking-wider mb-3 font-semibold'>Navegação</h2>
            <div className='flex flex-col gap-1'>
              <button 
                className='flex items-center gap-3 px-2 py-2 text-sm text-text-primary hover:bg-surface/50 rounded-md transition-colors'
                onClick={() => handleNavigation('/agora')}
              >
                <Zap size={18} />
                <span>Agora</span>
              </button>
              <button 
                className='flex items-center gap-3 px-2 py-2 text-sm text-text-primary hover:bg-surface/50 rounded-md transition-colors'
                onClick={() => handleNavigation('/dharmas')}
              >
                <List size={18} />
                <span>Todos os Dharmas</span>
              </button>
            </div>
          </div>
        </div>

        <div className='border-t border-surface p-6 flex flex-col gap-1'>
          <button 
            className='flex items-center gap-3 px-2 py-2 text-sm text-text-muted hover:text-text-primary hover:bg-surface/50 rounded-md transition-colors'
            onClick={toggleShowHidden}
          >
            {showHidden ? <EyeOff size={18} /> : <Eye size={18} />}
            <span>Ocultar privados</span>
          </button>
          <button 
            className='flex items-center gap-3 px-2 py-2 text-sm text-text-muted hover:text-text-primary hover:bg-surface/50 rounded-md transition-colors'
            onClick={toggleTheme}
          >
            {theme === 'light' ? <Moon size={18} /> : <Sun size={18} />}
            <span>Modo escuro</span>
          </button>
          <button 
            className='flex items-center gap-3 px-2 py-2 text-sm text-text-muted hover:text-text-primary hover:bg-surface/50 rounded-md transition-colors'
            onClick={handleLogout}
          >
            <LogOut size={18} />
            <span>Sair</span>
          </button>
        </div>
      </aside>
    </>
  );
}
