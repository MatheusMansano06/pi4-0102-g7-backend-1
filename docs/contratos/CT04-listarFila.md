# CT04: listarFila

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E4 |
| Como chamar | `GET /api/solicitacoes/fila` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO | não | PENDENTE |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacoes | lista de objetos SolicitacaoFila | sim | lista ordenada por data de criação |
| solicitacaoId | inteiro | sim, por solicitação | 101 |
| material | texto ou objeto Material | sim, por solicitação | Madeira |
| quantidade | inteiro | sim, por solicitação | 2 |
| status | texto | sim, por solicitação | PENDENTE |
| criadaEm | data e hora (ISO 8601) | sim, por solicitação | 2026-10-13T12:00:00 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Usuário sem autenticação | Erro `NAO_AUTENTICADO` |
| Perfil diferente de RESPONSAVEL | Erro `ACESSO_NEGADO` |
| Falha ao consultar a fila | Erro `ERRO_INTERNO` |
