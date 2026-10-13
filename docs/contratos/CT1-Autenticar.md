# CT01: autenticar

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E1 |
| Como chamar | `POST /api/auth/login` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| email | texto | sim | aluno@exemplo.com |
| senha | texto | sim | senha informada pelo usuário |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| usuarioId | inteiro | sim | 12 |
| nome | texto | sim | Ana Silva |
| perfil | um de: ALUNO, RESPONSAVEL | sim | ALUNO |
| token | texto | sim | token de sessão |

## Erros

| Quando | O que volta |
| :--- | :--- |
| E-mail ou senha incorretos | Erro `CREDENCIAIS_INVALIDAS` |
| Campo obrigatório ausente ou inválido | Erro `DADOS_INVALIDOS` |
| Conta inativa | Erro `USUARIO_INATIVO` |
| Falha inesperada | Erro `ERRO_INTERNO` |
