# Documentação Técnica: Funcionalidade Projects

## 1. Visão Geral
* **Objetivo:** Fornecer um contexto de longo prazo para tarefas que exigem múltiplos passos, sem sobrecarregar o usuário.
* **Relação:**
    * **Tasks:** Ações imediatas e atômicas.
    * **Projects:** Agrupador de tasks com um objetivo comum.
    * **Milestones:** Marcadores de progresso dentro de um projeto.
* **Design:** 
    * Tasks = **O que fazer** (ação).
    * Projects = **Por que fazer** (contexto).
    * Milestones = **Onde chegamos** (marcos).

## 2. Escopo e Regras
* **O que faz:**
    * Agrupa tarefas por objetivo.
    * Permite visualizar o progresso via milestones.
    * Organiza o fluxo de trabalho em "contextos maiores" que o Dharma.
* **O que NÃO faz:**
    * Sem Visualização Kanban (evita distração visual).
    * Sem Timelines/Gantt (evita ansiedade com prazos).
    * Sem Dependências entre tarefas (evita bloqueios mentais).
    * Sem Automações complexas.

## 3. Modelo de Dados

### Project
* `id`: UUID / Long
* `user_id`: FK (Owner)
* `dharma_id`: FK (Contexto pai)
* `title`: String (máx 60 chars)
* `description`: String (máx 200 chars)
* `status`: Enum (ACTIVE, ARCHIVED)
* `created_at`: Timestamp

### Milestone
* `id`: UUID / Long
* `project_id`: FK
* `title`: String (máx 60 chars)
* `status`: Enum (OPEN, COMPLETED)
* `order`: Integer (ordenação manual simples)

### Task (Update)
* `project_id`: FK (Opcional)
* `milestone_id`: FK (Opcional)

## 4. Fluxos Principais
* **Criar projeto:** Nome, descrição curta e escolha do Dharma.
* **Listar projetos:** Visualização simples em cards ou lista, filtrado por Dharma.
* **Visualizar projeto:** Lista de tasks associadas e linha do tempo de milestones (vertical e simples).
* **Marcos (Milestones):** Criação rápida de "checkpoints". Concluir um marco visualiza progresso.
* **Associar Task:** No modal de criação/edição da task, selecionar projeto e milestone.
* **Arquivar:** Remove da visão principal sem deletar os dados (foco no que é atual).

## 5. Frontend
* **Telas:**
    * `ProjectsPage`: Lista de projetos ativos.
    * `ProjectDetailPage`: Foco total no projeto, reusando layout da tela "Agora".
* **Componentes:**
    * `ProjectCard`: Resumo com barra de progresso (milestones concluídas).
    * `MilestoneTrack`: Lista vertical simples de marcos.
* **Estados UI:**
    * `Empty`: Sugestão de primeiro projeto.
    * `Loading`: Shimmer simples.
    * `Archived`: Seção separada ou toggle na listagem.
* **Layout "Agora":** No detalhe do projeto, focar apenas nas tasks "Today" daquele projeto.

## 6. Backend / API
* `GET /api/projects`: Lista projetos do usuário.
* `POST /api/projects`: Cria projeto.
* `GET /api/projects/{id}`: Detalhes + tasks + milestones.
* `PATCH /api/projects/{id}/archive`: Arquivamento lógico.
* `POST /api/projects/{id}/milestones`: Adiciona marco.
* **Validação:** Títulos curtos obrigatórios; Projeto deve pertencer ao mesmo usuário do Dharma.

## 7. Integração com Dharma / Karma
* **Dharma:** O projeto sempre "herda" ou é categorizado por um Dharma (ex: Projeto "Site Novo" dentro do Dharma "Trabalho").
* **Karma:** 
    * O projeto serve como agrupador de contexto para o Karma.
    * Concluir tarefas e marcos dentro de um projeto contribui para a visão de "Esforço por Projeto".
    * Sem bônus numéricos; o foco é o registro histórico da energia dedicada ao projeto.

## 8. Não-objetivos
* Colaboração/Times (foco individual).
* Notificações de prazos de projeto (foco em execução, não em cobrança).
* Dashboard de métricas complexas.

## 9. MVP
* **Obrigatório:** Criar/Listar Projetos, Associar Task a Projeto, Arquivar.
* **Posterior:** Milestones, Ordenação de projetos, Filtros avançados.

## 10. Checklist de Implementação

### Backend
- [ ] Criar entidades [Project](#project) e [Milestone](#milestone) (Java JPA)
- [ ] Criar repositórios para Project e Milestone
- [ ] Atualizar entidade [Task](#task-update) com FKs de Project e Milestone
- [ ] Implementar [Endpoints de Projeto](#6-backend--api) (Controller e Service)
- [ ] Adicionar validações de propriedade (User -> Dharma -> Project)

### Frontend
- [ ] Criar página de [Lista de Projetos](#5-frontend)
- [ ] Implementar [ProjectCard](#componentes) com barra de progresso
- [ ] Criar página de [Detalhes do Projeto](#telas) (reuso do layout Agora)
- [ ] Implementar [MilestoneTrack](#componentes) (linha do tempo vertical)
- [ ] Integrar seleção de Projeto/Milestone no modal de criação de Task

