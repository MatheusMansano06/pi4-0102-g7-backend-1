# CT12: consultar e persistir solicitações

| Linha | Valor |
| :--- | :--- |
| Fronteira | Backend ↔ Banco de dados (interno; não é HTTP) |
| Quem chama | Backend API |
| Quem responde | Banco de dados PostgreSQL |
| Atende | E3, E4, E6, E8 e E9 |
| Como chamar | Operações internas de inclusão, consulta e atualização de solicitações |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro | sim para consultar/atualizar um pedido existente | 101 |
| usuarioId | inteiro | sim ao associar pedido ao aluno | 12 |
| materialId | inteiro | sim ao criar pedido | 7 |
| quantidade | inteiro positivo | sim ao criar pedido | 2 |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO | sim ao criar/alterar | PENDENTE |
| previsaoRetirada | data e hora | não | 2026-10-15T14:00:00 |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro | sim após criação | 101 |
| usuarioId | inteiro | sim | 12 |
| materialId | inteiro | sim | 7 |
| quantidade | inteiro | sim | 2 |
| status | texto | sim | PENDENTE |
| criadaEm | data e hora | sim | 2026-10-13T12:00:00 |
| atualizadaEm | data e hora | sim após alteração | 2026-10-13T12:10:00 |
| previsaoRetirada | data e hora | não | 2026-10-15T14:00:00 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Solicitação não encontrada | Backend trata como `SOLICITACAO_NAO_ENCONTRADA` |
| Dados inválidos | Backend rejeita a operação e informa a validação correspondente |
| Falha ao consultar ou gravar | Falha de persistência tratada pelo Backend |

**Observação:** o banco não define sozinho as permissões ou as transições de status; o Backend deve validar as regras antes de gravar.
