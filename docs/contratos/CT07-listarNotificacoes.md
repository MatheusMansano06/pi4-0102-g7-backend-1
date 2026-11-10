# CT07: listarNotificacoes

> Entrega inicial #1161: contrato HTTP e serviço testados com consulta simulada.
> A rota continua desativada por padrão até ligar a leitura ao banco.

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E7 |
| Como chamar | `GET /api/notificacoes` |
| Autenticação | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| somenteNaoLidas | booleano na query (`true` ou `false`) | não; padrão `false` | `?somenteNaoLidas=true` |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| notificacoes | lista de objetos Notificacao | sim | lista de avisos do usuário |
| notificacaoId | inteiro | sim, por notificação | 55 |
| tipo | texto | sim, por notificação | PEDIDO_PRONTO |
| mensagem | texto | sim, por notificação | Seu pedido está pronto para retirada |
| criadaEm | data e hora (ISO 8601) | sim, por notificação | 2026-10-13T12:15:00 |
| lida | booleano | sim, por notificação | false |
| solicitacaoId | inteiro ou null | não | 101 |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| somenteNaoLidas diferente de true ou false, se informado | 400 | Erro `DADOS_INVALIDOS` |
| Falha ao consultar notificações | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida recebe HTTP 401 / NAO_AUTENTICADO.

Sucesso: HTTP 200 com objeto `{"notificacoes":[]}` quando vazio. Sem filtro ou
com `false`, a consulta inclui lidas e não lidas. Com `true`, pede somente as
não lidas. A operação apenas consulta; não marca avisos como lidos.

O backend obtém usuarioId da identidade validada e o envia à consulta CT13.
ALUNO e RESPONSAVEL recebem somente seus próprios avisos. Parâmetros da URL
não escolhem outro destinatário. Ordenação e paginação não são definidas aqui.

Neste recorte, `archflow.notificacoes.em-desenvolvimento` fica ligada apenas
nos testes, que fornecem identidade interna e consulta simuladas. Não habilitar
a propriedade em um ambiente compartilhado: ainda falta implementar a consulta
com o esquema de H01.2, provar o isolamento no PostgreSQL e exercitar avisos
gerados por H09.1. A configuração padrão não registra a rota.
