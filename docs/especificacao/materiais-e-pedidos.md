# Materiais, parâmetros e pedidos — proposta #1136

**Proposta em revisão.** Preserva os nomes CT03 `dimensao`, `diametro` e
`observacao`. O formato em mm e os metadados abaixo aguardam validação da equipe.

## Material consultado em CT02 e CT11

| Campo | Referência | Uso |
| --- | --- | --- |
| materialId, nome, descricao, especificacao | Entidade/saída já existentes | Identificação e descrição fixa do material |
| quantidadeDisponivel | Entidade existente | Saldo inteiro não negativo |
| quantidadeMinima | Entidade existente; interno | Classificar estoque baixo |
| ativo | Entidade existente; interno | Material apto ao catálogo/pedido |
| statusEstoque | Calculado no backend | DISPONIVEL, BAIXO ou INDISPONIVEL |
| parametrosAceitos | Proposto | Lista de DIMENSAO e/ou DIAMETRO, podendo ser vazia |
| parametrosObrigatorios | Proposto | Subconjunto de parametrosAceitos |
| atualizadaEm | Requisito CT11/#1154 | Data persistida da última atualização do saldo |

Os dois metadados de parâmetros não existem hoje no DTO. A equipe deve informar
a política de cada material e a Lara definir sua representação no banco. Não
atribuir regras a MDF, balsa ou acrílico só pelo nome ou por uma expressão regular.
A espessura/medida fixa em especificacao não substitui a medida do pedido.

## Tela de nova solicitação → CT03 → CT12

| Campo da tela | JSON em CT03 | Valor interno/persistido em CT12 |
| --- | --- | --- |
| Material selecionado: id | materialId | materialId/material_id |
| Quantidade | quantidade | Inteiro positivo |
| Largura e altura, ambas em mm | dimensao | Texto canônico, exemplo 300 x 200 mm |
| Diâmetro em mm | diametro | Número decimal positivo, quando aplicável |
| Observação | observacao | Texto opcional; não descartar no caminho |
| Identidade da sessão/token | Não enviar usuarioId livre no corpo | usuarioId obtido da autenticação |
| Nenhum campo de status inicial | Não enviar status de criação | PENDENTE, definido pelo backend |
| Nenhum relógio do navegador | Não enviar criadaEm/atualizadaEm | Horários gerados no backend e gravados |

Regras propostas para adaptar o modal atual:

1. Manter os inputs largura/altura na interface; quando aplicáveis, ambos positivos
   e preenchidos em conjunto. Compor `dimensao` como `<largura> x <altura> mm`.
2. Se ambos estiverem vazios e o material permitir ausência de DIMENSAO, omitir
   o campo ou enviar null. Só uma medida preenchida é PARAMETROS_INVALIDOS.
3. Diâmetro enviado é número positivo em mm; omitir/enviar null quando inaplicável.
   Usar ponto no número JSON. Não acrescentar unidade no valor numérico.
4. Exigir parâmetros obrigatórios; recusar parâmetros não aceitos. Diâmetro e
   dimensão só podem coexistir se a política daquele material permitir.
5. Quantidade precisa ser inteira positiva. Falta de materialId é DADOS_INVALIDOS;
   quantidade ausente/inválida é QUANTIDADE_INVALIDA; medidas incompatíveis são
   PARAMETROS_INVALIDOS. Material inexistente/inativo é MATERIAL_NAO_ENCONTRADO.
6. Guardar dimensão, diâmetro e observação do pedido, sem sobrescrever a
   especificação do material. A criação retorna solicitacaoId e PENDENTE.
7. Não decidir baixa/reserva automática nem recusa por saldo insuficiente nesta
   revisão: alinhar essa política antes da implementação que depender dela.

Exemplo de CT03 com DIMENSAO aplicável; não é cadastro de regra de um material:

```json
{
  "materialId": 7,
  "quantidade": 2,
  "dimensao": "300 x 200 mm",
  "diametro": null,
  "observacao": "Uso em maquete"
}
```

A escolha de mm concilia os rótulos do modal atual. Substitui, como proposta,
o exemplo antigo em cm; não adicionar conversão silenciosa. Limites máximos,
precisão e comprimento dos textos ainda precisam de confirmação.

## Atualização de estoque

CT05 substitui o saldo; 10 com entrada 24 resulta em 24. Zero é válido. Recusar
ausência, null, fração, texto e negativo com QUANTIDADE_INVALIDA.

CT11 deve gravar quantidade e atualizadaEm na mesma operação. A resposta de CT05
usa o horário persistido, e não um LocalDateTime.now() exclusivo do DTO. Sem
gravação confirmada não retornar sucesso. O horário inicial de materiais legados
e a precisão/fuso precisam ser alinhados com a Lara; não inventar data histórica.
