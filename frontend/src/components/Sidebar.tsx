import { useNavigate } from 'react-router-dom';
import { useStore } from '../state/store';
import { LogOut } from 'lucide-react';

export function Sidebar() {
  const navigate = useNavigate();
  const dharmas = useStore((state) => state.dharmas);
  const logout = useStore((state) => state.logout);

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <aside className={Styles.sidebar}>
      <div className={Styles.section}>
        <strong className={Styles.sectionTitle}>Dharmas</strong>
        <ul className={Styles.list}>
          {dharmas.map((dharma) => (
            <li
              key={dharma.id}
              onClick={() => navigate(`/tasks/${dharma.id}`)}
              className={Styles.listItem}
            >
              <span style={{ backgroundColor: dharma.color }} className={Styles.dot} />
              {dharma.name}
            </li>
          ))}
          <li onClick={() => navigate('/dharmas')} className={Styles.listItem + ' ' + Styles.addItem}>
            + Gerenciar Dharmas
          </li>
        </ul>
      </div>

      <div className={Styles.section}>
        <strong className={Styles.sectionTitle}>Navegação</strong>
        <ul className={Styles.list}>
          <li onClick={() => navigate('/agora')} className={Styles.listItem}>
            ⚡ Agora
          </li>
          <li onClick={() => navigate('/dharmas')} className={Styles.listItem}>
            📋 Todos os Dharmas
          </li>
        </ul>
      </div>

      <button onClick={handleLogout} className={Styles.logoutButton}>
        <LogOut size={16} />
        Sair
      </button>
    </aside>
  );
}

const Styles = {
  sidebar: 'w-56 bg-gray-200 p-4 flex flex-col h-full',
  section: 'mb-6',
  sectionTitle: 'block mb-2 text-sm font-bold',
  list: 'space-y-1',
  listItem: 'text-sm py-1 px-2 hover:bg-gray-300 cursor-pointer flex items-center gap-2',
  addItem: 'text-gray-600 border-t border-gray-400 mt-2 pt-2',
  dot: 'w-3 h-3 rounded-full flex-shrink-0',
  logoutButton: 'mt-auto flex items-center gap-2 text-sm px-2 py-1 hover:bg-gray-300 w-full text-left',
};
