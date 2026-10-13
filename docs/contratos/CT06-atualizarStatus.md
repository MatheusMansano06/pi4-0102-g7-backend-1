# CT06: atualizarStatus

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E6 |
| Como chamar | `PATCH /api/solicitacoes/{solicitacaoId}/status` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro (na URL) | sim | 101 |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO | sim | PRONTO_PARA_RETIRADA |
| previsaoRetirada | data e hora (ISO 8601) | não | 2026-10-15T14:00:00 |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro | sim | 101 |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO | sim | PRONTO_PARA_RETIRADA |
| previsaoRetirada | data e hora (ISO 8601) | não | 2026-10-15T14:00:00 |
| atualizadaEm | data e hora (ISO 8601) | sim | 2026-10-13T12:10:00 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Usuário sem autenticação | Erro `NAO_AUTENTICADO` |
| Perfil diferente de RESPONSAVEL | Erro `ACESSO_NEGADO` |
| Solicitação inexistente | Erro `SOLICITACAO_NAO_ENCONTRADA` |
| Status inválido ou transição não permitida | Erro `STATUS_INVALIDO` |
| Previsão de retirada no passado | Erro `PREVISAO_INVALIDA` |
| Falha ao gravar status ou notificação | Erro `ERRO_INTERNO` |
