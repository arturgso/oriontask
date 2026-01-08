import { useEffect, useState } from 'react';
import { ArrowLeft, CalendarDays, Eye, EyeOff, Mail, ShieldCheck, UserCircle2 } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { Sidebar } from '../components/Sidebar';
import { api } from '../api/client';
import type { Profile, UpdateProfileRequest } from '../types';

type EditingSection = 'none' | 'profile' | 'password';

const Styles = {
  page: 'h-screen flex bg-canvas pt-14 md:pt-0 overflow-hidden',
  main: 'flex flex-1 flex-row overflow-hidden',
  content: 'flex-1 bg-card md:border-l border-surface px-4 sm:px-10 py-8 overflow-y-auto custom-scrollbar',
  container: 'max-w-3xl mx-auto space-y-6',
  back: 'inline-flex items-center gap-2 text-nebula text-sm hover:text-stellar transition',
  heading: 'space-y-1',
  title: 'text-3xl font-semibold text-stellar',
  subtitle: 'text-sm text-nebula',
  panel: 'bg-canvas/95 backdrop-blur rounded-3xl border border-surface shadow-lg px-6 sm:px-8 py-7 space-y-6',
  hero: 'flex flex-col sm:flex-row sm:items-center gap-4',
  heroIcon: 'h-16 w-16 rounded-2xl border border-surface bg-card flex items-center justify-center text-nebula',
  heroMeta: 'flex-1',
  heroLabel: 'text-xs uppercase tracking-wide text-nebula',
  heroName: 'text-2xl font-semibold text-stellar',
  heroEmail: 'text-sm text-nebula',
  sectionHeading: 'text-xs font-semibold text-nebula uppercase tracking-wide flex items-center gap-2',
  infoList: 'space-y-4',
  infoItem: 'flex items-start gap-3',
  infoIcon: 'h-11 w-11 rounded-xl bg-card border border-surface flex items-center justify-center text-nebula',
  infoCopy: 'space-y-1',
  infoLabel: 'text-xs text-nebula uppercase tracking-wide',
  infoValue: 'text-base text-stellar font-medium',
  buttonRow: 'flex flex-wrap gap-3 pt-2',
  ghostButton: 'px-4 py-2.5 rounded-xl border border-surface text-stellar hover:bg-canvas/60 transition disabled:opacity-60',
  primaryButton: 'px-4 py-2.5 rounded-xl bg-accent text-black font-semibold shadow-sm hover:opacity-90 transition disabled:opacity-60',
  editPanel: 'border border-surface rounded-2xl bg-card/80 p-5 space-y-5',
  form: 'grid gap-4 md:grid-cols-2',
  inputGroup: 'space-y-2 md:col-span-1',
  inputWrapper: 'relative flex items-center gap-3',
  input: 'w-full px-4 py-3 rounded-xl bg-canvas border border-surface text-stellar focus:border-accent focus:ring-2 focus:ring-accent/30 outline-none placeholder:text-nebula',
  togglePassword: 'flex items-center justify-center text-nebula hover:text-stellar transition',
  helper: 'text-xs text-nebula',
  loading: 'text-nebula text-center w-full py-10',
  meta: 'text-xs text-nebula border-t border-surface pt-4',
};

const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]+$/;

