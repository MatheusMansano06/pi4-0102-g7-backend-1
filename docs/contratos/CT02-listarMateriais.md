# CT02: listarMateriais

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E2 e E5 |
| Como chamar | `GET /api/materiais?busca={termo}` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| busca | texto | não | madeira |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| materiais | lista de objetos Material | sim | lista de materiais ativos |
| materialId | inteiro | sim, por material | 7 |
| nome | texto | sim, por material | Madeira |
| descricao | texto | não | Chapa para maquete |
| quantidadeDisponivel | inteiro | sim, por material | 24 |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Usuário sem autenticação | Erro `NAO_AUTENTICADO` |
| Falha ao consultar o banco | Erro `ERRO_INTERNO` |
