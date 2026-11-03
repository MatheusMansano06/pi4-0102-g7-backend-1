# CT10: consultarUsuario

> Revisão necessária após a escolha do [Keycloak](../adr/0007-adotar-keycloak.md). A ficha abaixo preserva o modelo anterior de login local; senhaHash e consulta por e-mail não são requisitos aprovados da integração. Definir vínculo entre identidade autenticada e usuarioId de domínio; ausência desse vínculo não significa automaticamente senha inválida.

| Linha | Valor |
| :--- | :--- |
| Fronteira | Backend ↔ Banco de dados (interno; não é HTTP) |
| Quem chama | Backend API |
| Quem responde | Banco de dados PostgreSQL |
| Atende | E1 |
| Como chamar | Consulta interna por e-mail, por exemplo `SELECT ... FROM usuarios WHERE email = ?` |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| email | texto | sim | aluno@exemplo.com |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| usuarioId | inteiro | sim, se encontrado | 12 |
| nome | texto | sim, se encontrado | Ana Silva |
| email | texto | sim, se encontrado | aluno@exemplo.com |
| senhaHash | texto | sim, para validação interna | hash armazenado |
| perfil | ALUNO ou RESPONSAVEL | sim, se encontrado | ALUNO |
| ativo | booleano | sim, se encontrado | true |

## Erros

| Quando | O que volta |
| :--- | :--- |
| Nenhum usuário corresponde ao e-mail | Resultado vazio; Backend trata como credencial inválida |
| Falha na consulta | Falha de persistência tratada pelo Backend |
