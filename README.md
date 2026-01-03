# OrionTask

Gerenciador de tarefas simples baseado em propósito para pessoas com TDAH.

## O que é

OrionTask ajuda você a organizar tarefas por áreas de vida em vez de listas infinitas. Cada tarefa mostra consequências claras, tornando decisões mais fáceis.

## Filosofia

O sistema se baseia em três conceitos:

### Dharma (Propósito)
Cada tarefa pertence a uma área de vida (Dharma): Saúde, Família, Aprendizado, etc. Isso força decisão por propósito, não por urgência falsa. Sem Dharma definido, a tarefa não pode ser salva. Exemplo: "Beber água" pertence ao Dharma "Saúde".

### Karma (Consequência)
Toda tarefa mostra o ganho se você fizer e a perda se não fizer. Textos curtos (máx 40 caracteres) e diretos. Exemplo: Fazer caminhada → "Mais energia" | Não fazer → "Sono ruim". Isso motiva ação imediata ao tornar consequências visíveis.

### KISS (Keep It Simple, Stupid)
Complexidade paralisa. O app limita escolhas propositalmente: máximo 5 tarefas visíveis por vez, sem sub-tarefas, sem prioridades numéricas, sem filtros avançados. Menos decisões = mais ação.

## Para quem é

- Pessoas com TDAH que querem agir sem complexidade
- Quem prefere decidir por propósito, não por urgência
- Uso pessoal (não corporativo)

## O que faz

- **Dharmas:** organize tarefas por áreas de vida (Saúde, Família, Aprendizado, etc)
- **Karma:** veja consequências claras (ganho vs perda) para cada tarefa
- **Agora:** máximo 5 tarefas visíveis por vez (evita paralisia)
- **Simples:** sem sub-tarefas, sem filtros complexos, sem agenda detalhada

## Estrutura do projeto

```
backend/          # Spring Boot + PostgreSQL
```

## Como executar

### Backend

```bash
cd backend
./gradlew bootRun
```

### Frontend

Em desenvolvimento.

## Tecnologias

- **Backend:** Spring Boot 3, PostgreSQL/H2, JWT
- **Frontend:** Em desenvolvimento
- **Comunicação:** REST API

## Princípios

1. **KISS:** manter simples sempre
2. **Máximo 5 tarefas visíveis:** evitar paralisia
3. **Toda tarefa precisa de Dharma:** decisão por propósito
4. **Karma curto:** textos até 40 caracteres
5. **Sem prioridades numéricas:** apenas agora, próximo, aguardando

## O que NÃO faz

- Não substitui terapia ou medicação
- Não tem sub-tarefas ou Kanban complexo
- Não tem agenda minuto a minuto
- Não tem notificações excessivas

---

**Status:** MVP em desenvolvimento
