# CT03: criarSolicitacao

> Implementação proposta no #1145: ver [H04.1](../H04.1-CRIAR-SOLICITACAO.md).
> A branch prepara POST/201 e persistência de medidas, mas depende de novas colunas,
> regras por material e identidade Keycloak. A ficha abaixo deve ser conciliada
> com a PR #10 antes de integrar; não comprova operação liberada na main.

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E3 |
| Como chamar | `POST /api/solicitacoes` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materialId | inteiro | sim | 7 |
| quantidade | inteiro | sim | 2 |
| dimensao | texto | não | 30 x 20 cm |
| diametro | decimal | não | 4.5 |
| observacao | texto | não | Uso em maquete |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro | sim | 101 |
| materialId | inteiro | sim | 7 |
| quantidade | inteiro | sim | 2 |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO | sim | PENDENTE |
| criadaEm | data e hora (ISO 8601) | sim | 2026-10-13T12:00:00 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Usuário sem autenticação | Erro `NAO_AUTENTICADO` |
| Material inexistente ou inativo | Erro `MATERIAL_NAO_ENCONTRADO` |
| Quantidade menor ou igual a zero | Erro `QUANTIDADE_INVALIDA` |
| Dados incompatíveis com o material | Erro `PARAMETROS_INVALIDOS` |
| Falha ao registrar solicitação | Erro `ERRO_INTERNO` |
