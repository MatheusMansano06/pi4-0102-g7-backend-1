# Autenticação, identidade e rotas — #1136

O [ADR 0008](../adr/0008-login-proprio-sem-keycloak.md) é a decisão vigente.
A PR #19 implementou o login próprio (#1138) e a validação da identidade (#1139).
Esta matriz descreve o código e identifica as rotas ainda previstas.

## Login e credencial

O frontend envia `email` e `senha` no corpo de
[`POST /api/auth/login`](../contratos/CT1-Autenticar.md), sem token prévio.
O backend consulta a conta por e-mail no [CT10](../contratos/CT10-consultarUsuario.md),
confere a senha com BCrypt e exige conta ativa. Sucesso: HTTP 200 com
`usuarioId`, `nome`, `perfil` (ALUNO ou RESPONSAVEL) e `token`.
O hash fica no banco e não aparece na resposta.

O backend assina o JWT com HS256 e validade de oito horas. A chave de assinatura
fica no ambiente do servidor (`ARCHFLOW_TOKEN_SEGREDO`). Nas chamadas protegidas,
a tela envia o token recebido:

```http
Authorization: Bearer <token>
```

A identidade e o perfil vêm do token validado, não de `X-Perfil`,
`X-Usuario-Id`, parâmetros ou corpo da requisição. O servidor valida a credencial
antes de chamar o controlador; cada operação aplica a sua permissão. O
`OPTIONS` usado pelo navegador para CORS passa sem token. Hoje o login é a
única operação publicada em `/api/auth/`, prefixo dispensado pelo filtro.

| CT | Método e rota | Acesso | Estado |
| --- | --- | --- | --- |
| CT01 | POST /api/auth/login | Sem token prévio | Implementado na PR #19 |
| CT02 | GET /api/materiais | ALUNO ou RESPONSAVEL | Publicado |
| CT03 | POST /api/solicitacoes | ALUNO; pedido associado ao usuarioId do token | Publicado |
| CT04 | GET /api/solicitacoes/fila | RESPONSAVEL | Publicado |
| CT05 | PATCH /api/materiais/{materialId}/estoque | RESPONSAVEL quando habilitada | Parcial; desativada por padrão |
| CT06 | PATCH /api/solicitacoes/{solicitacaoId}/status | RESPONSAVEL | Publicado |
| CT07 | GET /api/notificacoes | Usuário autenticado; somente seus avisos | Previsto |
| CT08 | GET /api/solicitacoes/historico | ALUNO: próprios pedidos; RESPONSAVEL: todos | Publicado |
| CT09 | GET /api/prazos | ALUNO; somente seus pedidos | Previsto |

CT10–CT13 descrevem operações internas do backend com o banco. Não são rotas
HTTP e não recebem cabeçalhos da tela.

## Respostas

| Situação | HTTP | codigo |
| --- | --- | --- |
| Token ausente, inválido ou expirado em rota protegida | 401 | NAO_AUTENTICADO |
| Token válido, mas perfil sem permissão para a operação | 403 | ACESSO_NEGADO |
| E-mail ou senha incorretos no login | 401 | CREDENCIAIS_INVALIDAS |
| Dados obrigatórios de login ausentes/inválidos | 400 | DADOS_INVALIDOS |
| Conta inativa no login | 403 | USUARIO_INATIVO |
| Falha inesperada | 500 | ERRO_INTERNO |

Formato: `{"codigo":"NAO_AUTENTICADO","mensagem":"..."}`. A tela pode exibir a
mensagem de erro; senha, hash e token não devem aparecer em evidências ou logs.

## O que validar no #1141

O backend já emite e confere o token. A
[PR #14 do frontend](https://github.com/anadiasxavier/pi4-0102-g7-frontend/pull/14)
liga as telas ao CT01, guarda a resposta de sessão no navegador e envia o
token no catálogo. Ela ainda está fora da main. A API não publica uma operação
de renovação ou revogação; o tratamento de expiração e a ligação do logout
na interface precisam ser exercitados na integração.

Com a tabela `usuarios` de H01.2/#1134, contas dos dois perfis e a chave de
assinatura configurada, demonstrar login pela tela, catálogo com token, recusa
sem token e fila recusada a um aluno. Conferir também que o histórico do aluno
não inclui pedidos de outro usuário. A validação de tela não substitui as
permissões do servidor. As provas pertencem ao #1141; este recorte é documental.
