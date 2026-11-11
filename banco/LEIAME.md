# Banco de dados

Esta pasta e do modulo de banco de dados (Lara), conforme a tabela de modulos da especificacao.

Começar pelo [guia consolidado do backend](../docs/GUIA-BANCO-PARA-LARA.md),
atualizado após as PRs #23/#24: entidades, login próprio do ADR 0008,
restrições de usuários, estoque e divergências dos exemplos. A PR #10 já foi
integrada; medidas opcionais para todos permanecem a definição do Luis.

Todo arquivo `.sql` aqui dentro e executado pelo PostgreSQL **na primeira vez** que o banco sobe.
Eles rodam em ordem alfabetica, por isso o numero na frente do nome:

```
01-tabelas.sql        cria as tabelas
02-dados-exemplo.sql  exemplos que ainda precisam ser conciliados com o esquema
```

O Java nao cria nem apaga tabela nenhuma: ele esta configurado com `ddl-auto: validate`,
que so confere se as classes batem com as tabelas. Ver `docs/adr/0005`.

O 01 cria usuarios, materiais e solicitacoes. A PR #24 exige RA/curso para
ALUNO, matrícula funcional/cargo para RESPONSAVEL e liga solicitacoes.usuario_id
a usuarios. O 02 usa colunas que o 01 não cria, status não existentes no enum e
hashes DEMO_SEM_LOGIN, sem conta responsável. Não tratá-lo como carga válida para
demonstrar login/pedidos. A reprodução isolada falhou em materiais.categoria;
ver a [lista exata de compatibilidade](../docs/especificacao/requisitos-banco.md).

## Mexeu no script depois do banco ja ter subido?

Os scripts de inicialização só rodam na primeira vez. Em um banco existente,
combine a migração com H01.2 e preserve o volume. Para o #1154, o
[script de atualização de estoque](../docs/sql/H07.1-data-do-estoque.sql)
adiciona `materiais.atualizada_em` sem alterar o saldo. A coluna precisa
existir antes de iniciar a nova versão com `ddl-auto: validate`.

## O que ainda precisa existir

A lista atual está em [pendências](../docs/pendencias.md) e
[requisitos do banco](../docs/especificacao/requisitos-banco.md).
Os contratos CT10–CT13 descrevem o que o backend espera de cada operação interna.
Ainda falta esquema de notificações; publicar criação de tabelas não comprova
migração de um banco existente ou contas funcionais de demonstração.
