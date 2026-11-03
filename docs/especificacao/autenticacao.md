# Autenticação, identidade e rotas — proposta #1136

**Proposta em revisão**, vinculada ao [alinhamento H01.4](H01.4-alinhamento.md).
Transportar um token não decide como ele será emitido ou validado.

## Transporte proposto

O login CT01 não exige token prévio. Para CT02–CT09, o frontend enviará:

```http
Authorization: Bearer <token>
```

O token não vai na URL nem substitui parâmetros de domínio. Emissões, validade,
renovação, armazenamento no navegador e logout dependem da abordagem escolhida.
Não prescrever UUID, JWT ou tabela sessoes como decisões já aceitas.

| CT | Método e rota | Acesso alvo |
| --- | --- | --- |
| CT01 | POST /api/auth/login | Público; recebe credenciais |
| CT02 | GET /api/materiais | ALUNO ou RESPONSAVEL autenticado |
| CT03 | POST /api/solicitacoes | ALUNO autenticado, dono do novo pedido |
| CT04 | GET /api/solicitacoes/fila | RESPONSAVEL |
| CT05 | PATCH /api/materiais/{materialId}/estoque | RESPONSAVEL |
| CT06 | PATCH /api/solicitacoes/{solicitacaoId}/status | RESPONSAVEL |
| CT07 | GET /api/notificacoes | Usuário autenticado; somente seus avisos |
| CT08 | GET /api/solicitacoes/historico | Histórico permitido à identidade/perfil |
| CT09 | GET /api/prazos | ALUNO autenticado; somente seus pedidos |

O alcance global do histórico do responsável deve ser confirmado no CT08.
CT10–CT13 são interfaces internas com o banco: não são rotas HTTP e não recebem
um cabeçalho do navegador. O backend transmite a identidade já validada às
operações internas quando necessário.

## Respostas da proposta

| Situação | HTTP | codigo |
| --- | --- | --- |
| Credencial ausente, vazia, malformada, desconhecida ou expirada em rota protegida | 401 | NAO_AUTENTICADO |
| Identidade válida, mas operação/perfil/registro não permitido | 403 | ACESSO_NEGADO |
| E-mail ou senha incorretos no CT01 | 401 | CREDENCIAIS_INVALIDAS |
| Dados obrigatórios de login ausentes/inválidos | 400 | DADOS_INVALIDOS |
| Conta inativa no CT01 | 403 | USUARIO_INATIVO |
| Falha inesperada | 500 | ERRO_INTERNO |

Corpo de erro: `{"codigo":"NAO_AUTENTICADO","mensagem":"..."}`. Mensagem legível,
sem senha, hash ou token. Os números seguem o padrão já usado no tratamento de
erros do backend; as rotas que ainda não existem precisam implementá-lo.

O usuarioId do pedido ou do filtro de avisos vem da identidade validada, não de
um valor livre enviado pela tela. X-Perfil não autentica e deve sair quando H02.2
for integrado. O redirecionamento da tela para /admin não substitui autorização
da API.

## Decisão de implementação pendente

O ADR 0003 propõe login local, hash e token opaco persistido. O detalhamento
recebido relata Keycloak como alternativa permitida para login. Antes de #1138 e
H02.2, registrar uma escolha única com a equipe e ajustar o CT01 caso o provedor
altere o fluxo. Esta documentação não instala nem autoriza dependências novas.

Critérios de integração: login real de aluno e responsável; credencial enviada
nas rotas protegidas; ausência/invalidade recusadas; acesso ao registro de outro
usuário recusado. Mock, cabeçalho manual ou menu fixo não fecham #1141.
