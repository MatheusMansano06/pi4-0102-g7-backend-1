# ArchFlow - Backend API

Backend do ArchFlow, sistema de solicitacao e controle de materiais do Atelie CEATEC.

Projeto Integrador IV - PUC-Campinas - Turma 0102 - Grupo 7.

| | |
| :--- | :--- |
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2 |
| Banco | PostgreSQL 15 |
| Ambiente | Docker Compose |
| Frontend | [pi4-0102-g7-frontend](https://github.com/anadiasxavier/pi4-0102-g7-frontend) (React) |

## Como subir numa maquina limpa

Precisa ter **Docker** instalado. Nao precisa de Java nem de Maven: o Docker compila tudo.

**1.** Clone os dois repositorios **lado a lado**, numa pasta qualquer:

```bash
git clone https://github.com/MatheusMansano06/pi4-0102-g7-backend-1.git
git clone https://github.com/anadiasxavier/pi4-0102-g7-frontend.git
```

A pasta precisa ficar assim, com esses nomes:

```
uma-pasta-qualquer/
├── pi4-0102-g7-backend-1/     <- o Compose mora aqui
└── pi4-0102-g7-frontend/
```

**2.** Dentro do backend, crie o seu arquivo de variaveis:

```bash
cd pi4-0102-g7-backend-1
cp .env.example .env
```

**3.** Suba tudo:

```bash
docker compose up
```

Pronto. Fica disponivel em:

| O que | Endereco |
| :--- | :--- |
| Tela do sistema | http://localhost:5173 |
| API | http://localhost:8080/api |
| Banco | localhost:5432 |

Para parar, `Ctrl+C`. Para parar e apagar os dados do banco, `docker compose down -v`.

## Versao de apresentacao

O arquivo acima e o do dia a dia: a tela atualiza sozinha quando alguem salva um arquivo do front.
Para a demonstracao existe uma versao com o React ja compilado e servido pelo nginx:

```bash
docker compose -f docker-compose.prod.yml up --build
```

O `Dockerfile` e o `nginx.conf` ja estao commitados na raiz do repositorio do frontend. Nesta
versao o nginx repassa `/api` para o backend por dentro do Docker, entao a tela fica so em
http://localhost e nao existe CORS.

## Organizacao das pastas

```
.
├── banco/      scripts SQL que criam as tabelas (modulo de banco de dados)
├── docs/
│   ├── adr/          decisoes de arquitetura e o porque de cada uma
│   ├── contratos/    os treze contratos CT01 a CT13
│   ├── convencoes-codigo.md
│   └── pendencias.md o que ainda falta decidir, e com quem
└── src/
    ├── main/java/com/archflow/
    │   ├── autenticacao/   login, logout e conferencia do token
    │   ├── usuario/        usuarios e perfis
    │   ├── material/       catalogo e estoque
    │   ├── solicitacao/    pedidos, fila, status, historico e prazos
    │   ├── notificacao/    avisos dentro da plataforma
    │   └── comum/          tratamento de erro e configuracao
    └── test/java/com/archflow/
```

Cada pasta dentro de `com/archflow` guarda apenas uma funcionalidade, com tudo que ela precisa:
Controller, Service, Repository, Entity e DTOs. Quem abre a pasta `solicitacao` ve a solicitacao
inteira sem procurar em outro lugar.

## Como trabalhamos

- Uma branch por card do OpenProject, com o nome do card: `h05-listar-fila`.
- Antes de escrever codigo, leia `docs/convencoes-codigo.md`.
- Toda decisao que muda o rumo do projeto vira um arquivo em `docs/adr/`.

## Quem faz o que no backend

| Pessoa | Cards |
| :--- | :--- |
| Matheus | H01.1, H01.3, H02.2, H05.1, H08.1, H10.1, H11.2 e as integracoes H05.3, H08.3, H10.3 |
| Luis | H01.4, H02.1, H04.1, H07.1, H09.2 e as integracoes H02.4, H04.3, H07.3 |
| Lara | H01.2 (banco), H01.5, H03.1, H06.1, H09.1 e as integracoes H03.3, H06.3, H09.4 |
