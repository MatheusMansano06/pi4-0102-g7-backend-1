# CT06: atualizarStatus

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E6 |
| Como chamar | `PATCH /api/solicitacoes/{solicitacaoId}/status` |
| Autenticação | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro (na URL) | sim | 101 |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO, CANCELADO | sim | PRONTO_PARA_RETIRADA |
| previsaoRetirada | data e hora (ISO 8601) | não | 2026-10-15T14:00:00 |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro | sim | 101 |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO, CANCELADO | sim | PRONTO_PARA_RETIRADA |
| previsaoRetirada | data e hora (ISO 8601) | não | 2026-10-15T14:00:00 |
| atualizadaEm | data e hora (ISO 8601) | sim | 2026-10-13T12:10:00 |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| Perfil diferente de RESPONSAVEL | 403 | Erro `ACESSO_NEGADO` |
| Solicitação inexistente | 404 | Erro `SOLICITACAO_NAO_ENCONTRADA` |
| Status inválido ou transição não permitida | 400 | Erro `STATUS_INVALIDO` |
| Previsão de retirada no passado | 400 | Erro `PREVISAO_INVALIDA` |
| Falha ao gravar status ou notificação | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida recebe HTTP 401 / NAO_AUTENTICADO.

## Transições e estado da implementação

| Origem | Destinos do modelo atual |
| --- | --- |
| PENDENTE | EM_SEPARACAO, AGUARDANDO_COMPRA, CANCELADO |
| EM_SEPARACAO | PRONTO_PARA_RETIRADA, AGUARDANDO_COMPRA, PENDENTE, CANCELADO |
| AGUARDANDO_COMPRA | EM_SEPARACAO, CANCELADO |
| PRONTO_PARA_RETIRADA | CONCLUIDO, EM_SEPARACAO, CANCELADO |
| CONCLUIDO | nenhum |
| CANCELADO | nenhum |

Sucesso: HTTP 200. A data da alteração é gravada via CT12. A previsão é opcional;
na implementação atual, ausência/null conserva a previsão já existente. O
perfil RESPONSAVEL vem do token. A criação de notificação CT13 continua
pendente, embora status/transições já existam.
