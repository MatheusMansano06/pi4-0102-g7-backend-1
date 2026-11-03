# CT02: listarMateriais

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E2 e E5 |
| Como chamar | `GET /api/materiais?busca={termo}` |
| Autenticação proposta | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| busca | texto | não | madeira |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| corpo JSON | lista direta de objetos | sim | [] quando não houver material |
| materialId | inteiro | sim, por material | 7 |
| nome | texto | sim, por material | Madeira |
| descricao | texto | não | Chapa para maquete |
| especificacao | texto | não | 12 mm · 600 × 400 mm |
| statusEstoque | DISPONIVEL, BAIXO ou INDISPONIVEL | sim | DISPONIVEL |
| parametrosAceitos | lista proposta de DIMENSAO e/ou DIAMETRO | a validar | ["DIMENSAO"] |
| parametrosObrigatorios | subconjunto proposto de parametrosAceitos | a validar | ["DIMENSAO"] |
| quantidadeDisponivel | inteiro | sim, por material | 24 |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| Falha ao consultar o banco | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida segue a proposta comum de HTTP 401 / NAO_AUTENTICADO.

## Forma da resposta e parâmetros

O corpo é uma lista direta, como no controller atual; não é um objeto envolvendo
materiais. Cada item possui os campos acima. Os metadados de parâmetros são uma
proposta nova, ainda ausente no DTO; consultar [materiais e pedidos](../especificacao/materiais-e-pedidos.md).
A busca implementada filtra nome; a busca também por especificação na tela é uma
divergência a alinhar antes de prometer esse comportamento na API.

Saldo zero produz INDISPONIVEL; saldo positivo até quantidadeMinima produz BAIXO;
acima disso, DISPONIVEL. A interface adapta os nomes para rótulos/classes CSS.
A exigência de autenticação desta ficha ainda não está implementada no GET atual.
