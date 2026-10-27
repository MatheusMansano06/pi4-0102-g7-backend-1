# Pendencias

O que ainda nao esta decidido, e com quem.

## 1. Para o professor

- **S1 a S4 sao quais datas?** A sprint do OpenProject vai de 14/10 a 22/11, o backlog fala em
  entrega 15/11 e o escopo fala em doze semanas e dezembro. Sao tres prazos diferentes.
- **Quem e o PO?** Os cards H12 e H13 dizem "so entra no quadro se o PO confirmar", e o grupo
  nao definiu essa pessoa. Os dois cards estao parados por isso.
- **Spring Security e permitido?** Optamos por escrever a autenticacao a mao (ADR 0003), entre
  outros motivos porque o Spring Security 6 so configura com lambda, que a disciplina nao ensina.

## 2. Decisao do grupo que ainda falta

**Como o navegador chama a API.** Duas opcoes:

- *Proxy no frontend*: o front chama `/api/materiais` sem host e o Vite repassa para o backend
  por dentro do Docker. Acaba com o CORS e tira a porta do codigo do front.
- *Direto*: o front chama `http://localhost:8080/api/materiais`. Mais obvio de ler, mas exige
  configuracao de CORS no backend e deixa a porta fixa no codigo do front.

Precisa estar decidida **antes do card H02.4** (login funcionando de ponta a ponta).

## 3. Mudancas no banco - para a Lara

Saem das decisoes tomadas junto com as telas ja prontas do frontend.

| Tabela | Mudanca | Por que |
| :--- | :--- | :--- |
| `materiais` | campo `ativo` | o CT02 fala em "materiais ativos" e o CT03 recusa material inativo, mas o campo nao existe |
| `materiais` | campo `especificacao` | o card do catalogo mostra "12 mm, 600 x 400 mm" e nao ha de onde tirar esse texto |
| `materiais` | campo `quantidadeMinima` | sem um minimo nao da para dizer que um material esta com estoque baixo |
| `solicitacoes` | campo `observacao` | o CT03 recebe esse campo e hoje nada o guarda |
| `solicitacoes` | status `CANCELADO` | sem ele, pedido errado ou desistencia ficam presos na fila para sempre |
| `usuarios` | campo `curso` | a barra superior do frontend mostra o curso do aluno |
| **nova** `sessoes` | token, usuario, validade | onde o token do login fica guardado (ADR 0003) |

Os scripts ficam em `banco/`. Ver `banco/LEIAME.md`.

## 4. Mudancas nos contratos - para a Lara e o Luis

| Contrato | Mudanca |
| :--- | :--- |
| CT01 | dizer que o `token` e um UUID e como ele viaja na requisicao |
| CT02 | incluir `especificacao` e `statusEstoque` (DISPONIVEL, BAIXO, INDISPONIVEL) |
| CT03 | tirar `dimensao` e `diametro`; a medida e do material, nao do pedido |
| CT06 | incluir `CANCELADO` na tabela de transicoes |
| CT10 | incluir `curso` |
| novo | logout, que apaga a linha da tabela `sessoes` |
| todos | a coluna de numero HTTP de cada erro, conforme o ADR 0004 |

Falta tambem escrever a **tabela de transicoes de status**, que o CT06 cobra mas nenhum documento tem:

```
PENDENTE              -> EM_SEPARACAO, AGUARDANDO_COMPRA, CANCELADO
EM_SEPARACAO          -> PRONTO_PARA_RETIRADA, AGUARDANDO_COMPRA, PENDENTE, CANCELADO
AGUARDANDO_COMPRA     -> EM_SEPARACAO, CANCELADO
PRONTO_PARA_RETIRADA  -> CONCLUIDO, EM_SEPARACAO, CANCELADO
CONCLUIDO             -> (nenhum, e final)
CANCELADO             -> (nenhum, e final)
```

O aluno so pode cancelar enquanto o pedido estiver PENDENTE. O responsavel cancela de qualquer
status, menos CONCLUIDO. As setas que voltam existem para corrigir engano.

## 5. Para a Mirele - repositorio do frontend

**a) O commit que criou o projeto React apagou documentos.** Sumiram
`docs/adr/0002-adotar-react-como-frontend.md` e os nove fluxos `docs/fluxos/E1` a `E9`,
escritos pela Lara. Estao no historico e voltam com:

```
git checkout 51f9923 -- docs/
```

**b) O README.md ainda e o template do Vite**, falando de React Compiler e TypeScript.

**c) Os nomes dos campos nao batem com os contratos.** Hoje o catalogo usa `id` e `quantidade`;
os contratos dizem `materialId` e `quantidadeDisponivel`. Como ainda nao ha nenhuma chamada de
API no front, e barato alinhar agora.

**d) O Compose de apresentacao precisa de um Dockerfile na raiz do repositorio do frontend.**
O do dia a dia nao precisa de nada. Conteudo para commitar:

```dockerfile
# Etapa 1: compilar o React.
FROM node:20-alpine AS compilacao
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Etapa 2: servir os arquivos prontos pelo nginx.
FROM nginx:alpine
COPY --from=compilacao /app/dist /usr/share/nginx/html
EXPOSE 80
```
