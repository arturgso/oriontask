# OrionTask Frontend

Frontend da aplicação OrionTask construído com React, TypeScript, Tailwind CSS e Vite.

## Stack

- **React 19** - UI library
- **TypeScript** - Type safety
- **Tailwind CSS 3.4.7** - Styling
- **Vite** - Build tool
- **React Router DOM** - Routing
- **Zustand** - State management
- **Lucide React** - Icons
- **React Hot Toast** - Notifications

## Filosofia

Este frontend segue princípios de design para TDAH:
- **Máximo 5 itens** por tela principal
- **Texto grande e claro** (frases até 8 palavras)
- **Botão único** por ação principal
- **Feedback imediato** ao interagir
- **Fundo neutro** com destaque apenas no item ativo
- **Animações discretas** (<200ms)

## Estrutura

```
src/
├── api/          # Cliente REST e endpoints
├── components/   # Componentes reutilizáveis
├── pages/        # Páginas principais
├── state/        # Zustand store
└── types/        # TypeScript types
```

## Rotas

- `/` - Login/Signup
- `/dharmas` - Lista de Dharmas (áreas de vida)
- `/tasks/:dharmaId` - Tasks de um Dharma específico
- `/agora` - Visualização de até 5 tasks para ação imediata

## Desenvolvimento

```bash
# Instalar dependências
yarn install

# Rodar em dev
yarn dev

# Build para produção
yarn build

# Preview do build
yarn preview
```

## API

O frontend se comunica com o backend em `http://localhost:8080/api/v1`.

Configure o backend antes de rodar o frontend.

## Padrão de Componentes

Todos os componentes seguem este padrão com const `Styles` no final:

```typescript
export function MeuComponente() {
  return (
    <div className={Styles.container}>
      <h1 className={Styles.title}>Título</h1>
    </div>
  );
}

const Styles = {
  container: 'classe-tailwind aqui',
  title: 'outra-classe',
};
```

## Limitações Intencionais

- Sem sub-tasks
- Sem datas de vencimento
- Sem notificações push (no MVP)
- Sem modo offline (no MVP)
- Máximo 8 Dharmas por usuário
- Máximo 5 Tasks em "Agora"
