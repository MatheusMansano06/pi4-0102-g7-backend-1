# CT08: listarHistorico

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E8 |
| Como chamar | `GET /api/solicitacoes/historico` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nenhum | — | — | O Backend identifica o usuário autenticado |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacoes | lista de objetos SolicitacaoHistorico | sim | histórico permitido ao usuário |
| solicitacaoId | inteiro | sim, por solicitação | 101 |
| material | texto ou objeto Material | sim, por solicitação | Madeira |
| quantidade | inteiro | sim, por solicitação | 2 |
| status | texto | sim, por solicitação | CONCLUIDO |
| criadaEm | data e hora (ISO 8601) | sim, por solicitação | 2026-10-10T09:00:00 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Usuário sem autenticação | Erro `NAO_AUTENTICADO` |
| Tentativa de acessar registros não autorizados | Erro `ACESSO_NEGADO` |
| Falha ao consultar histórico | Erro `ERRO_INTERNO` |
