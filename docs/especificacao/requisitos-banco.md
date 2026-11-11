# Requisitos de banco definidos no H01.4

Luis entrega a especificação #1136; Lara implementa/revisa H01.2 e combina a
aplicação no destino. O [dicionário](colunas-materiais-e-solicitacoes.md) define
as seis colunas. Não há pendência de a Lara escolher regras: o Luis confirmou
as duas medidas opcionais para todos os materiais do MVP.

Base atual: main bd733aa, após as PRs #19, #23 e #24, em 11/11/2026.
O Java continua em ddl-auto: validate.

| Conjunto | Definição / situação |
| --- | --- |
| materiais | Campos básicos existentes; quatro BOOLEAN anuláveis, defaults TRUE/FALSE/TRUE/FALSE. CHECKs recusam saldos/mínimos negativos e exigir medida não aceita. Exemplos da PR #24 ainda divergem da política uniforme. |
| solicitacoes | dimensao VARCHAR(255), diametro DOUBLE PRECISION e observacao VARCHAR(255) anuláveis. usuario_id/material_id NOT NULL e FKs para usuarios/materiais. CHECKs de quantidade positiva e diâmetro ausente ou positivo. Identidade, PENDENTE e datas do servidor. |
| parametrosAceitos / parametrosObrigatorios | Projeções dos booleanos, sem colunas/arrays adicionais. Ainda ausentes no DTO do catálogo. |
| Data do estoque | #1154 mapeia materiais.atualizada_em (TIMESTAMP WITHOUT TIME ZONE, anulável, sem default). Em banco existente, revisar/aplicar com H01.2 antes de iniciar a versão, mesmo com o PATCH desativado. |
| usuarios | Criada pela PR #24: usuario_id gerado, nome/email/senha_hash/perfil obrigatórios, ativo DEFAULT TRUE. CHECK de perfil e dados específicos; email único por LOWER, RA/matrícula únicos quando presentes. Aplicação no destino e contas funcionais ainda a comprovar. |
| notificacoes | Detalhamento CT13/H09.1 é da Lara; consulta #1161 é do Luis. Não entregues aqui. |

## Compatibilidade da instalação publicada na PR #24

01-tabelas.sql já cria as três tabelas e é compatível com os mapeamentos usados
na prova isolada da [PR #25](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/25).
Isso não torna a carga de exemplos compatível: a execução de 02-dados-exemplo.sql
foi reproduzida separadamente e falhou na coluna materiais.categoria. A lista
abaixo foi conferida contra o DDL e os enums/entidades, sem alterar os scripts.

| Arquivo de exemplos | Esquema/contrato vigente | Ajuste necessário em H01.2 |
| --- | --- | --- |
| materiais.categoria, unidade_medida | Não existem no DDL nem no Material atual | Conciliar a carga com os campos publicados, sem adicionar requisito por suposição |
| materiais.quantidade_estoque | quantidade_disponivel | Usar o nome do esquema/JPA |
| Solicitacao: data_solicitacao, data_atualizacao, retirada_prevista | criada_em, atualizada_em, previsao_retirada | Usar os três nomes definidos em CT12/JPA |
| APROVADA, RETIRADA, REJEITADA | PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO, CANCELADO | Conciliar os cenários com o enum; não inferir equivalência automática |
| Materiais com regras diferentes, inclusive diâmetro obrigatório | Ambas as medidas permitidas e opcionais para todos | Restaurar a política explicitamente confirmada pelo Luis |
| senha_hash = DEMO_SEM_LOGIN | Hash BCrypt válido no CT10 | Prover credenciais de demonstração por procedimento próprio; placeholder não autentica |
| Somente três alunos de exemplo | #1141 exige aluno e responsável | Prover também responsável com matrícula/cargo e conta ativa |

O SQL de usuários exige RA e curso para ALUNO, com matrícula funcional/cargo
nulos; para RESPONSAVEL exige matrícula funcional/cargo, com RA/curso nulos.
A entidade Usuario lê apenas os seis campos comuns. Não presumir que salvar um
novo Usuario pela entidade atual satisfaça esses CHECKs; fixtures/provisionamento
devem preencher também os dados específicos exigidos pelo SQL.

Nenhuma dessas divergências modifica a definição das seis colunas, autoriza
alterar contratos silenciosamente ou comprova migração no banco da equipe.

## Banco existente

1. Combinar destino e cópia de segurança; conferir os tipos com o dicionário.
2. Aplicar o [script](../sql/H04.1-colunas-propostas.sql): adiciona campos,
   estabelece defaults e configura as quatro regras em todos os materiais.
3. O UPDATE muda só as regras, sem apagar dados ou alterar saldo, ativo, pedidos
   e datas. Repetição preserva o resultado. Tipos incompatíveis preexistentes
   exigem correção específica; IF NOT EXISTS não os converte.
4. Conferir e iniciar com validate. Arquivo novo não migra um volume inicializado.
5. Conferir a criação/migração de usuarios e as novas FKs/CHECKs da PR #24.
   O script das seis colunas não entrega essa migração. Validar previamente
   usuários referenciados, dados de perfil e valores existentes incompatíveis;
   não reatribuir pedidos, excluir linhas ou impor novos dados por suposição.
6. Testar em ambiente dedicado com contas ativas de aluno e responsável.
   Conciliar seus IDs com os pedidos de exemplo sem reatribuir dados existentes
   por suposição. Configurar a assinatura do JWT no backend.

CT10 lê o hash para conferir a senha no backend. A ficha CT01 não o expõe à
tela. O ADR 0008 não requer tabela de sessões.

## Validação disponível

A PR #25 executou 95 testes, incluindo sete CT03 e dois CT05 em PostgreSQL
16.15 isolado com 01-tabelas.sql atual, sem carga de exemplos nem esquema
substituto. Usuários e materiais de teste foram revertidos/removidos, e os
bancos descartáveis excluídos. Não comprova login pela tela, execução no
PostgreSQL 15 do Compose ou alteração de um banco compartilhado existente.

Não executar DROP/recriar volume. A aplicação no banco compartilhado continua
coordenada com H01.2; esta entrega prepara o script, sem executá-lo nesse banco.
