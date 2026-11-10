# Guia do banco do backend ArchFlow

Atualização #1136 de 04/11/2026 sobre main 6181761, após PRs #12–#15.
**Luis definiu dimensão e diâmetro permitidos e opcionais para todos os materiais
do MVP.** A especificação é sua entrega H01.4; não é pendência de a Lara decidir
os requisitos. Lara/H01.2 revisa/implementa a estrutura e coordena a aplicação.

A referência é o [dicionário das seis colunas](especificacao/colunas-materiais-e-solicitacoes.md),
com tipo, nulabilidade, default, unidade e correspondência CT03/CT11/CT12.
A revisão está na PR #10; PR #14 já foi integrada parcialmente e permanece como
histórico da implementação. Não é necessária uma nova definição de colunas do zero.

## Estrutura e scripts

Java 17, Spring Boot 3.2.5, JPA e PostgreSQL 15; ddl-auto: validate confere
compatibilidade e não cria/migra esquema. Regras Java não viram automaticamente
CHECK, NOT NULL, DEFAULT ou índices no SQL.

| Arquivo | Uso |
| --- | --- |
| [01-tabelas.sql](../banco/01-tabelas.sql) | Criação nova de materiais/solicitacoes, seis campos e defaults do MVP |
| [02-dados-exemplo.sql](../banco/02-dados-exemplo.sql) | Materiais/pedidos de demonstração; quatro regras explícitas nos materiais |
| [H04.1-colunas-propostas.sql](sql/H04.1-colunas-propostas.sql) | Banco existente; adiciona campos, define defaults e configura as regras; fora da inicialização automática |

O script de atualização agora contém UPDATE dos quatro indicadores, porque o
Luis confirmou a regra uniforme. Não altera saldo, ativo, identificação, pedidos
ou datas. Pode repetir, sem DROP/recriar volume. IF NOT EXISTS não corrige tipos
incompatíveis já existentes. Combinar destino/cópia de segurança/aplicação antes
de iniciar a API atualizada. Esta entrega não executa o SQL no banco compartilhado.

## Tabelas que o backend usa

Fontes: [Material](../src/main/java/com/archflow/material/Material.java) e
[Solicitacao](../src/main/java/com/archflow/solicitacao/Solicitacao.java).

| materiais | Tipo / regra |
| --- | --- |
| material_id | BIGINT IDENTITY, PK |
| nome, descricao, especificacao | VARCHAR(255); descrição fixa, não política por interpretação do nome |
| quantidade_disponivel, quantidade_minima | INTEGER NOT NULL; Java recusa negativo |
| ativo | BOOLEAN NOT NULL; inativo não aparece no catálogo nem aceita pedido |
| atualizada_em | TIMESTAMP WITHOUT TIME ZONE, anulável, sem default; preenchido pelo CT05 ao alterar saldo (#1154) |
| aceita_dimensao, aceita_diametro | BOOLEAN anulável DEFAULT TRUE |
| exige_dimensao, exige_diametro | BOOLEAN anulável DEFAULT FALSE |

statusEstoque é calculado: zero INDISPONIVEL; positivo até o mínimo BAIXO;
acima do mínimo DISPONIVEL. Não precisa de coluna. parametrosAceitos e
parametrosObrigatorios serão projeções dos booleanos, não novas colunas.
NULL nas regras ainda identifica configuração incompleta, recusada no POST.
O [script de estoque](sql/H07.1-data-do-estoque.sql) adiciona a coluna ao
banco existente. A aplicação deve ser coordenada com H01.2 antes da subida
do backend atualizado; desativar o PATCH não elimina a validação da coluna.

| solicitacoes | Tipo / regra |
| --- | --- |
| solicitacao_id | BIGINT IDENTITY, PK |
| usuario_id | BIGINT; aluno resolvido pela identidade validada |
| material_id | BIGINT, FK materiais.material_id |
| quantidade | INTEGER NOT NULL, positiva no Java |
| status | VARCHAR(255), nomes do enum |
| criada_em, atualizada_em, previsao_retirada | TIMESTAMP; previsão opcional; criação/atualização do servidor |
| observacao | VARCHAR(255), anulável; já existente antes da PR #14 |
| dimensao | VARCHAR(255), anulável, texto em mm do pedido |
| diametro | DOUBLE PRECISION, anulável, número positivo/finito em mm |

Não criar largura/altura adicionais neste recorte. Não alterar especificacao do
material ao gravar medida do pedido. Double não significa decimal exato nem fixa
casas decimais. A entrada da API antecipa o limite de dimensão/observação com
String.length(): acima de 255 recebe 400 DADOS_INVALIDOS antes de gravar.
O texto permitido não é cortado/aparado; a métrica Java está no dicionário.

## Pedidos estados e consultas

POST /api/solicitacoes retorna 201 após gravação, com PENDENTE, aluno e horários
do servidor. Medidas/observação são preservadas em atualizações. Sem token
válido do CT01, a chamada recebe 401; X-Perfil não autentica.

Estados: PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA,
CONCLUIDO e CANCELADO. [CT06](contratos/CT06-atualizarStatus.md) registra as
transições do código. Status é texto, nunca posição de enum. A fila sem filtro
exclui só CONCLUIDO atualmente. Histórico da PR #13 já existe e usa a identidade
do JWT próprio da PR #19. Não remover pedidos históricos.

PATCH de status preserva previsão se ausente/null e recusa previsão passada.
Não criar baixa automática ou notificações por trigger: não são efeitos definidos
por esta revisão. usuarioId vem do token emitido após consultar `usuarios`.
A solicitação ainda guarda o número sem relação JPA; conferir IDs existentes
antes de decidir uma chave estrangeira ou migrar dados.

## O que resta por módulo

| Item | Quem conduz / situação |
| --- | --- |
| Aplicar atualização no banco existente | Lara/H01.2 coordena o destino com o SQL/especificação entregues pelo Luis |
| Tabela usuarios | H01.2/#1134: criar a tabela exigida pelo CT10 e prover contas de teste dos dois perfis |
| Login integrado | PR #19 fornece o token com usuarioId/perfil; #1141/Luis deve provar pela tela |
| Data persistida do estoque | Luis/#1154 preparou gravação e SQL; H01.2 coordena migração, PATCH permanece desativado |
| Metadados no catálogo | Projetar booleanos conforme CT02; DTO ainda não os expõe |
| Formulário #1147 | Modal ainda sem envio; PR #14 do frontend adiciona Bearer ao catálogo, fora da main |
| dataLimite | Fora de CT03/CT12; não confundir com previsaoRetirada ou relógio de criação |
| Notificações | H09.1/Lara produz; #1161/Luis consulta; esquema próprio ainda a detalhar |

O ADR 0008 exige `senha_hash` BCrypt em `usuarios`, sem tabela de sessões.
Não preencher dados de usuário, horários históricos ou regras por material
por suposição. A configuração uniforme de medidas decorre da confirmação do Luis.

## Verificação

A PR #14 teve 67 testes padrão aprovados e quatro PostgreSQL aprovados em cada
um de dois cenários (novo/migrado). A evidência dessa revisão de defaults e
configuração fica na PR #10; testes de fixture não provam autenticação ou pedido
pela tela. Não declarar cards concluídos ou atualizar OpenProject por este guia.
