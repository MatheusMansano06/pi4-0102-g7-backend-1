# ADR 0003: proposta de token opaco guardado no banco

**Status:** proposta não adotada, substituída pela [decisão de usar Keycloak](0007-adotar-keycloak.md), confirmada pelo usuário após consultar o grupo em 03/11/2026.

O conteúdo abaixo preserva a proposta anterior para rastreabilidade; não orienta a implementação atual.

**Contexto:** CT01 prevê uma credencial de acesso e perfis. É necessário definir
emissão, validação, validade e encerramento. Token é uma credencial de transporte;
pode ter estado no servidor. Um token opaco persistido não é uma solução sem estado.

**Proposta original, ainda não aprovada:** no login local, conferir o hash da senha
e a conta ativa; emitir token opaco, registrar usuário e validade e devolver a
credencial ao frontend. UUID era o formato sugerido, não uma exigência já aceita.
Essa opção precisa da tabela sessoes, além de usuarios. A conferência de token
e perfil pertence ao H02.2; a operação de login do Luis é H02.1/#1138.

**Alternativa a avaliar:** o detalhamento recebido relata Keycloak como recomendação
permitida pelo professor para login, com Spring Security/OAuth2 nessa parte. Isso
não escolhe a alternativa nem autoriza adicionar dependências por esta revisão.
Se adotada, revisar o fluxo de CT01/CT10 e a divisão de H02.1/H02.2 antes de codificar.

**Transporte proposto, comum à revisão:** `Authorization: Bearer <token>` nas rotas
CT02–CT09. Ver [matriz de autenticação](../especificacao/autenticacao.md). Cabeçalho
de transporte e mecanismo de emissão são decisões distintas. Cookie, armazenamento,
renovação e logout não estão aprovados por este ADR.

**Consequências condicionais:** a opção opaca faz consultas à sessão e precisa de
política de expiração/invalidação; a opção de provedor exige sua configuração e
integração. Não prometer ausência de riscos só por escolher token. Não implementar
ambas simultaneamente nem tratar o X-Perfil atual como autenticação real.
