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

O esquema oficial da PR37 cria notificacoes, com notificacao_id, usuario_id,
solicitacao_id opcional, tipo, mensagem, lida e criado_em. FKs ligam destinatário
a usuarios e solicitação a solicitacoes. criado_em é exposto como criadaEm.

NotificacaoRepository restringe consulta por usuarioId em todos os casos;
findByUsuarioIdAndLidaFalseOrderByCriadoEmDesc também filtra lida=false.
O destinatário vem do JWT, inclusive para RESPONSAVEL. A lista retorna os
campos de CT07, sem usuarioId; não cria nem marca avisos como lidos.

H09.1/Lara registra avisos no atualizarStatus transacional. A prova #1161
exercita a geração oficial e força falha de INSERT somente no banco descartável
para comprovar rollback de status/previsão/data e ausência do aviso.
A produção integrada gera avisos para outros status além dos dois mínimos
AGUARDANDO_COMPRA e PRONTO_PARA_RETIRADA; conciliar documentação H01.5/#1137.

[Prova HTTP e PostgreSQL oficiais](../integracao/H09.2-retomada-jwt.md).
As tabelas temporárias antigas não definem mais o adaptador de produção e
foram retiradas desta branch ao conciliar a entrega oficial.
