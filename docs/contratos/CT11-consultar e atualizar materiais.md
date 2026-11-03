# CT11: consultar e atualizar materiais/estoque

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | Backend ↔ Banco de dados (interno; não é HTTP) |
| Quem chama | Backend API |
| Quem responde | Banco de dados PostgreSQL |
| Atende | E2, E3 e E5 |
| Como chamar | Consultas e atualizações internas sobre materiais e estoque |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materialId | inteiro | sim para consultar/atualizar um material específico | 7 |
| busca | texto | não, para filtrar catálogo | madeira |
| quantidadeDisponivel | inteiro não negativo | sim para atualizar estoque | 24 |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materialId | inteiro | sim, se encontrado | 7 |
| nome | texto | sim, se encontrado | Madeira |
| descricao | texto | não | Chapa para maquete |
| quantidadeDisponivel | inteiro | sim, se encontrado | 24 |
| atualizadaEm | data e hora | sim após atualização | 2026-10-13T12:05:00 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Material não encontrado | Resultado vazio; Backend trata como `MATERIAL_NAO_ENCONTRADO` |
| Quantidade inválida | Backend rejeita a atualização com `QUANTIDADE_INVALIDA` |
| Falha de leitura ou gravação | Falha de persistência tratada pelo Backend |

## Campos necessários para conciliar CT02, CT03 e CT05

| Campo adicional na consulta interna | Situação / regra |
| --- | --- |
| especificacao, quantidadeMinima, ativo | Já existem na entidade Material |
| parametrosAceitos, parametrosObrigatorios | Metadados propostos, política e armazenamento a validar |
| atualizadaEm | Persistir junto da atualização do saldo, não gerar só na resposta |

statusEstoque é derivado do saldo e do mínimo, sem exigir coluna própria.
Os parâmetros aceitos/obrigatórios são consultados para validar a criação do pedido;
não serão inferidos da descrição. Veja [materiais e pedidos](../especificacao/materiais-e-pedidos.md).
Este contrato é interno: a identidade/permissão já foi conferida na camada de API;
não é um endpoint que recebe Authorization diretamente. Falha de gravação não
pode ser apresentada como sucesso em CT05. Tipos físicos e regras de migração
seguem para [alinhamento com o banco](../especificacao/requisitos-banco.md).
