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

Para conferir que funciona, com o sistema no ar:

```bash
curl -i -H "Origin: http://localhost:5173" http://localhost:8080/api/materiais
```

A resposta traz a linha `Access-Control-Allow-Origin: http://localhost:5173`.
Trocando o endereco por outro qualquer, a resposta vira `403`.

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

```bash
docker compose up
```

Depois abra `http://localhost:5173/student/catalog`. Os materiais que aparecem
sao os que estao gravados no banco. Para provar, mude a quantidade de um
material direto no banco e recarregue a tela:

```bash
docker compose exec banco psql -U archflow -d archflow -c "UPDATE materiais SET quantidade_disponivel = 3 WHERE material_id = 1;"
```

## O que ainda nao esta ligado

Apenas o catalogo busca dados de verdade. Fila, prazos, historico e
notificacoes continuam com lista fixa no codigo da tela. O login tambem nao
existe ainda: enquanto nao existe, o perfil e o numero do aluno viajam nos
cabecalhos provisorios `X-Perfil` e `X-Usuario-Id`.
