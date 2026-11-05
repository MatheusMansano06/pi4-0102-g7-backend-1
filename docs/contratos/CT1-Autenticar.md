# CT01: autenticar

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E1 |
| Como chamar | `POST /api/auth/login` |
| Autenticação | Login público, sem token prévio; Keycloak escolhido; fluxo e rota anteriores a revisar |

> **Decisão posterior, 03/11/2026:** Keycloak escolhido pelo grupo ([ADR 0007](../adr/0007-adotar-keycloak.md)). A rota, as entradas, as saídas e os erros abaixo preservam a ficha anterior; precisam ser revisados conforme o fluxo de integração. Não implementar recebimento de senha ou emissão própria de token a partir desta ficha.

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
| token | texto | sim | credencial emitida pela abordagem a validar |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| E-mail ou senha incorretos | 401 | Erro `CREDENCIAIS_INVALIDAS` |
| Campo obrigatório ausente ou inválido | 400 | Erro `DADOS_INVALIDOS` |
| Conta inativa | 403 | Erro `USUARIO_INATIVO` |
| Falha inesperada | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida segue a proposta comum de HTTP 401 / NAO_AUTENTICADO.

## Integração e limites da proposta

Keycloak é o provedor confirmado. A proposta de CT10 fornecer senhaHash para
validar senha no backend foi superada. Definir o vínculo da identidade autenticada
com o usuarioId de domínio, os perfis e os dados retornados à interface.

O transporte Bearer, a validade, a renovação, o logout e o armazenamento continuam
em revisão em [autenticação](../especificacao/autenticacao.md). A escolha do
provedor não confirma o POST /api/auth/login nem o formato antigo de sucesso.
