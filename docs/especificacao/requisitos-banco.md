# Requisitos para alinhar H01.2 — refs #1136

**Lista para revisão com a Lara; não é script SQL aprovado.** Derivada dos
contratos CT10–CT13, fontes existentes e [proposta de campos](materiais-e-pedidos.md).
A representação física dos parâmetros e os tipos/limites/fuso precisam de decisão.
O Java permanece em ddl-auto: validate.

| Conjunto | Dados esperados | Diferença identificada / alinhamento |
| --- | --- | --- |
| usuarios | usuarioId, nome, email, senhaHash, perfil, ativo; curso a alinhar no CT10 | Autenticação local depende desses dados; senha nunca em texto puro |
| materiais | materialId, nome, descricao, especificacao, quantidadeDisponivel, quantidadeMinima, ativo | Entidade já contém esses campos; não há SQL oficial na main |
| Política de parâmetros do material | Quais parâmetros aceita e exige | Proposta parametrosAceitos/parametrosObrigatorios; decidir colunas ou relação própria sem inferir política do texto |
| Horário do estoque | materiais.atualizadaEm, nome físico sugerido atualizada_em | Ainda não existe na entidade; gravar junto com o saldo; alinhar início/backfill, precisão e fuso |
| solicitacoes | solicitacaoId, usuarioId, materialId, quantidade, status, criadaEm, atualizadaEm, previsaoRetirada | Entidade parcial já contém; referências a usuário/material e estado PENDENTE na criação |
| Parâmetros do pedido | dimensao, diametro, observacao | dimensao/diametro faltam na entidade; observacao existe; CT03 e CT12 precisam preservar os três |
| notificacoes | notificacaoId, usuarioId, solicitacaoId opcional, tipo, mensagem, criadaEm, lida | Referência CT13; parte do detalhamento de notificações com a responsável H09.1 |
| sessoes, somente se token opaco for aprovado | Token, usuário associado e validade | Condicional ao ADR 0003; não criar por suposição se Keycloak for escolhido |

Regras que os scripts/modelos devem representar conforme a decisão validada:

- Quantidades de material não negativas; quantidade do pedido positiva; identificadores
  e relações consistentes. Campos opcionais não devem ganhar valores fictícios.
- Status do pedido coerentes entre Java, CT03/CT04/CT06/CT12 e SQL: PENDENTE,
  EM_SEPARACAO, AGUARDANDO_COMPRA, PRONTO_PARA_RETIRADA, CONCLUIDO e CANCELADO.
- Dimensão em texto na representação proposta; diâmetro numérico em mm. Não trocar
  por campos de largura/altura no JSON sem atualizar simultaneamente as fichas.
- Saldo e horário do estoque persistidos juntos. Horários de criação/alteração
  definidos pelo backend; definir fuso/tipo físico antes da carga inicial.
- Dados de demonstração suficientes para aluno/responsável, material ativo/inativo
  e pedidos; definir política de parâmetros com a equipe. Não cadastrar exemplos
  com senha em texto puro nem tratar dados inventados como dados reais.

## Validação quando o SQL chegar

1. Conferir os campos acordados e compatibilidade com as entidades/colunas.
2. Inicializar ambiente dedicado, preservando os dados já existentes. SQL novo não
   roda automaticamente em um volume previamente inicializado.
3. Verificar que ddl-auto: validate permite iniciar o backend.
4. Criar pedido e consultar de novo para conferir aluno, parâmetros, PENDENTE e data.
5. Atualizar saldo e consultar novamente para conferir quantidade e data persistidas.

O PostgreSQL vazio preparado no projeto archflow-preparo prova conexão, não esses
critérios. Não atribuir esta lista a uma entrega já realizada pela responsável.
