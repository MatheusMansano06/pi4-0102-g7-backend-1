# Pendencias

O que ainda nao esta decidido, e com quem.

Atualização #1136: a revisão funcional está em
[H01.4-alinhamento](especificacao/H01.4-alinhamento.md). As propostas de autenticação,
unidades e parâmetros aguardam validação da equipe. Keycloak foi escolhido em 03/11; os detalhes de integração seguem pendentes ([ADR 0007](adr/0007-adotar-keycloak.md)). A lista abaixo não comprova
que os campos ou as tabelas já estejam implementados.

## 1. Para o professor

- **S1 a S4 sao quais datas?** A sprint do OpenProject vai de 14/10 a 22/11, o backlog fala em
  entrega 15/11 e o escopo fala em doze semanas e dezembro. Sao tres prazos diferentes.
- **Quem e o PO?** Os cards H12 e H13 dizem "so entra no quadro se o PO confirmar", e o grupo
  nao definiu essa pessoa. Os dois cards estao parados por isso.
- **Autenticação definida pelo grupo:** Keycloak, confirmado pelo usuário em 03/11. Detalhar integração, perfis e vínculo com os usuários de domínio antes de implementar; a proposta própria do ADR 0003 foi substituída.

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
| `solicitacoes` | campos `dimensao`, `diametro` e `observacao` | preservar parâmetros do pedido; observacao já existe na entidade, mas o SQL continua pendente |
| `materiais` | data `atualizadaEm` persistida | CT05/CT11 exigem gravar o horário junto do saldo; o DTO da PR #8 ainda não atende |
| política de parâmetros | aceitos e obrigatórios por material | proposta de metadados a validar; não inferir regras do texto especificacao |
| `solicitacoes` | status `CANCELADO` | sem ele, pedido errado ou desistencia ficam presos na fila para sempre |
| `usuarios` | campo `curso` | a barra superior do frontend mostra o curso do aluno |
| identidade Keycloak ↔ usuarioId | definir vínculo com usuário de domínio | Keycloak escolhido; sessoes e senhaHash da proposta própria não são requisitos aprovados |

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

O bloco acima descreve o modelo parcial já existente. A revisão #1136 também
requer dimensão e diâmetro do pedido e a data persistida do estoque, ainda ausentes
no código atual. Representação física, limites e política de parâmetros precisam
de validação; a lista completa está em [requisitos-banco](especificacao/requisitos-banco.md).

## 4. Mudancas nos contratos - para a Lara e o Luis

| Contrato | Mudanca |
| :--- | :--- |
| CT01 | proposta de transporte Bearer documentada; Keycloak escolhido; revisar fluxo e rota antigos, sem impor emissão própria |
| CT02 | incluir `especificacao` e `statusEstoque` (DISPONIVEL, BAIXO, INDISPONIVEL) |
| CT03/CT12 | manter dimensao, diametro e observacao no pedido; proposta em mm e adaptação do modal em revisão #1136 |
| CT06 | incluir `CANCELADO` na tabela de transicoes |
| CT10 | incluir `curso` |
| novo | logout e invalidação conforme a integração Keycloak a definir |
| todos | a coluna de numero HTTP de cada erro, conforme o ADR 0004 |

A **tabela de transições do modelo atual** foi incluída no CT06 nesta revisão.
Referência para conferência do comportamento existente:

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

**a) Recuperação documental preparada no #1136.** O commit que criou o React removeu
`docs/adr/0002-adotar-react-como-frontend.md` e os nove fluxos `docs/fluxos/E1` a `E9`,
escritos pela Lara. A PR documental do frontend recupera os arquivos de 51f9923
com revisão funcional, sem alterar componentes. A main ainda depende dessa PR.
Origem histórica dos arquivos:

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
