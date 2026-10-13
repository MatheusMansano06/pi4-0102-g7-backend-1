# CT11: consultar e atualizar materiais/estoque

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

