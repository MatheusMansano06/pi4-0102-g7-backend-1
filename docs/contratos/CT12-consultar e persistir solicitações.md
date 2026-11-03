# CT12: consultar e persistir solicitações

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | Backend ↔ Banco de dados (interno; não é HTTP) |
| Quem chama | Backend API |
| Quem responde | Banco de dados PostgreSQL |
| Atende | E3, E4, E6, E8 e E9 |
| Como chamar | Operações internas de inclusão, consulta e atualização de solicitações |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro | sim para consultar/atualizar um pedido existente | 101 |
| usuarioId | inteiro | sim ao associar pedido ao aluno | 12 |
| materialId | inteiro | sim ao criar pedido | 7 |
| quantidade | inteiro positivo | sim ao criar pedido | 2 |
| dimensao | texto; proposta em mm | conforme política do material | 300 x 200 mm |
| diametro | decimal positivo; proposta em mm | conforme política do material | 4.5 |
| observacao | texto | não | Uso em maquete |
| status | um de: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO, CANCELADO | sim ao criar/alterar | PENDENTE |
| previsaoRetirada | data e hora | não | 2026-10-15T14:00:00 |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| solicitacaoId | inteiro | sim após criação | 101 |
| usuarioId | inteiro | sim | 12 |
| materialId | inteiro | sim | 7 |
| quantidade | inteiro | sim | 2 |
| dimensao | texto ou null | conforme pedido salvo | 300 x 200 mm |
| diametro | decimal ou null | conforme pedido salvo | 4.5 |
| observacao | texto ou null | conforme pedido salvo | Uso em maquete |
| status | texto | sim | PENDENTE |
| criadaEm | data e hora | sim | 2026-10-13T12:00:00 |
| atualizadaEm | data e hora | sim após alteração | 2026-10-13T12:10:00 |
| previsaoRetirada | data e hora | não | 2026-10-15T14:00:00 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Solicitação não encontrada | Backend trata como `SOLICITACAO_NAO_ENCONTRADA` |
| Dados inválidos | Backend rejeita a operação e informa a validação correspondente |
| Falha ao consultar ou gravar | Falha de persistência tratada pelo Backend |

## Persistência e identidade

Na inclusão, usuarioId é recebido da identidade já validada na API, não de um
campo livre do corpo CT03. O backend define PENDENTE, criadaEm e atualizadaEm e
preserva dimensão, diâmetro e observação; a consulta posterior deve recuperá-los.
Na mudança de status/previsão, preservar os parâmetros originais do pedido.

Este contrato é interno, sem rota ou cabeçalho HTTP. Os filtros por usuário/perfil
devem respeitar as permissões de CT04, CT08 e CT09. Dimensao/diametro ainda faltam
na entidade atual; a descrição não comprova que as colunas já existam. Tipos,
unidades e limites seguem a [proposta comum](../especificacao/materiais-e-pedidos.md)
e o [alinhamento com a Lara](../especificacao/requisitos-banco.md).
