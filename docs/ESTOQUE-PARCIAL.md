# Estoque — persistência da data (#1154)

A PR #8, já integrada, validava o saldo, mas o horário do CT05 era calculado
somente na resposta. Este complemento grava saldo e data na mesma transação;
a resposta usa o valor do material gravado. O relógio é o do backend, com
precisão de segundos, sem deslocamento de fuso no CT05 ou na coluna.

A alteração ainda exige RESPONSAVEL. Zero continua válido, e quantidades
negativas ou inválidas são recusadas. A rota PATCH segue desativada por padrão
e o JSON do catálogo não muda. A integração pela tela fica no #1156.

## Banco existente

A coluna `materiais.atualizada_em` está no script para instalações novas.
O [script de atualização](sql/H07.1-data-do-estoque.sql) adiciona somente
essa coluna ao banco já criado. Não há preenchimento retroativo nem
alteração de saldo, pedidos ou volume. A coluna permite NULL até a primeira
alteração registrada. Se a coluna já existir, conferir o tipo: `IF NOT EXISTS`
não corrige um tipo diferente.

Revisar a coluna e combinar a migração com H01.2/Lara antes de iniciar esta
versão. Como o Hibernate usa `ddl-auto: validate`, o esquema precisa conter
a coluna mesmo com o PATCH desativado. A PR não aplica a migração por conta
própria.

## Verificação em PostgreSQL

1. Usar um banco local descartável `archflow_ct05_teste_<sufixo>`.
   Em instalação nova, aplicar [01-tabelas.sql](../banco/01-tabelas.sql).
   Para testar migração, usar uma cópia do esquema anterior e aplicar o
   [ALTER](sql/H07.1-data-do-estoque.sql), verificando que o saldo anterior
   permanece e a data começa como NULL.
2. O contexto completo do backend também exige a tabela `usuarios`. Ela já é
   criada pelo 01-tabelas.sql desde a PR #24. Não aplicar a antiga fixture do
   #1141: era um esquema auxiliar e foi removida da prova atual. Na cópia
   migrada, usar o esquema compatível de H01.2, preservando as referências.
   Nenhuma conta real é necessária: o teste emite credenciais para identidades
   de teste e usa o filtro da aplicação.
3. Definir `ARCHFLOW_CT05_TESTE_URL` com uma URL JDBC de loopback no banco
   dedicado, `ARCHFLOW_CT05_TESTE_USUARIO`, `ARCHFLOW_CT05_TESTE_SENHA` e
   `ARCHFLOW_CT05_TESTE_SEGREDO` (chave exclusiva de teste, ao menos 32 bytes).
   Executar `mvn -Dtest=MaterialEstoqueBancoTest test`.

A prova habilita `archflow.estoque.em-desenvolvimento` apenas no teste.
Confere o novo saldo e a data em outra consulta após a resposta do PATCH,
e preservação de ambos quando faltam credencial ou permissão, ou a quantidade
é inválida. Não habilitar a rota em ambiente compartilhado até validar o fluxo.

## Evidência da validação da PR

A prova foi executada com PostgreSQL 16.15 local no Ubuntu, isolado do banco da
equipe. Os dois cenários de MaterialEstoqueBancoTest passaram em instalação
nova e em uma cópia do esquema anterior com a migração aplicada: quatro
execuções aprovadas, sem cenários ignorados.

O ALTER foi executado duas vezes na cópia. Os seis materiais e cinco pedidos
de exemplo foram preservados, com a nova data inicialmente NULL e sem default.
A resposta do PATCH e a releitura após a transação confirmaram saldo e data
iguais. As recusas mantiveram os valores anteriores. Os dois bancos de teste
foram removidos e a instância isolada encerrada.

O Compose do projeto usa PostgreSQL 15 e não foi executado nesta prova. A
migração do ambiente da equipe continua pendente: aplicar a coluna antes de
iniciar a nova versão, mesmo com a rota desativada.
