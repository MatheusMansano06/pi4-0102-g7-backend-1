# Preparar o Docker enquanto os scripts SQL nao chegam

O PostgreSQL pode iniciar vazio. A API completa ainda depende dos scripts da
responsavel pelo banco: `ddl-auto: validate` exige tabelas compativeis com as
entidades. Um banco saudavel no Docker nao comprova que essas tabelas existem.

## 1. Conferir o Docker Desktop

Abra o Docker Desktop e aguarde o mecanismo Linux ficar pronto. No terminal:

```bash
docker version
docker compose version
```

`docker version` precisa mostrar Client e Server. Se mostrar somente Client ou
erro de conexao, ainda nao e possivel executar containers. No Windows, o Docker
Desktop tambem depende do WSL/virtualizacao configurados na maquina.

## 2. Rodar os testes do backend sem banco

Na pasta do backend:

```bash
docker compose -p archflow-testes -f docker-compose.testes.yml run --build --rm testes
```

Esse arquivo usa a etapa `testes` do Dockerfile. Ele compila e executa a suite
JUnit da branch atual com Java 17 e Maven dentro do container. Nao inicia a API,
PostgreSQL ou frontend, nao publica portas e nao precisa de `.env`. O comando
devolve erro se a compilacao ou algum teste falhar; procure `BUILD SUCCESS` e o
resumo dos testes para confirmar o resultado.

Os testes continuam respeitando seus limites: regras e acesso HTTP com
repositorio simulado nao comprovam SQL, persistencia ou autenticacao reais. A
quantidade de testes depende dos commits presentes na branch; PRs separadas ainda
nao integradas a main nao entram automaticamente nesta entrega.

## 3. Conferir os arquivos de configuracao

Ainda sem executar containers, podem ser conferidos os tres modos:

```bash
docker compose --env-file .env.example config --quiet
docker compose --env-file .env.example -f docker-compose.prod.yml config --quiet
docker compose -f docker-compose.testes.yml config --quiet
```

Sucesso aqui significa que a configuracao foi interpretada, nao que a aplicacao
iniciou. O ambiente completo tambem precisa do clone do frontend na pasta irma
indicada no README e dos scripts SQL compativeis.

## 4. Preparar um PostgreSQL vazio e isolado

Copie `.env.example` para `.env` se ainda nao existir; preserve qualquer `.env`
que ja esteja configurado. No PowerShell, a copia inicial e:

```powershell
Copy-Item .env.example .env
```

As credenciais de exemplo sao locais. Se a porta 5432 ja estiver ocupada, ajuste
`PORTA_BANCO` no seu `.env` antes de iniciar. O `.env` nao e versionado nem enviado
no contexto de build do Docker.

Use um nome de projeto separado para nao inicializar o volume do sistema completo
antes da entrega dos scripts:

```bash
docker compose -p archflow-preparo up -d banco
docker compose -p archflow-preparo ps banco
docker compose -p archflow-preparo exec -T banco sh -c 'pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
docker compose -p archflow-preparo exec -T banco sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -c "SELECT current_database(), current_user;"'
```

Esses comandos iniciam somente `banco`, sem API ou frontend. O nome separado cria
um volume exclusivo de preparacao. Nao inserir tabelas ou dados de dominio nesse
volume como substituto dos scripts da equipe.

Para parar e remover apenas os containers desse ambiente, preservando os dados:

```bash
docker compose -p archflow-preparo down
```

## 5. Quando os scripts SQL forem entregues

Conferir com a responsavel pelo banco os arquivos em `banco/`, as tabelas/colunas
esperadas e os dados de demonstracao. Depois, parar o ambiente `archflow-preparo`
para liberar a porta e seguir o README para subir o sistema completo.

Os scripts de inicializacao do PostgreSQL so rodam automaticamente quando o
volume ainda esta vazio. Colocar um SQL novo na pasta depois da primeira
inicializacao nao o executa. Para um volume existente, combinar a aplicacao dos
scripts com backup e preservacao dos dados; nao usar `down -v` como atualizacao
habitual, porque isso apaga o volume.

Os volumes de desenvolvimento, apresentacao e preparacao sao diferentes; aplicar
scripts em um deles nao atualiza os demais. Manter `ddl-auto: validate`: a criacao
oficial do esquema permanece nos scripts SQL da equipe.
