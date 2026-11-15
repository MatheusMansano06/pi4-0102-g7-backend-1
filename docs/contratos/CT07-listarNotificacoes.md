# CT07: listarNotificacoes

> #1161 conciliado com a implementação oficial das PRs37/38. Consulta JPA e geração H09.1 integradas; prova HTTP com login/JWT/PostgreSQL reais em banco isolado.

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

A rota é registrada normalmente desde a PR37; não depende da flag antiga da
PR22. NotificacaoService consulta NotificacaoRepository sobre notificacoes,
com usuarioId da identidade. O campo físico criado_em vira criadaEm no JSON.
Filtro informado vazio, 1/0, on ou letras maiúsculas é recusado com400 e
DADOS_INVALIDOS; somente true/false são aceitos.

## Prova integrada e reprodução

Ver [resultado e roteiro oficial](../integracao/H09.2-retomada-jwt.md).
NotificacaoBancoTest usa servidor HTTP em porta aleatória, login BCrypt/JWT,
JPA e as quatro tabelas do SQL oficial. Não usa consulta/tabela temporária.
Os testes MockMvc de contrato e assinatura continuam complementares.

As provas antigas de11/11 e a preparação de15/11 com consulta simulada estão
preservadas no histórico Git; não são evidência da implementação atual.
A leitura não cria avisos nem altera lida. Marcação de leitura é outro escopo.
