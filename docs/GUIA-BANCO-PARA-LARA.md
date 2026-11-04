# Guia do banco — consolidação do backend ArchFlow

**Para:** Lara, responsável pelo H01.2, e equipe de backend.
**Conferência:** 03/11/2026, após o merge autorizado da PR #8.
**Objetivo:** orientar os scripts do banco com base no código de todos os integrantes,
identificando o que já existe, o que precisa ser acrescentado e o que depende de decisão.
Este documento não executa SQL nem declara as propostas como aprovadas.

## 1. Versões e decisões que orientam a construção

| Referência | Situação conferida | Como usar |
| --- | --- | --- |
| Backend main `f90764e72ffce8fc924f7b3bb3e36638bbbb092c` | Inclui a PR #8 parcial | Base do mapeamento de entidades deste guia |
| [PR #10 — especificação](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/10), commit `14995ca` | Aberta, em revisão; ainda fora da main | Contém propostas de campos e decisão Keycloak; não é esquema implementado |
| [PR #9 — testes Docker sem banco](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/9) | Cancelada, sem merge, por decisão do grupo | Não faz parte da entrega atual |
| Frontend main `036bf2297f7797aa6c6d51b64e53cf81a9b4feb7` | Formulário com medidas, observação e novas datas | Conferir lacunas da seção 6; não copiar os mocks como dados definitivos |
| [PR frontend #6 — fichas e fluxos](https://github.com/anadiasxavier/pi4-0102-g7-frontend/pull/6), commit `3ee527a` | Documentação em revisão | Alinhar com a PR #10; não representa integração HTTP pronta |

O grupo confirmou **Keycloak para autenticação** em 03/11, conforme informação
do Luis após consulta à equipe. O ADR 0007 dessa decisão está na PR #10.
A proposta antiga de senha/hash e token próprio do ADR 0003 não deve orientar
a criação de `senha_hash` ou `sessoes` no banco de domínio.

O backend usa Java 17, Spring Boot 3.2.5, JPA e PostgreSQL 15. Em
[application.yml](../src/main/resources/application.yml), `ddl-auto: validate`
está ativo: os scripts precisam criar as estruturas compatíveis antes de iniciar
a aplicação. Essa opção não cria nem atualiza tabelas e não valida todas as regras
de negócio. Na main conferida, `banco/` contém apenas LEIAME.md, sem SQL publicado.
Isso não informa se a Lara já possui scripts locais ou em outra branch.

## 2. Entregas acumuladas e efeito no banco

| Entrega / evidência | Comportamento já preparado | Efeito para o banco |
| --- | --- | --- |
| Estrutura base `18ebd4a`, erros/CI `81b7f85` | Camadas, PostgreSQL/Compose, erros JSON e testes | Configuração já espera banco existente; CI com mocks não comprova esquema |
| [PR #2](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/2) | Ambiente de apresentação com frontend separado | Scripts em `banco/`, volume próprio de apresentação |
| [PR #3](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/3), CT02 / `111cedc` | Catálogo de materiais ativos, busca por nome e classificação de saldo | Exige `materiais`, incluindo especificação, quantidade mínima e ativo |
| [PR #4](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/4), CT04 / `f406b7c` | Fila ordenada pela criação, filtro por status e entidade Solicitacao | Exige `solicitacoes` relacionada ao material e com referência ao aluno |
| [PR #5](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/5), `b58085c` | Fila padrão exclui CONCLUIDO | Não excluir registros concluídos do banco; eles serão usados no histórico |
| [PR #6](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/6), CT06 / `fca471d` | Mudança de status, data de atualização e previsão opcional | Preservar status como texto, datas e previsão; conferir transições abaixo |
| [PR #7](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/7) | Convenções alinhadas ao material da disciplina | Sem mudança de esquema |
| [PR #8](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/8), CT05 / `a9e11dc` e `6dc4348` | Validações, operação e testes do estoque, com PATCH desativado por padrão | Não adicionou coluna. Falta persistir a data do estoque em conjunto com o saldo |
| PR #10 / #1136 | Contratos e propostas de material/pedido, Keycloak e requisitos do banco | Prever alinhamento das próximas colunas, sem confundir proposta com entidade pronta |

O histórico atribui os commits funcionais anteriores de catálogo/fila/status a
Matheus Henrique Mansano e os de estoque a Luis Felipe Moura. O README distribui
H01.2 (banco) à Lara; H02.2 (identidade/perfis) ao Matheus; H01.4, H02.1 e H07.1 ao Luis.
As atribuições do planejamento e a autoria dos commits são informações distintas.

### Rotas conferidas

- `GET /api/materiais`: lista direta, consulta materiais ativos e filtra por nome.
- `GET /api/solicitacoes/fila`: lista direta; usa `X-Perfil` provisório e filtro de status.
- `PATCH /api/solicitacoes/{solicitacaoId}/status`: altera status/previsão; também usa `X-Perfil` provisório.
- `PATCH /api/materiais/{materialId}/estoque`: **não registrado por padrão** após a PR #8.
  A propriedade `archflow.estoque.em-desenvolvimento` só é ativada pelos testes.
- Login, criação de pedido por POST, notificações, histórico e painel de prazos:
  sem endpoints implementados na main conferida. Existem contratos e/ou modelos parciais.

Não ativar o PATCH de estoque no ambiente compartilhado para testar o banco antes
da autorização real e da data persistida. A criação pela tela e as notificações
também não podem ser consideradas prontas só porque as tabelas existirem.

## 3. Estruturas exigidas pelas entidades atuais

Fontes: [Material.java](../src/main/java/com/archflow/material/Material.java) e
[Solicitacao.java](../src/main/java/com/archflow/solicitacao/Solicitacao.java).
São as duas entidades JPA identificadas na main.

As tabelas são explícitas em `@Table`; `material_id` na associação também é explícito.
Os demais nomes abaixo são a correspondência física esperada dos atributos na
configuração atual e precisam ser conferidos na partida com `validate`. A maioria
dos campos não tem `@Column` com limites ou nulidade: regras do Java não equivalem
automaticamente a CHECK, NOT NULL, DEFAULT ou UNIQUE no SQL.

### 3.1 Tabela materiais

| Atributo Java | Coluna esperada | Tipo Java / correspondência SQL a validar | Regra observada |
| --- | --- | --- | --- |
| materialId | material_id | Long / inteiro de 64 bits, identidade | PK; geração IDENTITY |
| nome | nome | String / texto compatível com JPA | Java exige nome não vazio e remove espaços externos |
| descricao | descricao | String / texto compatível com JPA | Opcional no código |
| especificacao | especificacao | String / texto compatível com JPA | Descrição fixa do material, já devolvida no catálogo |
| quantidadeDisponivel | quantidade_disponivel | int / inteiro de 32 bits | Não negativa; zero é válido |
| quantidadeMinima | quantidade_minima | int / inteiro de 32 bits | Não negativa; usada para classificar saldo |
| ativo | ativo | boolean / booleano | Catálogo consulta somente true |

Não criar `status_estoque` como dado obrigatório para o Java: o valor é calculado
no backend. Quantidade zero resulta em INDISPONIVEL; positiva e menor ou igual à
mínima resulta em BAIXO; acima da mínima resulta em DISPONIVEL.

`atualizada_em` **ainda não está mapeada em Material**. A necessidade de incluí-la
está na seção 5. Não presumir que a PR #8 já a grava.

### 3.2 Tabela solicitacoes

| Atributo Java | Coluna esperada | Tipo Java / correspondência SQL a validar | Regra observada |
| --- | --- | --- | --- |
| solicitacaoId | solicitacao_id | Long / inteiro de 64 bits, identidade | PK; geração IDENTITY |
| usuarioId | usuario_id | Long / inteiro de 64 bits | Java exige identificador do aluno não nulo |
| material | material_id | Relação ManyToOne / FK para materiais.material_id | Java exige material não nulo; nome da coluna explícito |
| quantidade | quantidade | int / inteiro de 32 bits | Deve ser maior que zero |
| status | status | EnumType.STRING / texto | Gravar o nome do enum, nunca a posição numérica |
| criadaEm | criada_em | LocalDateTime / timestamp compatível | Gerada no construtor do pedido pelo backend |
| atualizadaEm | atualizada_em | LocalDateTime / timestamp compatível | Atualizada pelo setStatus, inclusive no construtor |
| previsaoRetirada | previsao_retirada | LocalDateTime / timestamp compatível | Opcional; não pode ser apagada quando o PATCH omite/null |
| observacao | observacao | String / texto compatível com JPA | Já existe na entidade; preservar mesmo sem dimensões implementadas |

O código usa `usuarioId` como número, sem relação JPA com Usuario: essa entidade
ainda não existe. O destino da FK de usuário depende do modelo de domínio e do
vínculo com Keycloak. Não fazer FK para uma tabela interna do provedor por suposição.
A relação com materiais, por sua vez, já existe no modelo JPA.

Confirmar tamanhos das colunas de texto e precisão temporal com os mapeamentos
antes de fechar o DDL. Não trocar livremente tipos nem impor textos ilimitados ou
novos limites como se já estivessem definidos. LocalDateTime não leva offset no
objeto usado por estes campos; o tratamento de fuso ainda precisa de acordo.

### 3.3 Estados e transições existentes

Valores de `status` que o SQL precisa representar:

`PENDENTE`, `EM_SEPARACAO`, `AGUARDANDO_COMPRA`, `PRONTO_PARA_RETIRADA`, `CONCLUIDO`, `CANCELADO`.

| Estado atual | Destinos permitidos no código |
| --- | --- |
| PENDENTE | EM_SEPARACAO, AGUARDANDO_COMPRA, CANCELADO |
| EM_SEPARACAO | PRONTO_PARA_RETIRADA, AGUARDANDO_COMPRA, PENDENTE, CANCELADO |
| AGUARDANDO_COMPRA | EM_SEPARACAO, CANCELADO |
| PRONTO_PARA_RETIRADA | CONCLUIDO, EM_SEPARACAO, CANCELADO |
| CONCLUIDO | Nenhum |
| CANCELADO | Nenhum |

As transições são verificadas no Java. Uma restrição de valores no banco pode
espelhar a lista, mas não deve inventar transições ou eventos automáticos.
A fila padrão atualmente exclui somente CONCLUIDO: CANCELADO ainda pode aparecer.
Excluir ambos é proposta de revisão, não comportamento já publicado.

O serviço de status mantém a previsão anterior quando a entrada vem sem previsão
ou com null; uma previsão informada no passado é recusada. Preservar parâmetros
do pedido nas atualizações futuras. Não criar baixa automática de estoque ou
notificação por trigger como se esses efeitos já estivessem definidos.

## 4. O que NÃO virou tabela ou coluna só por existir em uma resposta

- `statusEstoque` é calculado a partir do saldo e mínimo, não persistido.
- O campo `material` da resposta da fila é o **nome** obtido da relação; não é
  uma segunda coluna textual de material em solicitacoes.
- `atualizadaEm` da resposta de estoque ainda usa o relógio no DTO; não prova
  coluna de data em materiais. Já a data de atualização de solicitacoes está na entidade.
- `usuarioId` no futuro pedido precisa vir da identidade validada, não de uma
  escolha livre enviada pelo navegador. Menu de aluno/responsável e X-Perfil não autenticam.
- CT10–CT13 são operações internas entre backend e banco, não novas rotas HTTP.

## 5. Acréscimos a alinhar antes das próximas funcionalidades

| Necessidade | Situação / referência | O que Lara e backend precisam combinar |
| --- | --- | --- |
| Data persistida do estoque | Requisito CT05/CT11 e #1154; ausente na entidade | Coluna sugerida materiais.atualizada_em, tipo/precisão/fuso e valor inicial dos registros existentes. Gravar com saldo e devolver o valor persistido; não inventar data histórica |
| Dimensão do pedido | CT03/CT12 e #1145; ausente na entidade | Proposta de dimensao textual, exemplo 300 x 200 mm; unidade/limites ainda em revisão |
| Diâmetro do pedido | CT03/CT12 e #1145; ausente na entidade | Proposta numérica positiva em mm; decidir tipo Java/SQL e precisão antes de codificar |
| Observação do pedido | Já mapeada em Solicitacao | Incluir no script base; não esperar nova entidade para preservá-la |
| Política de parâmetros por material | Proposta da PR #10 | Quais materiais aceitam/exigem DIMENSAO/DIAMETRO; metadados propostos parametrosAceitos/parametrosObrigatorios. Decidir representação física; não inferir pela descrição/nome do material |
| Usuário de domínio | Pacote usuario ainda sem classes | Dados esperados: usuarioId, nome, email, perfil ALUNO/RESPONSAVEL e ativo; curso e origem dos dados a confirmar |
| Vínculo com Keycloak | Provedor confirmado; integração ainda não implementada | Identificador estável do provedor ligado ao usuarioId, origem dos perfis e atualização/desativação. Nome/tipo/unicidade do vínculo a definir com H02.1/H02.2 |
| Notificações | CT13 e H09.1/H09.2; pacote ainda sem classes | Campos propostos notificacaoId, usuarioId, solicitacaoId opcional, tipo, mensagem, criadaEm e lida. Modelo, tipos e evento gerador ainda a alinhar |

**Autenticação:** não criar senha_hash ou sessoes por causa do CT10/ADR 0003 antigo.
Revisar CT01/CT10 junto da PR #10 para Keycloak. O banco de domínio não deve ser
usado para recriar a autenticação própria. A persistência/configuração do Keycloak
é uma decisão separada da criação das tabelas de materiais e pedidos.

**Restrições:** quantidade de estoque e mínima não negativas, quantidade do pedido
positiva e referências válidas são regras já observadas no Java. A equipe deve
espelhá-las de forma consistente no DDL. Tamanhos máximos, unicidade de email,
defaults, ações de exclusão e índices ainda precisam de revisão; não são garantias
já implementadas pelas classes atuais.

**Índices para avaliar, sem assumir aprovação:** consultas existentes usam ativo,
nome, status e ordenação por criada_em; futuras consultas de pedidos/avisos usam
o usuário. Escolher índices pela consulta real, sem prometer que um índice simples
resolve a busca de trecho do nome. Preservar pedidos históricos; política de exclusão
de usuários/materiais e eventuais cascatas precisa ser definida antes do SQL.

## 6. Ajustes percebidos no frontend que impactam o alinhamento

Na main frontend `036bf22`, o modal tem largura e altura em mm, diâmetro, quantidade,
observação, data da solicitação somente leitura e data limite. Não há onSubmit
ou chamada HTTP de criação nesse formulário; o envio ainda não está integrado.

| Dado da tela | Tratamento para alinhar com o banco/API |
| --- | --- |
| Largura e altura | Proposta: compor dimensao textual; não confundir a especificação fixa do material com o pedido personalizado |
| Diâmetro | Proposta: diametro numérico, obrigatório/aceito conforme material; ainda sem coluna e campo Java |
| Data da solicitação | O navegador só mostra uma data local; a data autoritativa de criação deve vir do backend em criadaEm |
| Data limite, nova em `49b59c8` | Ainda sem campo em Solicitacao/CT03 da revisão examinada; confirmar significado, obrigatoriedade, tipo (data ou data/hora), nome e validação antes de criar coluna |
| Previsão de retirada | Já existe em Solicitacao e no PATCH CT06; é a previsão do atendimento, não equivale automaticamente à data limite solicitada pelo aluno |

A revisão documental da PR #10/frontend #6 foi preparada antes dessas novas datas.
Precisa ser complementada no alinhamento, sem transformar a tela publicada em
aprovação automática de um campo ou substituir previsao_retirada por data_limite.
Valores fixos da interface não são cadastro real nem definição da política de materiais.

## 7. Sequência de construção e validação para H01.2

1. Atualizar a base de trabalho a partir da main com a PR #8 integrada e conferir
   se houve commits posteriores a f90764e. Preservar trabalho local da responsável.
2. Conferir os mapeamentos da seção 3 e preparar os scripts sob `banco/`, em branch
   própria e PR. Os nomes `01-tabelas.sql` e `02-dados-exemplo.sql` no LEIAME são
   orientação de organização, não arquivos já entregues.
3. Fechar os pontos da seção 5 com os responsáveis. Coordenar entidade e SQL na
   mesma evolução; criar uma coluna isolada não faz a API gravá-la.
4. Criar as estruturas na ordem das dependências: materiais antes de pedidos;
   usuários antes das FKs de aluno quando o modelo for acordado; notificações
   depois de usuários/pedidos quando o módulo correspondente estiver definido.
5. Preparar dados de demonstração coerentes: materiais ativos/inativos, saldo
   zero/baixo/disponível, aluno/responsável conforme o vínculo de identidade e
   pedidos nos estados necessários. Seeds SQL precisam informar dados obrigatórios:
   não executam os construtores Java que definem datas, ativo e PENDENTE.
6. Iniciar com `ddl-auto: validate`. Confirmar os tipos, identidades, nomes e
   relações pela aplicação, além das restrições verificadas diretamente no SQL.
7. Testar catálogo (ativo, nome, especificação, saldo), fila (ordem/filtro, relação
   com material) e mudança de status/previsão, consultando o banco depois.
8. Manter estoque HTTP desativado até implementar data persistida e Keycloak.
   Depois, provar saldo/data após nova consulta e recusas de acesso. Login,
   criação de pedido e notificações terão validações próprias quando implementados.

Não cadastrar senha em texto puro nem lançar dados reais pessoais como exemplos.
Se os seeds inserirem IDs manualmente, revisar a continuidade da geração IDENTITY
para que o próximo INSERT da aplicação não reutilize um identificador.

### Uso dos ambientes existentes

[docker-compose.yml](../docker-compose.yml) usa PostgreSQL 15 e monta `./banco`
em `/docker-entrypoint-initdb.d:ro`; o [Compose de apresentação](../docker-compose.prod.yml)
faz o mesmo com outro volume. Scripts de inicialização rodam na primeira criação
do banco em volume vazio; acrescentar SQL à pasta depois não executa uma atualização.

Desenvolvimento usa `dados_do_banco`; apresentação usa `dados_do_banco_demo`
(nomes físicos também dependem do projeto Compose). Banco saudável no healthcheck
não comprova tabelas compatíveis. O PostgreSQL local de preparação usa o projeto
`archflow-preparo` e outro volume: não é o esquema oficial. Antes de usar a mesma
porta local, parar somente esse ambiente preservando seus dados.

Para volume com dados, combinar aplicação dos scripts e preservação/backup.
Não usar `docker compose down -v` como atualização habitual: remove os dados.
Não mudar `ddl-auto` para contornar divergências. A etapa alternativa de testes
Docker da PR #9 continua cancelada; este guia usa o ambiente existente do projeto.

## 8. Conferência que falta para considerar o banco integrado

- [ ] SQL publicado e revisado, com nomes/tipos compatíveis com os mapeamentos.
- [ ] Decisões pendentes de datas, medidas, identidade e notificações registradas.
- [ ] Seeds coerentes com PKs/FKs, enums e regras de quantidade.
- [ ] Aplicação inicia com validate no banco oficial, sem esquema criado pelo Java.
- [ ] Catálogo, fila e status conferidos com nova leitura do PostgreSQL.
- [ ] Data do estoque e autorização reais implementadas antes de habilitar o PATCH.
- [ ] Demais integrações validadas conforme cada módulo for entregue.

As caixas ficam em aberto: os 45 testes da PR #8 e sua CI aprovada usam mocks e
não comprovam esses itens. O merge parcial de estoque não fecha o card #1154.
Este documento não altera cards, não registra horas no OpenProject e não envia
mensagens à equipe; serve como material para a revisão e construção com a Lara.
