# ADR 0007 — Adotar Keycloak para autenticação

> Decisão histórica, substituída em 10/11/2026 pelo
> [ADR 0008 — Login próprio no backend](0008-login-proprio-sem-keycloak.md).

**Status: aceito quanto à escolha do provedor.** Em 03/11/2026, Luis informou,
após consultar o grupo: “vamos seguir com a autenticação do usuário com o Keycloack”.
Esta decisão substitui a proposta de autenticação própria do ADR 0003.

## Alcance confirmado

A autenticação do ArchFlow será integrada ao Keycloak. A proposta de validar
senhas e emitir tokens próprios no backend deixa de orientar a implementação.
Não criar a tabela `sessoes` nem exigir `senhaHash` no banco de domínio com base
nessa proposta antiga. Campos existentes exigem revisão coordenada antes de remoção.

## Definições técnicas ainda pendentes

- Fluxo de login entre React, Keycloak e API; revisar o CT01 antes de implementar.
  O antigo POST com email/senha é referência histórica, não contrato confirmado.
- Configuração do ambiente: versão, endereços, realm, clientes e redirecionamentos.
- Representação e validação dos perfis ALUNO/RESPONSAVEL; vínculo da identidade
  do provedor com o usuarioId usado pelos pedidos e pelas notificações.
- Transporte e validação da credencial, validade, renovação, armazenamento e logout.
- Tratamento de contas inativas e erros: distinguir respostas do provedor das da API.
- Configuração do Docker e dependências necessárias à integração, em entrega própria.

Esses itens não são decisões adicionais aprovadas pela escolha do provedor.
Esta revisão registra somente a decisão comunicada; não adiciona bibliotecas,
configuração de segurança, serviço Docker ou código de autenticação.

## Impacto nos cards e contratos

- #1136: alinhar CT01/CT10, E1 e requisitos de banco com esta decisão.
- #1138/H02.1 (Luis): adaptar a operação de autenticação à integração acordada.
- H02.2 (Matheus): alinhar validação da identidade e autorização dos perfis.
- #1141/H02.4: comprovar login e acesso reais junto da tela H02.3.
- H01.2 (Lara): separar dados de domínio dos dados de autenticação; definir vínculo.

O transporte Bearer continua como proposta na [matriz de autenticação](../especificacao/autenticacao.md).
Medidas, limites e política de parâmetros por material permanecem pendentes.
Esta decisão não conclui #1136 ou os cards de implementação/integração.
