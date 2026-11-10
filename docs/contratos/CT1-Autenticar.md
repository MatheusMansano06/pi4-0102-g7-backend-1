# CT01: autenticar

> Implementado em 10/11/2026, conforme o [ADR 0008](../adr/0008-login-proprio-sem-keycloak.md):
> login próprio no backend, sem Keycloak. A ficha abaixo é o que o código faz.

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E1 |
| Como chamar | `POST /api/auth/login` |
| Autenticação | Login público, sem token prévio. É a única rota da API que não exige token |

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
| token | texto | sim | JWT assinado pelo backend, válido por 8 horas |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| E-mail ou senha incorretos | 401 | Erro `CREDENCIAIS_INVALIDAS` |
| Campo obrigatório ausente ou inválido | 400 | Erro `DADOS_INVALIDOS` |
| Conta inativa | 403 | Erro `USUARIO_INATIVO` |
| Falha inesperada | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida segue a proposta comum de HTTP 401 / NAO_AUTENTICADO.

## Como o token funciona

O token é assinado pelo próprio backend com a senha secreta de
`ARCHFLOW_TOKEN_SEGREDO` (HS256) e vale 8 horas. Dentro dele vão o `usuarioId` e
o `perfil`; senha nenhuma entra no token. A tela guarda o token e manda em todas
as outras chamadas no cabeçalho `Authorization: Bearer <token>`, como descreve a
[matriz de autenticação](../especificacao/autenticacao.md).

A senha é conferida contra o hash BCrypt gravado na tabela `usuarios`
([CT10](CT10-consultarUsuario.md)). E-mail inexistente e senha errada devolvem a
mesma resposta de propósito, para não entregar quais e-mails têm conta.
