# CT14: cadastrarAluno

> Implementado em 11/11/2026. A tela de cadastro de aluno existia no frontend
> (`StudentRegister.jsx`) sem nenhuma operação do backend para chamar: o
> `handleSubmit` só impedia o recarregamento da página. Esta ficha define a
> operação que faltava. Não havia card no OpenProject para ela: o **#1350** foi
> aberto depois da implementação, para o trabalho ter onde ser apontado.

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E1 |
| Como chamar | `POST /api/auth/cadastro` |
| Autenticação | Rota pública, sem token prévio. Quem ainda não tem conta não tem token |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| nome | texto | sim | Ana Silva |
| ra | texto, até 30 | sim | 2026001 |
| curso | texto, até 120 | sim | Sistemas de Informação |
| email | texto | sim | ana@exemplo.com |
| senha | texto, de 8 a 72 | sim | senha escolhida pelo usuário |

O corpo **não tem campo `perfil`**. Esta rota cria somente conta de `ALUNO`.
Conta de `RESPONSAVEL` nasce pelo script do banco; se ela pudesse ser pedida
pela tela pública, qualquer pessoa viraria responsável do laboratório.

## Saída

Mesmos campos do [CT01](CT1-Autenticar.md), com HTTP **201**. O token já vem
emitido, para a pessoa não ter que entrar de novo logo depois de se cadastrar.

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| usuarioId | inteiro | sim | 12 |
| nome | texto | sim | Ana Silva |
| curso | texto | sim, curso do aluno cadastrado | Arquitetura |
| perfil | sempre ALUNO | sim | ALUNO |
| token | texto | sim | JWT assinado pelo backend, válido por 8 horas |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Campo obrigatório ausente ou em branco | 400 | Erro `DADOS_INVALIDOS` |
| Senha com menos de 8 ou mais de 72 caracteres | 400 | Erro `SENHA_FRACA` |
| Já existe conta com esse e-mail | 400 | Erro `EMAIL_JA_CADASTRADO` |
| Já existe conta com esse RA | 400 | Erro `RA_JA_CADASTRADO` |
| Falha inesperada | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`, como no ADR 0004.

As duas recusas de repetido vêm como 400, e não 409, porque o
[ADR 0004](../adr/0004-padrao-de-erros-da-api.md) mapeia `NegocioException` para 400 e
esta ficha não altera esse mapeamento. O código distingue o caso para a tela.

## Regras

- O cadastro verifica duplicidade com `findByEmailIgnoreCase` e o banco tem
  índice único por `LOWER(email)`. A entidade remove espaços nas extremidades;
  a caixa informada é preservada. O login CT01 ainda consulta `findByEmail`
  após trim; não prometer login insensível à caixa a partir da regra do cadastro.
- O RA também é único no banco. A recusa acontece antes do `INSERT`, para a
  tela receber a mensagem em vez da violação do índice.
- A senha é gravada **só como hash BCrypt**. A senha digitada nunca vai para o
  banco nem para o token.
- O serviço verifica 8 a 72 por `String.length()` (unidades UTF-16). Essa
  verificação não equivale a limitar 72 bytes para senhas Unicode; a ficha não
  declara resolvida essa diferença nem modifica a autenticação nesta revisão.
- A conta nasce com `ativo = true`, com `matricula_funcional` e `cargo` nulos,
  satisfazendo o `CHECK ck_usuarios_dados_perfil` da tabela `usuarios`.

## O que esta ficha resolve do banco

A PR backend28 já corrigiu a carga de exemplos para usar BCrypt com pgcrypto;
o aviso histórico de DEMO_SEM_LOGIN não descreve o arquivo atual. O cadastro
CT14 também já é chamado por StudentRegister na main do frontend. A carga
continua criando apenas alunos, sem responsável de demonstração. Publicar SQL
ou integrar cadastro não comprova aplicação no destino; ver
[requisitos-banco](../especificacao/requisitos-banco.md).

RA/curso têm capacidades físicas 30/120 no SQL. O serviço verifica presença,
mas não antecipa todos os limites de coluna; excesso não tem recusa de negócio
específica documentada. A senha/hash nunca entram nas evidências.

## Prova

`CadastroAlunoBancoTest`, sete testes em PostgreSQL 17 isolado com o
`banco/01-tabelas.sql` atual, executados em 11/11/2026:

- a conta é gravada e o token devolvido já abre `/api/materiais`;
- o hash gravado é BCrypt de verdade e o login seguinte funciona com ele;
- e-mail repetido é recusado mesmo escrito com outra caixa;
- RA repetido é recusado;
- campo ausente ou em branco é recusado e nada é gravado;
- senha curta é recusada e nada é gravado;
- `perfil: "RESPONSAVEL"` enviado no corpo é ignorado e a conta sai `ALUNO`.
