# Roadmap do OrionTask

Este documento lista as correções necessárias e sugestões de novas funcionalidades para o projeto OrionTask, baseado na análise do código atual (Frontend React/Vite e Backend Spring Boot).

## 🐛 Correções e Melhorias Técnicas (Fixes)

### 1. Timer de 2h para Tarefas "Depois" (Prioridade Alta)
**Problema:** Atualmente, mover uma tarefa para "Depois" (`TaskStatus.NEXT`) apenas altera o status, removendo-a da visualização "Agora".
**Solução Planejada:**
- Fazer com que tarefas marcadas como "Depois" desapareçam da lista "Agora" por 2 horas e, após esse período, retornem automaticamente.
- **Backend:**
    - Adicionar um campo `snoozedUntil` (Timestamp) ou `lastMovedToNextAt` na entidade `Task`.
    - Opção A (Job Agendado): Um job (Spring Scheduler) roda a cada minuto verificar tasks `NEXT` cujo tempo expirou e move para `NOW`.
    - Opção B (Query Inteligente): Ao buscar tasks `NOW`, incluir também tasks `NEXT` cujo `snoozedUntil` < `agora`.
- **Frontend:**
    - Atualizar a interface para refletir que a task foi "adiada" e não apenas movida indefinidamente (feedback visual).

### 2. Validação e Segurança
- **Verificação de Propriedade:** Garantir que todas as operações de `TasksController` (edit, move, delete) validem estritamente se o `userId` do token corresponde ao dono da task no Service layer (análise preventiva).

### 3. Otimização de Queries
- Revisar paginação em `getTasksByDharma` e `getTasksByUserAndStatus` para garantir índices adequados no banco de dados, especialmente se o volume de tasks crescer.

---

## 🚀 Novas Features Sugeridas

### 1. Sistema de Notificações
- Notificar o usuário quando uma task "Depois" retornar para "Agora".
- **Implementação:** WebSockets ou Polling no frontend + Toast notifications.

### 2. Configuração de Snooze Personalizada
- Permitir que o usuário escolha o tempo de adiamento (não fixo em 2h).
- Opções: "1h", "2h", "Amanhã", "Próxima Semana".

### 3. Gamificação Expandida
- O sistema já possui `KarmaBadge`.
- **Ideia:** Criar um "Ranking de Karma" ou "Streak" (dias seguidos completando tasks).
- Visualização de estatísticas de produtividade na Home.

### 4. Colaboração em Dharmas (Shared Dharmas)
- Frontend para convidar usuários para um Dharma.
- Log de atividades (quem completou qual task em Dharmas compartilhados).

---

## Estrutura Atual (Resumo da Análise)
- **Frontend:** React + Vite + TypeScript + Tailwind + Zustand.
- **Backend:** Java (Spring Boot) + Gradle.
- **Pontos de Atenção:** Lógica de `AgoraPage.tsx` depende de `TaskStatus.NOW`. A implementação do timer exigirá mudanças coordenadas no Backend.
