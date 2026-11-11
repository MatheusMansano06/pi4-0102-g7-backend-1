# Guia do banco do backend ArchFlow

Atualização #1136 de 11/11/2026 sobre main bd733aa, após PRs #23/#24.
**Luis definiu dimensão e diâmetro permitidos e opcionais para todos os materiais
do MVP.** A especificação é sua entrega H01.4; não é pendência de a Lara decidir
os requisitos. Lara/H01.2 revisa/implementa a estrutura e coordena a aplicação.

A referência é o [dicionário das seis colunas](especificacao/colunas-materiais-e-solicitacoes.md),
com tipo, nulabilidade, default, unidade e correspondência CT03/CT11/CT12.
A revisão foi integrada na PR #10; PR #14 já foi integrada parcialmente e permanece como
histórico da implementação. Não é necessária uma nova definição de colunas do zero.

## Estrutura e scripts

Java 17, Spring Boot 3.2.5, JPA e PostgreSQL 15; ddl-auto: validate confere
compatibilidade e não cria/migra esquema. Regras Java não viram automaticamente
CHECK, NOT NULL, DEFAULT ou índices no SQL.

| Arquivo | Uso |
| --- | --- |
| [01-tabelas.sql](../banco/01-tabelas.sql) | Criação nova de usuarios/materiais/solicitacoes, seis campos, data do estoque e restrições da PR #24 |
| [02-dados-exemplo.sql](../banco/02-dados-exemplo.sql) | Carga ainda incompatível com o DDL/Java; corrigir antes da primeira instalação completa |
| [H04.1-colunas-propostas.sql](sql/H04.1-colunas-propostas.sql) | Banco existente; adiciona campos, define defaults e configura as regras; fora da inicialização automática |
| [H07.1-data-do-estoque.sql](sql/H07.1-data-do-estoque.sql) | Banco existente; adiciona data do estoque, sem default/backfill ou alteração de saldo |

O script de atualização agora contém UPDATE dos quatro indicadores, porque o
Luis confirmou a regra uniforme. Não altera saldo, ativo, identificação, pedidos
ou datas. Pode repetir, sem DROP/recriar volume. IF NOT EXISTS não corrige tipos
incompatíveis já existentes. Combinar destino/cópia de segurança/aplicação antes
de iniciar a API atualizada. Esta entrega não executa o SQL no banco compartilhado.

## Tabelas que o backend usa

usuarios já existe no DDL da PR #24. Campos comuns: usuario_id IDENTITY PK,
nome, email, senha_hash, perfil e ativo. ALUNO exige RA/curso e não admite
matrícula funcional/cargo; RESPONSAVEL exige matrícula funcional/cargo e não
admite RA/curso. Hash para autenticar deve ser BCrypt, não DEMO_SEM_LOGIN.
Notificações continuam sem esquema publicado.

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
| usuario_id | BIGINT NOT NULL, FK usuarios.usuario_id; aluno resolvido pelo token |
| material_id | BIGINT NOT NULL, FK materiais.material_id |
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
A solicitação guarda o número sem relação JPA com Usuario, mas o SQL já exige
a FK. Conferir referências existentes antes da migração; mapeamento JPA simples
não elimina a integridade referencial do banco.

## O que resta por módulo

| Item | Quem conduz / situação |
| --- | --- |
| Aplicar atualização no banco existente | Lara/H01.2 coordena o destino com o SQL/especificação entregues pelo Luis |
| Tabela usuarios | DDL publicado na #24; conciliar exemplos, contas com BCrypt dos dois perfis e migração no destino |
| Login integrado | PR #19 fornece o token com usuarioId/perfil; #1141/Luis deve provar pela tela |
| Data persistida do estoque | PR #23 integrada e provada isoladamente; H01.2 coordena migração, PATCH permanece desativado |
| Metadados no catálogo | Projetar booleanos conforme CT02; DTO ainda não os expõe |
| Formulário #1147 | Modal ainda sem envio; PR #14 do frontend adiciona Bearer ao catálogo, fora da main |
| dataLimite | Fora de CT03/CT12; não confundir com previsaoRetirada ou relógio de criação |
| Notificações | H09.1/Lara produz; #1161/Luis consulta; esquema próprio ainda a detalhar |

O ADR 0008 exige `senha_hash` BCrypt em `usuarios`, sem tabela de sessões.
Não preencher dados de usuário, horários históricos ou regras por material
por suposição. A configuração uniforme de medidas decorre da confirmação do Luis.

## Compatibilidade dos exemplos atuais

Ver a [comparação exata entre carga, DDL e Java](especificacao/requisitos-banco.md).
Materiais usam categoria/unidade_medida/quantidade_estoque inexistentes no DDL;
pedidos usam datas e status diferentes dos contratos. A carga também diverge
da política de medidas e não fornece logins funcionais dos dois perfis.
A execução isolada falhou em categoria. Este guia registra o ajuste necessário
em H01.2 e não modifica/aplica SQL da equipe.

## Verificação atual e histórico

A [PR #25](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/25)
atualiza o teste CT03 para usuários/IDs do esquema novo e JWT real: 95 testes
aprovados, incluindo sete CT03 e dois CT05 em PostgreSQL 16.15 isolado. O
esquema veio do 01 atual; os exemplos incompatíveis foram conferidos à parte.
Não é prova de migração compartilhada, login por senha ou pedido pela tela.

A PR #14, historicamente, teve 67 testes padrão aprovados e quatro PostgreSQL aprovados em cada
um de dois cenários (novo/migrado). A evidência dessa revisão de defaults e
configuração fica na PR #10; testes de fixture não provam autenticação ou pedido
pela tela. Não declarar cards concluídos ou atualizar OpenProject por este guia.
