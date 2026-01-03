import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { usersApi } from '../api';
import { useStore } from '../state/store';
import toast from 'react-hot-toast';
import { User as UserIcon } from 'lucide-react';

export function LoginPage() {
  const [isSignup, setIsSignup] = useState(false);
  const [username, setUsername] = useState('');
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const setUser = useStore((state) => state.setUser);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const user = await usersApi.getByUsername(username);
      setUser(user);
      toast.success(`Bem-vindo de volta, ${user.name}!`);
      navigate('/dharmas');
    } catch {
      toast.error('Usuário não encontrado');
    } finally {
      setLoading(false);
    }
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const user = await usersApi.signup({ name, username });
      setUser(user);
      toast.success(`Bem-vindo, ${user.name}!`);
      navigate('/dharmas');
    } catch {
      toast.error('Erro ao criar usuário. Username pode já existir.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={Styles.container}>
      <div className={Styles.card}>
        <div className={Styles.header}>
          <UserIcon size={48} className={Styles.icon} />
          <h1 className={Styles.title}>OrionTask</h1>
          <p className={Styles.subtitle}>Tarefas com propósito</p>
        </div>

        <div className={Styles.tabs}>
          <button
            onClick={() => setIsSignup(false)}
            className={Styles.tab(!isSignup)}
          >
            Entrar
          </button>
          <button
            onClick={() => setIsSignup(true)}
            className={Styles.tab(isSignup)}
          >
            Criar conta
          </button>
        </div>

        {!isSignup ? (
          <form onSubmit={handleLogin} className={Styles.form}>
            <div className={Styles.field}>
              <label htmlFor="username" className={Styles.label}>
                Username
              </label>
              <input
                id="username"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className={Styles.input}
                placeholder="joao_silva"
                required
                minLength={3}
                maxLength={20}
                pattern="[a-zA-Z0-9_]+"
              />
            </div>

            <button type="submit" disabled={loading} className={Styles.button}>
              {loading ? 'Entrando...' : 'Entrar'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleSignup} className={Styles.form}>
            <div className={Styles.field}>
              <label htmlFor="name" className={Styles.label}>
                Nome completo
              </label>
              <input
                id="name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className={Styles.input}
                placeholder="João Silva"
                required
                minLength={3}
                maxLength={50}
              />
            </div>

            <div className={Styles.field}>
              <label htmlFor="username-signup" className={Styles.label}>
                Username
              </label>
              <input
                id="username-signup"
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className={Styles.input}
                placeholder="joao_silva"
                required
                minLength={3}
                maxLength={20}
                pattern="[a-zA-Z0-9_]+"
              />
              <p className={Styles.hint}>Apenas letras, números e underscore</p>
            </div>

            <button type="submit" disabled={loading} className={Styles.button}>
              {loading ? 'Criando...' : 'Criar conta'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}

const Styles = {
  container: 'min-h-screen flex items-center justify-center bg-gray-100 p-4',
  card: 'bg-white border border-gray-400 p-8 w-full max-w-md',
  header: 'text-center mb-6',
  icon: 'mx-auto mb-3',
  title: 'text-2xl font-bold mb-2',
  subtitle: 'text-sm text-gray-600',
  tabs: 'flex gap-2 mb-6',
  tab: (active: boolean) =>
    `flex-1 py-2 font-medium ${active ? 'bg-gray-800 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'}`,
  form: 'space-y-4',
  field: 'space-y-1',
  label: 'block text-sm font-semibold',
  input: 'w-full px-2 py-2 border border-gray-400 focus:outline-none focus:border-gray-600',
  hint: 'text-xs text-gray-500',
  button: 'w-full bg-gray-800 text-white py-2 font-semibold hover:bg-gray-700 disabled:opacity-50',
};
