# CT08: listarHistorico

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E8 |
| Como chamar | `GET /api/solicitacoes/historico` |
| Autenticação proposta | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

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

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| Tentativa de acessar registros não autorizados | 403 | Erro `ACESSO_NEGADO` |
| Falha ao consultar histórico | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida segue a proposta comum de HTTP 401 / NAO_AUTENTICADO.

Proposta de resposta: HTTP 200 com objeto { "solicitacoes": [] } quando vazio.
ALUNO só acessa seus pedidos. O alcance do RESPONSAVEL (global ou restrito) e
quais estados entram no histórico precisam de confirmação com o responsável pelo
módulo. Não usar um usuarioId livre da tela para ampliar a consulta CT12. A rota
não existe na main; esta revisão não implementa filtros novos.
