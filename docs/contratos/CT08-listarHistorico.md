# CT08: listarHistorico

> Revisão #1136: resposta e acesso conforme a implementação atual. Ver [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E8 |
| Como chamar | `GET /api/solicitacoes/historico` |
| Autenticação | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nenhum | — | — | O Backend identifica o usuário autenticado |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| corpo | lista direta de objetos SolicitacaoHistorico | sim | `[]` quando vazio |
| solicitacaoId | inteiro | sim, por solicitação | 101 |
| material | texto | sim, por solicitação | Madeira |
| quantidade | inteiro | sim, por solicitação | 2 |
| status | texto | sim, por solicitação | CONCLUIDO |
| criadaEm | data e hora (ISO 8601) | sim, por solicitação | 2026-10-10T09:00:00 |
| atualizadaEm | data e hora (ISO 8601) ou null | sim, por solicitação | 2026-10-11T13:00:00 |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| Falha ao consultar histórico | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida recebe HTTP 401 / NAO_AUTENTICADO.

Sucesso: HTTP 200 com lista direta em ordem decrescente de criação, sem filtro
de status. ALUNO recebe somente seus pedidos, pelo usuarioId do token;
RESPONSAVEL recebe todos. A chamada não recebe usuarioId da tela. Filtros novos
não fazem parte desta revisão.
