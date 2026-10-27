# ADR 0006: ambiente Docker com dois repositorios

**Status:** proposto (aguardando aprovacao do grupo)

**Contexto:** O professor pediu dois repositorios, um para o frontend e outro para o backend. O card H01.1 pede um Compose que suba frontend, backend e PostgreSQL com um comando so, com o backend esperando o banco ficar pronto. Um arquivo do Compose precisa enxergar os dois repositorios, que sao clones separados.

**Decisao:** O `docker-compose.yml` fica no repositorio do backend, que e o que tem o banco e a API, e aponta para o frontend como pasta irma `../pi4-0102-g7-frontend`. Sao dois arquivos: `docker-compose.yml` para o dia a dia, com atualizacao automatica da tela, e `docker-compose.prod.yml` para a apresentacao, com o React compilado e servido pelo nginx. Os dois repositorios precisam ser clonados lado a lado.

**Alternativas consideradas:**
- Submodule do git: descartado porque confunde quem nao conhece e cria um repositorio dentro do outro.
- Um terceiro repositorio so de infraestrutura: descartado porque o professor pediu dois repositorios.
- Um Compose em cada repositorio, ligados por rede externa: descartado porque exige subir duas coisas em ordem certa, o que contraria o "um comando so" do card.

**Consequencias:**
- Positivas: um comando sobe tudo; o backend espera o banco por healthcheck; quem mexe no frontend nao precisa saber Docker, porque no dia a dia usamos a imagem do Node direto, sem Dockerfile no repositorio dele.
- Negativas: o repositorio do frontend sozinho nao sobe o sistema, so junto com o do backend; todo mundo precisa clonar as duas pastas lado a lado com os nomes exatos; o Compose de apresentacao exige um Dockerfile commitado no repositorio do frontend.

**Pendente:** falta decidir como o navegador chama a API, se por proxy no frontend ou direto em `localhost:8080`. A escolha muda uma classe de CORS no backend e o bloco de proxy do nginx. Precisa estar decidida antes do card H02.4.
