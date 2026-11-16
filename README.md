# ArchFlow — Backend

Sistema de solicitação e controle de materiais do Ateliê CEATEC, PI IV, turma 0102, grupo 7. Java 17, Spring Boot, JPA e PostgreSQL 15. Frontend React no [repositório da interface](https://github.com/anadiasxavier/pi4-0102-g7-frontend).

## Instalação em máquina limpa

Requisitos: Git e Docker Desktop iniciado com suporte a contêineres Linux. Clone os dois repositórios lado a lado, preservando os nomes:

```bash
git clone https://github.com/MatheusMansano06/pi4-0102-g7-backend-1.git
git clone https://github.com/anadiasxavier/pi4-0102-g7-frontend.git
cd pi4-0102-g7-backend-1
cp .env.example .env
```

No PowerShell, o último comando é `Copy-Item .env.example .env`. No `.env`, defina sua senha de banco e um `ARCHFLOW_TOKEN_SEGREDO` com pelo menos 32 caracteres. O arquivo não é versionado. Na primeira instalação, o Compose cria as tabelas oficiais e os exemplos automaticamente em um volume novo.

Para a apresentação, com interface compilada e servida pelo nginx:

```bash
docker compose -f docker-compose.prod.yml up --build -d
docker compose -f docker-compose.prod.yml ps
```

Abra [http://localhost:8081](http://localhost:8081). A API fica em `http://localhost:8080/api`; a tela encaminha `/api` internamente, sem depender de outro endereço de servidor. As portas de apresentação ficam limitadas à própria máquina. `PORTA_APRESENTACAO` e `PORTA_BACKEND` permitem escolher outras portas livres.

Contas públicas de demonstração, exclusivamente para banco novo com os exemplos:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| Aluno | aluno1@archflow.demo | Archflow@123 |
| Outro aluno | aluno2@archflow.demo | Archflow@123 |
| Responsável | responsavel@archflow.demo | Archflow@123 |

O cadastro de aluno também está disponível na tela. O login é próprio, BCrypt/JWT, conforme ADR 0008. O token identifica o usuário e o perfil nas rotas protegidas.

Para desenvolvimento, use `docker compose up --build`; a interface fica em `http://localhost:5173`, com proxy `/api`, e o banco em localhost:5432. Os arquivos de desenvolvimento são montados no contêiner. Não execute os dois ambientes simultaneamente nas mesmas portas.

Para parar a apresentação preservando dados, use `docker compose -f docker-compose.prod.yml down`. O ambiente de desenvolvimento e o de apresentação usam volumes diferentes.

## Banco existente e estoque

Os scripts de `banco/` são inicializadores de banco vazio. Não reaplique o seed em banco existente. Confira o esquema com [H07.1](docs/sql/H07.1-conferir-implantacao.sql) e combine a atualização com quem administra o destino. A coluna `materiais.atualizada_em`, as regras de medidas, os campos do pedido e a tabela `notificacoes` precisam existir antes da ativação.

CT05 fica desligado se a aplicação for iniciada sem a propriedade `archflow.estoque.em-desenvolvimento`. Os dois Compose habilitam explicitamente o fluxo no ambiente local preparado. Para outro destino, valide o esquema e a integração antes de habilitar. Atualizar estoque substitui o saldo e grava a data no servidor; criar pedido ou alterar status não baixa saldo automaticamente.

Dimensão e diâmetro são opcionais para todos os materiais do MVP. Materiais cadastrados pelo responsável seguem essa política. Não existe `dataLimite` contratada; a previsão pertence ao pedido e pode permanecer vazia.

## Fluxo de apresentação

1. Entre como aluno, consulte o catálogo e crie uma solicitação com observação.
2. Entre como responsável, confira a fila e filtre por status. Atualize o pedido para Em separação e depois Pronto para retirada, informando previsão futura.
3. Entre novamente como aluno: confira o painel de prazos, histórico e avisos. Outro aluno não deve receber esse pedido/aviso.
4. Como responsável, cadastre um material e atualize o saldo; confira saldo e data após recarregar a tela.

Status, previsão, data e aviso são transacionais. Os tipos de aviso seguem o [alinhamento H01.5](docs/especificacao/H01.5-fechamento.md). Não há envio de e-mail nem marcação de aviso como lido no MVP.

## Validação

Com Java 17 e Maven: `mvn test`. Os testes reais de PostgreSQL exigem as variáveis indicadas nas classes de teste CT01/CT03/CT05/CT07/CT14; sem elas, esses cenários são ignorados. Um resultado verde sem banco não comprova integração. A evidência do fechamento está em [H11](docs/integracao/H11-fechamento.md).

Contratos em `docs/contratos`, decisões em `docs/adr` e entidades/serviços em `src/main/java/com/archflow`. A disciplina e a revisão de código orientam o uso das estruturas já adotadas no projeto.
