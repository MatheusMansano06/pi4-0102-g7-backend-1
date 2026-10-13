# CT09: listarPrazos

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E9 |
| Como chamar | `GET /api/prazos` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nenhum | — | — | O Backend identifica o aluno autenticado |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacoes | lista de objetos SolicitacaoPrazo | sim | pedidos do aluno |
| solicitacaoId | inteiro | sim, por solicitação | 101 |
| status | texto | sim, por solicitação | PRONTO_PARA_RETIRADA |
| previsaoRetirada | data e hora (ISO 8601) | não | 2026-10-15T14:00:00 |
| atualizadaEm | data e hora (ISO 8601) | sim | 2026-10-13T12:10:00 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Usuário sem autenticação | Erro `NAO_AUTENTICADO` |
| Falha ao consultar prazos | Erro `ERRO_INTERNO` |
