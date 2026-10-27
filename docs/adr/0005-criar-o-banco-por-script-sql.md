# ADR 0005: criar o banco por script SQL

**Status:** proposto (aguardando aprovacao do grupo)

**Contexto:** O JPA consegue criar as tabelas sozinho a partir das classes Java (`ddl-auto`), mas a tabela de modulos da especificacao define "Banco de dados" como um modulo proprio, sob responsabilidade da Lara. Se o Java criar as tabelas, o modulo dela deixa de ter entregavel. Alem disso, misturar as duas formas faz o Hibernate apagar e recriar o schema a cada reinicio, perdendo os dados.

**Decisao:** As tabelas sao criadas por scripts SQL na pasta `banco/`, escritos pela responsavel do modulo de banco. O PostgreSQL executa todo arquivo `.sql` dessa pasta na primeira vez que o container sobe. O Java fica com `ddl-auto: validate`, ou seja, apenas confere se as classes batem com as tabelas e avisa quando nao batem.

**Alternativas consideradas:**
- Flyway: descartado por enquanto porque e uma dependencia nova e mais uma ferramenta para o grupo aprender. Fica anotado como melhoria caso o schema mude muito durante o projeto.
- Deixar o JPA criar (`ddl-auto: update` ou `create`): descartado porque anula o modulo de banco de dados e porque `create` apaga os dados a cada reinicio.

**Consequencias:**
- Positivas: o trabalho do modulo de banco fica visivel e versionado; o `validate` avisa cedo quando uma classe Java e uma tabela saem de sincronia; a responsavel pelo banco nao precisa mexer em Java.
- Negativas: os scripts so rodam quando o banco sobe pela primeira vez, entao mudar o schema exige apagar o volume (`docker compose down -v`) ou escrever a alteracao a mao; classe Java e tabela precisam ser mantidas iguais por duas pessoas diferentes.
