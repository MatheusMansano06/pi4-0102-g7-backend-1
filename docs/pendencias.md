# Pendencias

O que ainda nao esta decidido, e com quem.

## 1. Para o professor

- **S1 a S4 sao quais datas?** A sprint do OpenProject vai de 14/10 a 22/11, o backlog fala em
  entrega 15/11 e o escopo fala em doze semanas e dezembro. Sao tres prazos diferentes.
- **Quem e o PO?** Os cards H12 e H13 dizem "so entra no quadro se o PO confirmar", e o grupo
  nao definiu essa pessoa. Os dois cards estao parados por isso.
- **Spring Security e permitido?** Optamos por escrever a autenticacao a mao (ADR 0003), entre
  outros motivos porque o Spring Security 6 so configura com lambda, que a disciplina nao ensina.

## 2. ~~Decisao do grupo que ainda falta~~ - decidida em 04/11/2026

**Como o navegador chama a API: chamada direta.** O front chama
`http://localhost:8080/api/materiais` com o endereco inteiro, e o backend autoriza a tela pelo
CORS. A opcao de proxy no Vite foi descartada: ela resolveria o CORS sozinha, mas esconde o
caminho da chamada de quem le o codigo.

Ver [`docs/integracao/LEIAME.md`](integracao/LEIAME.md), que explica as duas pecas e como
conferir que funciona.

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

A tabela `solicitacoes` ainda nao existe no script, e o H05.1 (fila de atendimento) ja le dela.
A classe `Solicitacao` do backend espera estas colunas, com estes nomes:

```
solicitacao_id     identidade, gerada pelo banco
usuario_id         o aluno que pediu
material_id        o material pedido
quantidade         inteiro maior que zero
status             texto, um de PENDENTE, EM_SEPARACAO, AGUARDANDO_COMPRA,
                   PRONTO_PARA_RETIRADA, CONCLUIDO, CANCELADO
criada_em          data e hora da criacao
atualizada_em      data e hora da ultima troca de status
previsao_retirada  data e hora, aceita vazio
observacao         texto, aceita vazio
```

Enquanto o script nao criar a tabela, o backend compila e os testes passam, mas a aplicacao nao
sobe: o `ddl-auto: validate` recusa classe sem tabela.

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

**d) ~~O Compose de apresentacao precisa de um Dockerfile na raiz do repositorio do frontend.~~**
Resolvido em 28/10/2026. O `Dockerfile` e o `nginx.conf` estao na raiz do repositorio do
frontend (PR anadiasxavier/pi4-0102-g7-frontend#1). O nginx serve o React compilado e repassa
`/api` para o backend por dentro da rede do Docker, entao a versao de apresentacao nao tem CORS,
qualquer que seja a decisao da pendencia 2. O ambiente do dia a dia nao mudou.
