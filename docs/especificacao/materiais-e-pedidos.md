# Materiais parâmetros e pedidos

Definição conduzida pelo Luis no #1136. **Dimensão e diâmetro permitidos e
opcionais para todos os materiais do MVP**, confirmados em 04/11/2026.
O [dicionário dos campos](colunas-materiais-e-solicitacoes.md) fixa nomes,
tipos, nulabilidade, defaults, configuração e limites técnicos.

## Material e regras

Material mantém materialId, nome, descricao, especificacao, quantidadeDisponivel,
quantidadeMinima e ativo; statusEstoque é calculado do saldo/mínimo.
As regras são aceitaDimensao=TRUE, exigeDimensao=FALSE, aceitaDiametro=TRUE e
exigeDiametro=FALSE. CT11 as lê; não são inferidas de especificacao.
CT02 deverá projetá-las em parametrosAceitos/parametrosObrigatorios sem outras
colunas. Essa projeção ainda não existe no DTO atual. Material inativo continua
impedindo pedidos mesmo com a política preenchida.

## Tela e persistência

| Origem | Campo CT03 | CT12 / banco |
| --- | --- | --- |
| Material selecionado | materialId inteiro positivo | material_id / relação existente |
| Quantidade obrigatória | quantidade inteira positiva | quantidade INTEGER |
| Largura/altura opcionais, ambas positivas quando informadas | dimensao como 300 x 200 mm; null se ambas vazias | dimensao VARCHAR(255), anulável |
| Diâmetro opcional em mm | diametro numérico positivo/finito | diametro DOUBLE PRECISION, anulável |
| Observação opcional | observacao textual | observacao VARCHAR(255), já existente e anulável |
| Identidade validada | não enviar usuarioId livre | usuario_id resolvido no servidor |
| Sem status de entrada | não enviar status | PENDENTE do servidor |
| Relógio do servidor | não enviar criadaEm/atualizadaEm | horários inicialmente iguais |

Pode enviar apenas materialId e quantidade, ou acrescentar uma/as duas medidas.
O modal compõe largura/altura em conjunto; API preserva texto sem analisar esses
números. HTTP 201 devolve medidas/observação salvas. Textos têm capacidade de
255 pela contagem Java String.length(); H04.1 antecipa esse limite com
400 DADOS_INVALIDOS antes de gravar, preservando textos permitidos sem corte/trim.
Não prometer máximos de fabricação ou precisão decimal exata sem requisito.

dataLimite continua fora do contrato e não é previsaoRetirada. Não decidir
baixa/reserva ou recusa por saldo insuficiente nesta revisão de campos.

## Estoque

CT05 substitui o saldo; zero é válido. CT11 deve gravar saldo e atualizadaEm
juntos. PR #8 já integrada mantém rota desativada por padrão e data calculada
só no DTO. A data persistida do estoque é recorte #1154, não uma das seis colunas.
Não inventar data histórica para materiais legados.
