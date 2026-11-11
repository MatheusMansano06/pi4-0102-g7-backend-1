# CT10: consultarUsuario

> Vale a partir de 10/11/2026, com o [ADR 0008](../adr/0008-login-proprio-sem-keycloak.md):
> o login é próprio, então a consulta por e-mail e o `senhaHash` voltam a ser o que o código usa.
>
> A tabela `usuarios` precisa das colunas `usuario_id`, `nome`, `email`, `senha_hash`,
> `perfil` (ALUNO ou RESPONSAVEL) e `ativo`. O `senha_hash` guarda um hash BCrypt;
> senha em texto puro, nunca.

Em 11/11, a PR #24 passou a criar `usuarios` em 01-tabelas.sql. Além dos
campos lidos pela entidade, o banco exige RA e curso para ALUNO; para
RESPONSAVEL exige matrícula funcional e cargo, com campos do outro perfil nulos.
Esses campos extras não são uma nova saída do CT01/CT10 nem estão mapeados na
entidade Usuario. Fixtures e provisionamento precisam respeitar as restrições.
Os hashes DEMO_SEM_LOGIN dos exemplos não são BCrypt válidos para autenticar.

O índice de unicidade usa LOWER(email); a consulta atual findByEmail compara
o e-mail informado após trim. Não prometer consulta sem distinção de caixa
somente pela existência do índice.

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
