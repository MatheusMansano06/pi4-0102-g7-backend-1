# CT04: listarFila

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E4 |
| Como chamar | `GET /api/solicitacoes/fila` |
| Autenticação | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO, CANCELADO | não | PENDENTE |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| corpo JSON | lista direta de SolicitacaoFila | sim | [] ou lista em criadaEm crescente |
| solicitacaoId | inteiro | sim, por solicitação | 101 |
| material | texto com o nome do material | sim, por solicitação | Madeira |
| quantidade | inteiro | sim, por solicitação | 2 |
| status | texto | sim, por solicitação | PENDENTE |
| criadaEm | data e hora (ISO 8601) | sim, por solicitação | 2026-10-13T12:00:00 |
| dimensao | texto | não, vai vazio quando o pedido não tem | 30x40 cm |
| diametro | número decimal | não, vai vazio quando o pedido não tem | 8.5 |
| observacao | texto | não, vai vazio quando o pedido não tem | Uso em maquete |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| Perfil diferente de RESPONSAVEL | 403 | Erro `ACESSO_NEGADO` |
| Falha ao consultar a fila | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida recebe HTTP 401 / NAO_AUTENTICADO.

Sucesso: HTTP 200, com lista direta conforme o controller publicado. Status fora
da enumeração retorna 400 / STATUS_INVALIDO. Sem filtro, o código atual exclui
CONCLUIDO, mas ainda inclui CANCELADO; excluir ambos os terminais é proposta a
confirmar com o responsável pela fila. Não apresentar a correção como implementada.
O perfil RESPONSAVEL é obtido do JWT emitido no CT01. X-Perfil não autentica.
