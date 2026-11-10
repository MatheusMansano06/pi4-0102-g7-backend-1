# CT07: listarNotificacoes

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E7 |
| Como chamar | `GET /api/notificacoes` |
| Autenticação prevista para esta rota | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| somenteNaoLidas | booleano | não | false |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| notificacoes | lista de objetos Notificacao | sim | lista de avisos do usuário |
| notificacaoId | inteiro | sim, por notificação | 55 |
| tipo | texto | sim, por notificação | PEDIDO_PRONTO |
| mensagem | texto | sim, por notificação | Seu pedido está pronto para retirada |
| criadaEm | data e hora (ISO 8601) | sim, por notificação | 2026-10-13T12:15:00 |
| lida | booleano | sim, por notificação | false |
| solicitacaoId | inteiro | não | 101 |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| Falha ao consultar notificações | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida recebe HTTP 401 / NAO_AUTENTICADO.

Proposta de resposta: HTTP 200 com objeto { "notificacoes": [] } quando vazio;
cada item contém os campos da ficha. O filtro somenteNaoLidas tem padrão false.
O backend obtém usuarioId da credencial validada e o usa na consulta CT13; a tela
não escolhe livremente o destinatário. A ordem temporal e a paginação devem ser
alinhadas com o módulo antes de acrescentar parâmetros. A rota não existe na main.
