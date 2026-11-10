# CT09: listarPrazos

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E9 |
| Como chamar | `GET /api/prazos` |
| Autenticação prevista para esta rota | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

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

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| Falha ao consultar prazos | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida recebe HTTP 401 / NAO_AUTENTICADO.

Proposta de resposta: HTTP 200 com objeto { "solicitacoes": [] } quando vazio.
Identificar o ALUNO pela credencial e consultar apenas seus pedidos no CT12;
perfil não permitido retorna 403 / ACESSO_NEGADO. Previsão ausente permanece null,
sem data inventada. A rota não existe na main. Definir com o módulo quais estados
do pedido compõem o painel; não confundir esta ficha com o histórico completo.
