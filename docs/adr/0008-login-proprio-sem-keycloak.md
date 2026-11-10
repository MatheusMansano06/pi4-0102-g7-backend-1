# ADR 0008 — Login próprio no backend, sem Keycloak

**Status: aceito.** Em 10/11/2026 o grupo decidiu tirar o Keycloak do projeto.
Esta decisão substitui o [ADR 0007](0007-adotar-keycloak.md) e retoma, na prática,
a autenticação própria que o [ADR 0003](0003-autenticar-com-token-opaco.md) descreve,
com uma diferença: o token é assinado, e não opaco.

## Por que

As telas de login do aluno e do responsável foram refeitas no frontend como
formulário próprio de e-mail e senha, e nenhum arquivo do front usa mais o
Keycloak. Manter o provedor exigiria refazer as telas em cima dele, no meio da
sprint, para entregar a mesma coisa que o backend já sabe fazer.

## O que fica valendo

- `POST /api/auth/login` recebe e-mail e senha e devolve usuarioId, nome, perfil
  e token, conforme o [CT01](../contratos/CT1-Autenticar.md).
- A senha é guardada como hash BCrypt na tabela `usuarios`, conforme o
  [CT10](../contratos/CT10-consultarUsuario.md). Senha em texto puro, nunca.
- O token é um JWT assinado pelo próprio backend (HS256) e vale 8 horas. Dentro
  dele vão o usuarioId e o perfil, que é o que o filtro de identidade lê em cada
  chamada. Senha nenhuma entra no token.
- A senha secreta da assinatura vem da variável `ARCHFLOW_TOKEN_SEGREDO`, com no
  mínimo 32 caracteres. Trocar essa senha derruba os tokens já emitidos.
- `/api/auth/login` é a única rota da API que não exige token: é a porta de entrada.

## O que sai

- O serviço `keycloak` do `docker-compose.yml`, as variáveis `PORTA_KEYCLOAK`,
  `KEYCLOAK_ADMIN_USUARIO`, `KEYCLOAK_ADMIN_SENHA` e `KEYCLOAK_CERTIFICADOS`,
  e a pasta `keycloak/` com o realm exportado.
- A leitura de papéis do realm no `LeitorDeToken`.

## Impacto nos cards

- #1138/H02.1: a operação autenticar passa a ser o login próprio descrito aqui.
- #1139/H02.2: a conferência de identidade passa a abrir o token do backend.
- #1134/H01.2 (Lara): o script do banco precisa da tabela `usuarios` com
  `usuario_id`, `nome`, `email`, `senha_hash`, `perfil` e `ativo`. Sem ela o
  backend não sobe, porque o Hibernate confere as tabelas na partida (ADR 0005).
- #1140/H02.3 (Mirele) e as telas novas: a tela de login precisa chamar
  `POST /api/auth/login` e guardar o token para mandar nas chamadas seguintes.
- #1141/H02.4: a integração do login continua sendo a prova de ponta a ponta.