export default function ProfilePage() {
  const navigate = useNavigate();
  const [profile, setProfile] = useState<Profile | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [section, setSection] = useState<EditingSection>('none');
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [formData, setFormData] = useState({
    name: '',
    username: '',
    email: '',
    newPassword: '',
    confirmPassword: '',
  });

  useEffect(() => {
    loadProfile();
  }, []);

  const loadProfile = async () => {
    try {
      setLoading(true);
      const data = await api.get<Profile>('/users/profile');
      setProfile(data);
      setFormData((prev) => ({
        ...prev,
        name: data.name,
        username: data.username,
        email: data.email,
        newPassword: '',
        confirmPassword: '',
      }));
    } catch {
      toast.error('Não foi possível carregar o perfil.');
    } finally {
      setLoading(false);
    }
  };

  const handleProfileSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    const payload: UpdateProfileRequest = {};
    if (formData.name !== profile?.name) payload.name = formData.name;
    if (formData.username !== profile?.username) payload.username = formData.username;
    if (formData.email !== profile?.email) payload.email = formData.email;

    if (Object.keys(payload).length === 0) {
      setSection('none');
      return;
    }

    try {
      setSaving(true);
      const updated = await api.patch<Profile>('/users/profile', payload);
      setProfile(updated);
      toast.success('Perfil atualizado com sucesso.');
      setSection('none');
    } catch (err: any) {
      toast.error(err?.message || 'Erro ao atualizar perfil.');
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!formData.newPassword) {
      setSection('none');
      return;
    }

    if (formData.newPassword !== formData.confirmPassword) {
      toast.error('As senhas não coincidem.');
      return;
    }

    if (!passwordRegex.test(formData.newPassword)) {
      toast.error('Senha deve conter maiúscula, minúscula, número e caractere especial.');
      return;
    }

    try {
      setSaving(true);
      await api.patch<Profile>('/users/profile', { newPassword: formData.newPassword });
      setFormData((prev) => ({ ...prev, newPassword: '', confirmPassword: '' }));
      toast.success('Senha alterada com sucesso.');
      setSection('none');
    } catch (err: any) {
      toast.error(err?.message || 'Erro ao alterar senha.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className={Styles.page}>
        <main className={Styles.main}>
          <Sidebar
            isOpen={sidebarOpen}
            onClose={() => setSidebarOpen(false)}
            onToggle={() => setSidebarOpen((open) => !open)}
          />
          <section className={Styles.content}>
            <p className={Styles.loading}>Carregando perfil...</p>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className={Styles.page}>
      <main className={Styles.main}>
        <Sidebar
          isOpen={sidebarOpen}
          onClose={() => setSidebarOpen(false)}
          onToggle={() => setSidebarOpen((open) => !open)}
        />

        <section className={Styles.content}>
          <div className={Styles.container}>
            <button className={Styles.back} onClick={() => navigate(-1)}>
              <ArrowLeft size={16} /> Voltar
            </button>

            <div className={Styles.heading}>
              <h1 className={Styles.title}>Meu Perfil</h1>
              <p className={Styles.subtitle}>Gerencie suas informações pessoais</p>
            </div>

            <div className={Styles.panel}>
              <div className={Styles.hero}>
                <div className={Styles.heroIcon}>
                  <UserCircle2 size={28} />
                </div>
                <div className={Styles.heroMeta}>
                  <p className={Styles.heroLabel}>Conta</p>
                  <p className={Styles.heroName}>{profile?.name}</p>
                  <p className={Styles.heroEmail}>{profile?.email}</p>
                </div>
              </div>

              <div className={Styles.sectionHeading}>Resumo</div>

              <div className={Styles.infoList}>
                <div className={Styles.infoItem}>
                  <div className={Styles.infoIcon}>
                    <UserCircle2 size={20} />
                  </div>
                  <div className={Styles.infoCopy}>
                    <p className={Styles.infoLabel}>Nome</p>
                    <p className={Styles.infoValue}>{profile?.name}</p>
                  </div>
                </div>

                <div className={Styles.infoItem}>
                  <div className={Styles.infoIcon}>
                    <Mail size={18} />
                  </div>
                  <div className={Styles.infoCopy}>
                    <p className={Styles.infoLabel}>E-mail</p>
                    <p className={Styles.infoValue}>{profile?.email}</p>
                  </div>
                </div>

                <div className={Styles.infoItem}>
                  <div className={Styles.infoIcon}>
                    <CalendarDays size={18} />
                  </div>
                  <div className={Styles.infoCopy}>
                    <p className={Styles.infoLabel}>Membro desde</p>
                    <p className={Styles.infoValue}>
                      {new Date(profile?.createdAt || '').toLocaleDateString('pt-BR', {
                        month: 'long',
                        year: 'numeric',
                      })}
                    </p>
                  </div>
                </div>
              </div>

              <div className={Styles.buttonRow}>
                <button
                  className={Styles.primaryButton}
                  onClick={() => setSection(section === 'profile' ? 'none' : 'profile')}
                  disabled={saving}
                >
                  {section === 'profile' ? 'Fechar edição' : 'Editar perfil'}
                </button>

                <button
                  className={Styles.ghostButton}
                  onClick={() => setSection(section === 'password' ? 'none' : 'password')}
                  disabled={saving}
                >
                  Alterar senha
                </button>
              </div>

              {section === 'profile' && (
                <form className={Styles.editPanel} onSubmit={handleProfileSubmit}>
                  <div className={Styles.sectionHeading}>Editar informações</div>
                  <div className={Styles.form}>
                    <div className={Styles.inputGroup}>
                      <label className={Styles.infoLabel} htmlFor="name">Nome</label>
                      <input
                        id="name"
                        className={Styles.input}
                        value={formData.name}
                        onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                        minLength={3}
                        maxLength={50}
                        required
                      />
                    </div>

                    <div className={Styles.inputGroup}>
                      <label className={Styles.infoLabel} htmlFor="username">Username</label>
                      <input
                        id="username"
                        className={Styles.input}
                        value={formData.username}
                        onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                        minLength={3}
                        maxLength={20}
                        pattern="^[a-zA-Z0-9_]+$"
                        title="Apenas letras, números e underscore"
                        required
                      />
                      <p className={Styles.helper}>Use apenas letras, números ou underscore.</p>
                    </div>

                    <div className={`${Styles.inputGroup} md:col-span-2`}>
                      <label className={Styles.infoLabel} htmlFor="email">E-mail</label>
                      <input
                        id="email"
                        type="email"
                        className={Styles.input}
                        value={formData.email}
                        onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                        required
                      />
                    </div>
                  </div>

                  <div className={Styles.buttonRow}>
                    <button type="button" className={Styles.ghostButton} onClick={() => setSection('none')} disabled={saving}>
                      Cancelar
                    </button>
                    <button type="submit" className={Styles.primaryButton} disabled={saving}>
                      {saving ? 'Salvando...' : 'Salvar alterações'}
                    </button>
                  </div>
                </form>
              )}

              {section === 'password' && (
                <form className={Styles.editPanel} onSubmit={handlePasswordSubmit}>
                  <div className={Styles.sectionHeading}>
                    <ShieldCheck size={16} />
                    <span>Atualizar senha</span>
                  </div>
                  <div className="space-y-4">
                    <div className={Styles.inputGroup}>
                      <label className={Styles.infoLabel} htmlFor="newPassword">Nova senha</label>
                      <div className={Styles.inputWrapper}>
                        <button
                          type="button"
                          className={Styles.togglePassword}
                          onClick={() => setShowNewPassword((prev) => !prev)}
                          aria-label={showNewPassword ? 'Ocultar senha' : 'Mostrar senha'}
                        >
                          {showNewPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                        </button>
                        <input
                          id="newPassword"
                          type={showNewPassword ? 'text' : 'password'}
                          className={Styles.input}
                          value={formData.newPassword}
                          onChange={(e) => setFormData({ ...formData, newPassword: e.target.value })}
                          minLength={8}
                          maxLength={50}
                        />
                      </div>
                      <p className={Styles.helper}>Recomendamos senha com letras maiúsculas, minúsculas, números e símbolos.</p>
                    </div>

                    <div className={Styles.inputGroup}>
                      <label className={Styles.infoLabel} htmlFor="confirmPassword">Confirmar senha</label>
                      <div className={Styles.inputWrapper}>
                        <button
                          type="button"
                          className={Styles.togglePassword}
                          onClick={() => setShowConfirmPassword((prev) => !prev)}
                          aria-label={showConfirmPassword ? 'Ocultar senha' : 'Mostrar senha'}
                        >
                          {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                        </button>
                        <input
                          id="confirmPassword"
                          type={showConfirmPassword ? 'text' : 'password'}
                          className={Styles.input}
                          value={formData.confirmPassword}
                          onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                        />
                      </div>
                    </div>
                  </div>

                  <div className={Styles.buttonRow}>
                    <button type="button" className={Styles.ghostButton} onClick={() => setSection('none')} disabled={saving}>
                      Cancelar
                    </button>
                    <button type="submit" className={Styles.primaryButton} disabled={saving}>
                      {saving ? 'Salvando...' : 'Salvar senha'}
                    </button>
                  </div>
                </form>
              )}

              <div className={Styles.meta}>
                {profile?.updatedAt && (
                  <p>Última atualização em {new Date(profile.updatedAt).toLocaleDateString('pt-BR')}</p>
                )}
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
