# Documentação Técnica: Sistema de Karma (Awareness-Based)

## 1. Visão Geral
* **Objetivo:** Funcionar como um "espelho" do esforço despendido, promovendo autoconhecimento sobre a natureza do trabalho realizado, sem a pressão de metas ou pontuações.
* **Princípio:** Karma é a categorização da energia. Ele ajuda o usuário a entender se sua semana foi voltada para "fazer" (Action), "conectar" (People) ou "planejar" (Thinking).
* **Categorias:**
    * **ACTION:** Execução, tarefas operacionais.
    * **PEOPLE:** Social, comunicação, reuniões.
    * **THINKING:** Estratégia, estudo, criatividade.

## 2. Escopo e Regras
* **O que faz:**
    * Registra a categoria da tarefa no momento da conclusão.
    * Agrupa o volume de tarefas por categoria em períodos (Hoje, Esta Semana).
    * Oferece uma visão qualitativa da distribuição de esforço.
* **O que NÃO faz:**
    * Sem pontos, multiplicadores ou níveis.
    * Sem recompensas visuais "explosivas" (confetes, popups).
    * Sem elementos de comparação ou rankings.
    * Sem barra de progresso de "experiência".

## 3. Modelo de Dados

### User (Update)
Simplificado para contadores de ocorrências ou metadados de conclusão:
* `count_action`: Integer (Contagem total histórica)
* `count_people`: Integer
* `count_thinking`: Integer

### Task (Existente)
* `karma_type`: Enum (ACTION, PEOPLE, THINKING)
* `effort_level`: Enum (LOW, MEDIUM, HIGH) - Agora usado apenas como rótulo de "tamanho da entrega", não valor numérico.

## 4. Fluxos Principais
* **Registro de Conclusão:**
    * Ao finalizar uma task, o sistema apenas incrementa o contador da categoria correspondente no perfil do usuário.
    * O `effort_level` serve como um metadado descritivo (ex: "Você concluiu uma tarefa de alta complexidade em Ação").
* **Consulta de Histórico:**
    * O sistema sumariza: "Hoje você realizou 3 tarefas de Ação e 1 de Reflexão".

## 5. Frontend
* **Feedback Visual:**
    * Apenas uma confirmação silenciosa (check).
    * No perfil, uma lista de texto simples ou rótulos indicando a "Tendência Atual" (ex: "Foco predominante: People").
* **Componentes:**
    * `ActivitySummary`: Texto descritivo do saldo de atividades do dia/semana.
* **Estados UI:**
    * `Empty`: "Nenhuma atividade registrada ainda."
    * `Active`: "Atividade recente: 80% Action."

## 6. Backend / API
* **Processamento:**
    * Ao mudar o status para `DONE`, o service atualiza os contadores do usuário.
* **Endpoints:**
    * `GET /api/users/me/activity-log`: Retorna o sumário de contagens por categoria.

## 7. Filosofia ADHD (Sem Gamificação)
* **Redução de Ansiedade:** Elimina o medo de "não pontuar o suficiente" ou de "perder pontos" por inatividade.
* **Foco em Realidade:** Mostra o que FOI feito, não o que falta para um nível imaginário.
* **Auto-regulação:** O usuário percebe sozinho se está negligenciando uma área (ex: "Só estou fazendo Thinking e nada de Action").

## 8. Não-objetivos
* Distintivos (Badges) ou Conquistas.
* Gráficos complexos (Pizza, Linhas).
* Qualquer tipo de sistema de XP.

## 9. MVP
* **Obrigatório:** Adicionar contadores ao modelo do Usuário; lógica de incremento ao concluir task; endpoint de sumário simples.
* **Posterior:** Tendências semanais (comparativo qualitativo entre semana atual e anterior).

## 10. Checklist de Implementação

### Backend
- [ ] Atualizar a entidade [User](#user-update) com os contadores de categoria
- [ ] Criar migração SQL (Flyway) para novos campos de contador
- [ ] Alterar o [Service de Task](#6-backend--api) para incrementar contadores ao marcar como `DONE`
- [ ] Implementar [endpoint /activity-log](#endpoints)

### Frontend
- [ ] Implementar componente [ActivitySummary](#componentes) no Perfil
- [ ] Adicionar rótulos de [Tendência Atual](#feedback-visual) baseado nos contadores
- [ ] Criar estado [Empty](#estados-ui) para quando não houver atividades

