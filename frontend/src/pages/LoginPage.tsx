import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/authService';
import { useStore } from '../state/store';
import toast from 'react-hot-toast';
import { LogIn, UserPlus } from 'lucide-react';

export function LoginPage() {
  const [isSignup, setIsSignup] = useState(false);
  const [login, setLogin] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const setUser = useStore((state) => state.setUser);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await authService.login({ login, password });
      setUser({
        id: response.id,
        username: response.username,
        name: response.name,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      });
      toast.success(`Bem-vindo de volta, ${response.name}!`);
      navigate('/agora');
    } catch (error: any) {
      toast.error(error.message || 'Usuário ou senha inválidos');
    } finally {
      setLoading(false);
    }
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validação de senha
    if (password.length < 8) {
      toast.error('A senha deve ter no mínimo 8 caracteres');
      return;
    }
    if (!/[A-Z]/.test(password)) {
      toast.error('A senha deve conter ao menos uma letra maiúscula');
      return;
    }
    if (!/[a-z]/.test(password)) {
      toast.error('A senha deve conter ao menos uma letra minúscula');
      return;
    }
    if (!/[0-9]/.test(password)) {
      toast.error('A senha deve conter ao menos um número');
      return;
    }
    if (!/[!@#$%^&*(),.?":{}|<>]/.test(password)) {
      toast.error('A senha deve conter ao menos um caractere especial');
      return;
    }
    
    setLoading(true);

    try {
      const response = await authService.signup({ 
        name, 
        username: login, 
        email,
        password 
      });
      setUser({
        id: response.id,
        username: response.username,
        name: response.name,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      });
      toast.success(`Bem-vindo, ${response.name}!`);
      navigate('/agora');
    } catch (error: any) {
      toast.error(error.message || 'Erro ao criar usuário');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={Styles.container}>
      <div className={Styles.card}>
        <div className={Styles.header}>
          <img src="/logo.svg" alt="OrionTask Logo" className={Styles.icon} />
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
              <label htmlFor="login" className={Styles.label}>
                Username ou Email
              </label>
              <input
                id="login"
                type="text"
                value={login}
                onChange={(e) => setLogin(e.target.value)}
                className={Styles.input}
                placeholder="joao_silva ou email@exemplo.com"
                required
              />
            </div>

            <div className={Styles.field}>
              <label htmlFor="password" className={Styles.label}>
                Senha
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className={Styles.input}
                placeholder="••••••••"
                required
                minLength={8}
              />
            </div>

            <button type="submit" disabled={loading} className={Styles.button}>
              {loading ? 'Entrando...' : (
                <span className={Styles.buttonContent}>
                  <LogIn size={16} />
                  Entrar
                </span>
              )}
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
                value={login}
                onChange={(e) => setLogin(e.target.value)}
                className={Styles.input}
                placeholder="joao_silva"
                required
                minLength={3}
                maxLength={20}
                pattern="[a-zA-Z0-9_]+"
              />
            </div>

            <div className={Styles.field}>
              <label htmlFor="email" className={Styles.label}>
                Email
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className={Styles.input}
                placeholder="joao@exemplo.com"
                required
              />
            </div>

            <div className={Styles.field}>
              <label htmlFor="password-signup" className={Styles.label}>
                Senha
              </label>
              <input
                id="password-signup"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className={Styles.input}
                placeholder="••••••••"
                required
                minLength={8}
              />
              <p className={Styles.hint}>
                Mínimo 8 caracteres, com maiúscula, minúscula, número e caractere especial
              </p>
            </div>

            <button type="submit" disabled={loading} className={Styles.button}>
              {loading ? 'Criando...' : (
                <span className={Styles.buttonContent}>
                  <UserPlus size={16} />
                  Criar conta
                </span>
              )}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}

const Styles = {
  container: 'min-h-screen flex items-center justify-center bg-base p-3 md:p-4',
  card: 'bg-card border border-surface p-4 md:p-8 w-full max-w-sm rounded-lg',
  header: 'text-center mb-6',
  icon: 'mx-auto mb-2 md:mb-3 w-12 md:w-16 h-12 md:h-16',
  title: 'text-xl md:text-2xl font-bold mb-1 md:mb-2 text-text-primary',
  subtitle: 'text-xs md:text-sm text-text-muted',
  tabs: 'flex gap-2 mb-6',
  tab: (active: boolean) =>
    `flex-1 py-2 md:py-3 font-semibold rounded text-xs md:text-sm transition-colors ${active ? 'bg-accent text-text-primary' : 'bg-surface text-text-muted hover:bg-surface/80'}`,
  form: 'space-y-4',
  field: 'space-y-1',
  label: 'block text-xs md:text-sm font-semibold text-text-primary',
  input: 'w-full px-2 py-1.5 md:py-2 border border-surface bg-surface focus:outline-none focus:border-accent text-sm rounded text-text-primary',
  hint: 'text-xs text-text-muted',
  button: 'w-full bg-accent text-text-primary py-2.5 md:py-3 font-semibold hover:bg-accent/80 disabled:opacity-50 rounded flex items-center justify-center text-sm transition-colors',
  buttonContent: 'flex items-center gap-2',
};
