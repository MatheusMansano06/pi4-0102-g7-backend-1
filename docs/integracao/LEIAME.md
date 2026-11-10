# Como o front se conectou com o back

Esta pasta explica, em linguagem simples, como a tela do ArchFlow passou a
buscar dados de verdade no servidor, em vez de usar uma lista escrita na mao
dentro do codigo.

## O problema

Sao dois programas separados, cada um no seu endereco:

| Quem | Endereco | O que faz |
| :--- | :--- | :--- |
| Tela (frontend) | `http://localhost:5173` | o que o aluno ve |
| Servidor (backend) | `http://localhost:8080` | guarda e devolve os dados |

Porta diferente conta como endereco diferente. E o navegador, por seguranca,
**nao deixa** uma pagina de um endereco pedir dados a outro endereco. Essa
regra tem nome: CORS. Sem ela, qualquer site aberto numa aba poderia pedir os
dados de um sistema onde voce esta logado em outra.

## A escolha do grupo: chamada direta

Havia dois caminhos. O grupo escolheu o primeiro:

1. **Direta** — a tela chama `http://localhost:8080/api/materiais`, com o
   endereco inteiro escrito. Da para ler o codigo e ver exatamente para onde a
   chamada vai. Em troca, o servidor precisa autorizar a tela.
2. **Proxy** — a tela chamaria so `/api/materiais` e o Vite repassaria por
   baixo dos panos. Some com o problema do CORS, mas esconde o caminho de quem
   le o codigo.

Escolhemos a direta justamente porque ela mostra o mecanismo: quem le o projeto
ve a chamada saindo, ve o servidor autorizando e entende por que isso existe.

## As duas pecas

### 1. No servidor: a autorizacao

Arquivo: [`ConfiguracaoDeCors.java`](../../src/main/java/com/archflow/comum/ConfiguracaoDeCors.java)

Essa classe diz ao servidor: "aceite chamadas vindas da tela do ArchFlow". Ela
lista o endereco da tela, e so ele. Qualquer outro endereco continua barrado.

Para conferir a permissão de CORS, reproduza a consulta prévia do navegador
com o sistema no ar:

```bash
curl -i -X OPTIONS -H "Origin: http://localhost:5173" -H "Access-Control-Request-Method: GET" -H "Access-Control-Request-Headers: Authorization" http://localhost:8080/api/materiais
```

A resposta traz a linha `Access-Control-Allow-Origin: http://localhost:5173`.
Trocando o endereco por outro qualquer, a resposta vira `403`.
Essa consulta prévia não precisa de token; o GET em si precisa de Bearer.

### 2. Na tela: a chamada

No repositorio do frontend:

- `src/api.js` guarda o endereco do servidor numa linha so. Mudou a porta,
  muda aqui e pronto.
- `src/pages/student/Catalog/Catalog.jsx` chama `/api/materiais` quando a tela
  abre, e mostra "Carregando..." ate a resposta chegar. Se o servidor estiver
  fora do ar, aparece um aviso em vez de uma tela quebrada.

Os nomes dos campos do servidor e os da tela nao sao os mesmos. A traducao
acontece numa funcao unica dentro do `Catalog.jsx`:

| O servidor manda (CT02) | A tela usa |
| :--- | :--- |
| `materialId` | `id` |
| `quantidadeDisponivel` | `quantidade` |
| `statusEstoque` (`DISPONIVEL`, `BAIXO`, `INDISPONIVEL`) | `status` e a cor da etiqueta |

## Como ver funcionando

Antes de subir o ambiente, configure `ARCHFLOW_TOKEN_SEGREDO` no backend
e confirme a tabela `usuarios` no banco de teste, com contas dos dois perfis.

```bash
docker compose up
```

Entre pela tela com a revisão da
[PR #14 do frontend](https://github.com/anadiasxavier/pi4-0102-g7-frontend/pull/14)
e abra `http://localhost:5173/student/catalog`. Confira se o GET autenticado
retorna os materiais desse banco. Repita sem o token para confirmar HTTP 401.

## O que ainda nao esta ligado

O login da API está disponível desde a PR #19. A main do frontend ainda não
envia as credenciais ao login nem o Bearer nas consultas do catálogo e estoque;
a PR #14 do frontend liga o login e o catálogo. Fila, histórico e notificações
ainda não consultam a API pela tela. Veja a
[matriz de autenticação](../especificacao/autenticacao.md) para o estado das rotas.
