# Banco de dados

Esta pasta e do modulo de banco de dados (Lara), conforme a tabela de modulos da especificacao.

Começar pelo [guia consolidado do backend](../docs/GUIA-BANCO-PARA-LARA.md),
conferido após a PR #8: campos das entidades, alterações da equipe e pendências
de estoque, Keycloak, pedido e notificações. Ele distingue a main das propostas
da PR #10 e não substitui os scripts SQL da responsável.

Todo arquivo `.sql` aqui dentro e executado pelo PostgreSQL **na primeira vez** que o banco sobe.
Eles rodam em ordem alfabetica, por isso o numero na frente do nome:

```
01-tabelas.sql        cria as tabelas
02-dados-exemplo.sql  insere dois alunos, um responsavel, materiais e um pedido
```

O Java nao cria nem apaga tabela nenhuma: ele esta configurado com `ddl-auto: validate`,
que so confere se as classes batem com as tabelas. Ver `docs/adr/0005`.

## Mexeu no script depois do banco ja ter subido?

Os arquivos so rodam na primeira vez. Para o banco ser criado de novo do zero:

```bash
docker compose down -v
docker compose up
```

O `-v` apaga o volume com os dados. Tudo que estava gravado se perde.

## O que ainda precisa existir

A lista de tabelas e campos combinados esta em `docs/pendencias.md`, secao 3.
Os contratos CT10 a CT13, em `docs/contratos`, descrevem o que o backend espera de cada uma.
