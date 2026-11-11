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

- O e-mail é conferido **sem diferenciar maiúscula de minúscula**, porque o
  banco tem índice único por `LOWER(email)`. O e-mail é gravado como a pessoa
  digitou; a conferência é que ignora a caixa.
- O RA também é único no banco. A recusa acontece antes do `INSERT`, para a
  tela receber a mensagem em vez da violação do índice.
- A senha é gravada **só como hash BCrypt**. A senha digitada nunca vai para o
  banco nem para o token.
- O limite de 72 caracteres existe porque o BCrypt só lê os 72 primeiros bytes:
  sem o limite, duas senhas diferentes passariam a valer a mesma coisa.
- A conta nasce com `ativo = true`, com `matricula_funcional` e `cargo` nulos,
  satisfazendo o `CHECK ck_usuarios_dados_perfil` da tabela `usuarios`.

## O que esta ficha resolve do banco

Os usuários de `banco/02-dados-exemplo.sql` têm `senha_hash = 'DEMO_SEM_LOGIN'`,
que não é um hash BCrypt: **nenhum deles consegue entrar**. Com o CT14, dá para
criar pela tela uma conta de aluno com hash válido e entrar com ela, sem esperar
a correção da carga de exemplos. Isso **não substitui** essa correção, que
continua pendente em H01.2 — ver [requisitos-banco](../especificacao/requisitos-banco.md).

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
