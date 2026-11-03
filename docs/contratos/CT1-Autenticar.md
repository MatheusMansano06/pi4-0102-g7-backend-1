# CT01: autenticar

> Revisão #1136: contrato alvo em revisão; propostas e diferenças para o código estão em [H01.4](../especificacao/H01.4-alinhamento.md).

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E1 |
| Como chamar | `POST /api/auth/login` |
| Autenticação | Login público, sem token prévio; abordagem de emissão ainda pendente |

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

Na hipótese de login local, CT10 busca por e-mail e fornece senhaHash para validar
a senha, além do estado ativo. A resposta nunca inclui senha ou hash. Keycloak é
alternativa ainda não escolhida; se adotado, revisar o fluxo de emissão e este
contrato antes de implementar #1138/H02.2. A revisão não fixa UUID nem cria sessoes.

Sucesso proposto: HTTP 200 com usuarioId, nome, perfil e token. Nas chamadas CT02–CT09,
o frontend envia `Authorization: Bearer <token>`. Regras de validade, renovação,
logout e armazenamento da credencial continuam pendentes no alinhamento H01.4.
