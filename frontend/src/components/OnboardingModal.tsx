import { X, Check, Target, Zap, Shield, HelpCircle } from 'lucide-react';
import { useEffect, useState } from 'react';

interface OnboardingModalProps {
    isOpen: boolean;
    onClose: () => void;
}

export function OnboardingModal({ isOpen, onClose }: OnboardingModalProps) {
    const [shouldRender, setShouldRender] = useState(false);


    useEffect(() => {
        if (isOpen) setShouldRender(true);
    }, [isOpen]);

    const handleClose = () => {
        onClose();
    };

    const handleAnimationEnd = () => {
        if (!isOpen) setShouldRender(false);
    };

    if (!shouldRender) return null;

    return (
        <div
            className={`${Styles.overlay} ${isOpen ? 'opacity-100' : 'opacity-0 pointer-events-none'}`}
            onAnimationEnd={handleAnimationEnd}
        >
            <div className={`${Styles.modal} ${isOpen ? 'scale-100 opacity-100' : 'scale-95 opacity-0'}`}>
                <button onClick={handleClose} className={Styles.closeButton} title="Fechar">
                    <X size={20} />
                </button>

                <div className={Styles.header}>
                    <div className={Styles.iconWrapper}>
                        <HelpCircle size={32} />
                    </div>
                    <div>
                        <h2 className={Styles.title}>Como o Orion Task funciona</h2>
                        <p className={Styles.subtitle}>Guia rápido dos conceitos essenciais</p>
                    </div>
                </div>

                <div className={Styles.content}>
                    <div className={Styles.section}>
                        <div className={`${Styles.sectionIcon} text-blue-500 bg-blue-500/10`}>
                            <Target size={20} />
                        </div>
                        <div>
                            <h3 className={Styles.sectionTitle}>Dharmas</h3>
                            <p className={Styles.sectionText}>
                                São suas grandes áreas de vida ou propósitos (ex: Saúde, Carreira).
                                Você pode ter no máximo <strong>8 Dharmas</strong>.
                            </p>
                        </div>
                    </div>

                    <div className={Styles.section}>
                        <div className={`${Styles.sectionIcon} text-amber-500 bg-amber-500/10`}>
                            <Check size={20} />
                        </div>
                        <div>
                            <h3 className={Styles.sectionTitle}>Tarefas</h3>
                            <p className={Styles.sectionText}>
                                O fluxo é simples: <strong>Agora</strong> (fazer hoje) → <strong>Depois</strong> (fazer futuramente) → <strong>Concluído</strong>.
                            </p>
                        </div>
                    </div>

                    <div className={Styles.section}>
                        <div className={`${Styles.sectionIcon} text-purple-500 bg-purple-500/10`}>
                            <Zap size={20} />
                        </div>
                        <div>
                            <h3 className={Styles.sectionTitle}>Agora (Foco)</h3>
                            <p className={Styles.sectionText}>
                                Sua lista de foco imediato tem um limite de <strong>5 tarefas</strong>.
                                Isso ajuda você a não se sobrecarregar e focar no que importa.
                            </p>
                        </div>
                    </div>

                    <div className={Styles.section}>
                        <div className={`${Styles.sectionIcon} text-emerald-500 bg-emerald-500/10`}>
                            <Shield size={20} />
                        </div>
                        <div>
                            <h3 className={Styles.sectionTitle}>Privacidade</h3>
                            <p className={Styles.sectionText}>
                                Use o ícone de olho para ocultar Dharmas sensíveis da visualização
                                rápida. Suas tarefas privadas ficarão escondidas até você mostrar novamente.
                            </p>
                        </div>
                    </div>
                </div>

                <div className={Styles.footer}>
                    <button onClick={handleClose} className={Styles.primaryButton + ' w-full'}>
                        Entendi, vamos começar!
                    </button>
                </div>
            </div>
        </div>
    );
}

const Styles = {
    overlay: 'fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4 transition-opacity duration-300',
    modal: 'bg-card border border-surface w-full max-w-lg rounded-2xl shadow-2xl transition-all duration-300 transform overflow-hidden',
    closeButton: 'absolute top-4 right-4 text-nebula hover:text-stellar p-1 rounded-full hover:bg-surface transition-colors',
    header: 'p-6 md:p-8 flex items-start gap-4 border-surface bg-canvas/50',
    iconWrapper: 'w-12 h-12 rounded-xl bg-primary/10 text-primary flex items-center justify-center flex-shrink-0',
    title: 'text-xl md:text-2xl font-bold text-stellar',
    subtitle: 'text-sm text-nebula mt-1',
    content: 'p-6 md:p-8 space-y-6',
    section: 'flex gap-4',
    sectionIcon: 'w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0',
    sectionTitle: 'font-semibold text-stellar mb-1',
    sectionText: 'text-sm text-nebula leading-relaxed',
    footer: 'p-6 border-surface bg-canvas/30 flex justify-between items-center',
    checkboxContainer: 'flex items-center gap-2 cursor-pointer select-none',
    checkbox: 'w-4 h-4 rounded border-surface text-primary focus:ring-primary',
    checkboxLabel: 'text-sm text-nebula',
    primaryButton: 'bg-primary text-white px-6 py-2.5 rounded-xl font-semibold hover:bg-primary/90 transition-colors shadow-lg active:scale-95',
};
