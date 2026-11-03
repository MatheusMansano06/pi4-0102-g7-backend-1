# CT03: criarSolicitacao

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E3 |
| Como chamar | `POST /api/solicitacoes` |
| Autenticação proposta | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materialId | inteiro | sim | 7 |
| quantidade | inteiro | sim | 2 |
| dimensao | texto; proposta em mm | conforme política do material | 300 x 200 mm |
| diametro | decimal positivo; proposta em mm | conforme política do material | 4.5 |
| observacao | texto | não | Uso em maquete |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro | sim | 101 |
| materialId | inteiro | sim | 7 |
| quantidade | inteiro | sim | 2 |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO, CANCELADO | sim; criação retorna PENDENTE | PENDENTE |
| criadaEm | data e hora (ISO 8601) | sim | 2026-10-13T12:00:00 |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| Material inexistente ou inativo | 404 | Erro `MATERIAL_NAO_ENCONTRADO` |
| Quantidade ausente, não inteira ou menor ou igual a zero | 400 | Erro `QUANTIDADE_INVALIDA` |
| Dados incompatíveis com o material | 400 | Erro `PARAMETROS_INVALIDOS` |
| Falha ao registrar solicitação | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida segue a proposta comum de HTTP 401 / NAO_AUTENTICADO.

## Dados do pedido e persistência

Proposta de acesso: ALUNO autenticado; perfil não permitido retorna 403 / ACESSO_NEGADO.
materialId ausente e corpo ilegível retornam 400 / DADOS_INVALIDOS. Sucesso: HTTP 200
com os campos da saída; esse código é proposto para a operação ainda não implementada.

Largura e altura do modal compõem dimensao; não criar dois nomes novos no JSON
sem revisar a ficha. Unidade, formato, parâmetros aceitos/obrigatórios e exemplo
de corpo estão em [materiais e pedidos](../especificacao/materiais-e-pedidos.md).
As regras por material e os limites ainda aguardam validação, não inferência por nome.

O backend obtém usuarioId da identidade validada, define PENDENTE e os horários,
e entrega ao CT12 materialId, quantidade, dimensao, diametro e observacao. Não
descartar os opcionais no caminho para o banco. Baixa/reserva automática de saldo
não foi decidida nesta revisão. A main atual não implementa esse POST.
