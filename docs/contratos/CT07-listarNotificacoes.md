# CT07: listarNotificacoes

> Entrega parcial #1161: contrato HTTP/serviço testados com consulta simulada;
> consulta de referência validada em tabela temporária PostgreSQL. A rota
> continua desativada por padrão até ligar a leitura ao esquema oficial.

| Linha | Valor |
| :--- | :--- |
| Fronteira | HTTP |
| Quem chama | Frontend |
| Quem responde | Backend API |
| Atende | E7 |
| Como chamar | `GET /api/notificacoes` |
| Autenticação | `Authorization: Bearer <token>`; ver [rotas e erros](../especificacao/autenticacao.md) |

## Entrada

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| somenteNaoLidas | booleano na query (`true` ou `false`) | não; padrão `false` | `?somenteNaoLidas=true` |

## Saída

| Campo | Tipo | Obrigatório | Exemplo |
| :--- | :--- | :--- | :--- |
| notificacoes | lista de objetos Notificacao | sim | lista de avisos do usuário |
| notificacaoId | inteiro | sim, por notificação | 55 |
| tipo | texto | sim, por notificação | PEDIDO_PRONTO |
| mensagem | texto | sim, por notificação | Seu pedido está pronto para retirada |
| criadaEm | data e hora (ISO 8601) | sim, por notificação | 2026-10-13T12:15:00 |
| lida | booleano | sim, por notificação | false |
| solicitacaoId | inteiro ou null | não | 101 |

## Erros

| Quando | HTTP | O que volta |
| :--- | :--- | :--- |
| Usuário sem autenticação | 401 | Erro `NAO_AUTENTICADO` |
| somenteNaoLidas diferente de true ou false, se informado | 400 | Erro `DADOS_INVALIDOS` |
| Falha ao consultar notificações | 500 | Erro `ERRO_INTERNO` |

Formato dos erros: `{ "codigo": "...", "mensagem": "..." }`. Credencial ausente/inválida/expirada em rota protegida recebe HTTP 401 / NAO_AUTENTICADO.

Sucesso: HTTP 200 com objeto `{"notificacoes":[]}` quando vazio. Sem filtro ou
com `false`, a consulta inclui lidas e não lidas. Com `true`, pede somente as
não lidas. A operação apenas consulta; não marca avisos como lidos.

O backend obtém usuarioId da identidade validada e o envia à consulta CT13.
ALUNO e RESPONSAVEL recebem somente seus próprios avisos. Parâmetros da URL
não escolhem outro destinatário. Ordenação e paginação não são definidas aqui.

Neste recorte, `archflow.notificacoes.em-desenvolvimento` fica ligada apenas
nos testes, que fornecem identidade interna e consulta simuladas. Não habilitar
a propriedade em um ambiente compartilhado: ainda falta implementar a consulta
com o esquema de H01.2, provar o isolamento nesse esquema e exercitar avisos
gerados por H09.1. A configuração padrão não registra a rota.

## Prova local da consulta

`ConsultaNotificacoesBancoTest` liga o serviço a uma consulta de referência,
apenas no código de teste. A tabela temporária tem dois destinatários, cada
um com um aviso lido e um não lido. Os cenários conferem o isolamento dos dois
perfis, o filtro, a lista vazia, os campos do aviso e que ler a lista não marca
avisos como lidos.

1. Criar um banco PostgreSQL local descartável com nome no formato
   `archflow_ct07_teste_<sufixo>` (sufixo com minúsculas, números ou sublinhado).
   Ele pode estar vazio: o teste cria só a tabela temporária da sua conexão.
2. No processo do teste, definir `ARCHFLOW_CT07_TESTE_URL` no formato
   `jdbc:postgresql://127.0.0.1:<porta>/<banco>`, junto com
   `ARCHFLOW_CT07_TESTE_USUARIO` e `ARCHFLOW_CT07_TESTE_SENHA` do banco local.
3. Executar `mvn -Dtest=ConsultaNotificacoesBancoTest test` e conferir quatro
   cenários executados, sem ignorados. Sem a URL local no formato dedicado,
   os testes ficam desabilitados.

A prova não inicia a aplicação, não habilita o endpoint e não escreve em
tabelas permanentes. A conexão desfaz a transação e descarta a tabela ao
final de cada cenário. A tabela e a consulta de referência não substituem
o esquema nem a consulta real de H01.2.

## Resultado da validação de 11/11/2026

Branch atualizada com main 3af147b, incluindo o login próprio validado, o novo
DDL de usuários e a persistência de estoque. O conflito em docs/pendencias.md
foi conciliado, mantendo essas entregas e separando consulta/produção de avisos.

Os quatro cenários ConsultaNotificacoesBancoTest usam PostgreSQL 16.15 local
isolado. Conferem os avisos de cada destinatário, o filtro de não lidas sem
mudança de estado, lista vazia para outro usuário e os campos/referência nula.
O usuário e o perfil dessa prova são fixtures internas; ela não passa pelo
filtro JWT nem pelo endpoint. Os sete testes WebMvc separados usam consulta
simulada; o teste de configuração confere que a rota permanece desativada.

Suíte completa: **116 testes aprovados, zero falhas e zero ignorados**.
Inclui os quatro CT07 e os 18 testes PostgreSQL de login/pedido/estoque da
base. Na execução padrão sem essas variáveis, 22 cenários de PostgreSQL ficam
desabilitados; o verde da CI padrão não substitui esta prova local.

Ao terminar, CT07 não deixou tabela permanente nem avisos_ct07_teste em
nenhum esquema temporário do seu banco. Os outros bancos não continham
usuários/materiais/pedidos de fixture restantes. Os quatro bancos descartáveis
foram removidos e a instância isolada encerrada. O Compose declara PostgreSQL
15 e não foi executado nessa prova com PostgreSQL 16.15.

Esta prova é independente de tabela permanente de notificações e de H09.1.
Ela não permite habilitar a propriedade no ambiente compartilhado. Falta
implementar ConsultaDeNotificacoes sobre o esquema oficial, provar o endpoint
com identidade real e esse adaptador, e integrar avisos gerados por H09.1.
