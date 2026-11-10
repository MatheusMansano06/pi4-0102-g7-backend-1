# CT13: registrar e consultar notificações

| Linha | Valor |
| :--- | :--- |
| Fronteira | Backend ↔ Banco de dados (interno; não é HTTP) |
| Quem chama | Backend API |
| Quem responde | Banco de dados PostgreSQL |
| Atende | E6 e E7 |
| Como chamar | Operações internas de inclusão e consulta de notificações |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| usuarioId | inteiro | sim ao registrar destinatário | 12 |
| solicitacaoId | inteiro | não | 101 |
| tipo | texto | sim | PEDIDO_PRONTO |
| mensagem | texto | sim | Seu pedido está pronto para retirada |
| criadaEm | data e hora | gerada no registro | 2026-10-13T12:15:00 |
| lida | booleano | sim, com valor inicial proposto | false |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| notificacaoId | inteiro | sim após criação | 55 |
| usuarioId | inteiro | sim | 12 |
| solicitacaoId | inteiro | não | 101 |
| tipo | texto | sim | PEDIDO_PRONTO |
| mensagem | texto | sim | Seu pedido está pronto para retirada |
| criadaEm | data e hora | sim | 2026-10-13T12:15:00 |
| lida | booleano | sim | false |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Destinatário inexistente | Backend trata como referência inválida |
| Solicitação associada inexistente | Backend trata como referência inválida |
| Falha ao registrar ou consultar | Falha de persistência tratada pelo Backend |

## Consulta por destinatário (#1161)

`ConsultaDeNotificacoes.listarDoUsuario(usuarioId, somenteNaoLidas)` é a
interface interna usada pelo CT07. O usuarioId vem da identidade validada.
A implementação de banco deve restringir a leitura ao destinatário em todos
os casos e, com `somenteNaoLidas=true`, selecionar apenas registros com
`lida=false`. Não incluir outros destinatários no resultado, inclusive para
o perfil RESPONSAVEL.

A consulta retorna os campos do CT07, sem expor usuarioId no JSON, e devolve
uma lista vazia se não houver avisos correspondentes. Não modifica o estado
dos avisos. O esquema físico e a implementação dessa leitura aguardam H01.2.
O registro de avisos continua sendo responsabilidade de H09.1.

A [prova local do CT07](CT07-listarNotificacoes.md#prova-local-da-consulta)
usa nomes físicos exclusivos de uma tabela temporária para exercitar o
filtro no PostgreSQL. Esses nomes não definem a migração oficial.

