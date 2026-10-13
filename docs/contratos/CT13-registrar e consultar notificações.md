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

**Observação:** CT13 é uma operação interna de persistência; o contrato HTTP para consultar notificações é CT07.
