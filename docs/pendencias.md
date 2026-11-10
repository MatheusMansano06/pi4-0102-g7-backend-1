# Pendências do ArchFlow

Revisão #1136 conduzida pelo Luis. Definição das seis colunas entregue no
[dicionário](especificacao/colunas-materiais-e-solicitacoes.md); Lara implementa
H01.2, sem ter de elaborar regras funcionais que faltavam na especificação.

## Definições já tomadas

- Login próprio da PR #19 é a decisão vigente; ADR 0008 substitui ADR 0007.
- Comunicação de desenvolvimento é chamada direta + CORS, integrada na PR #15.
- Luis confirmou dimensão/diâmetro permitidos e opcionais para todos os materiais.
- As quatro regras usam BOOLEAN, defaults TRUE/FALSE/TRUE/FALSE; dimensão do pedido
  VARCHAR(255), diâmetro DOUBLE PRECISION, ambos anuláveis. Observação já existe.
- CT03 retorna 201 e CT12 preserva os opcionais; não retirar medidas para acomodar
  entidades antigas. Código e seis colunas já entraram parcialmente na PR #14.
- Entrada H04.1 valida dimensão/observação até 255 por String.length(), com erro
  400 DADOS_INVALIDOS no excesso e sem corte/trim dos textos permitidos.

## Entrega física e integrações

| Pendência | Quem conduz / próximo trabalho |
| --- | --- |
| Banco existente receber a configuração/colunas | H01.2/Lara coordena execução do [script definido](sql/H04.1-colunas-propostas.sql), com cópia de segurança e validate |
| Banco de usuários | H01.2/#1134: disponibilizar a tabela usuarios do CT10; banco criado apenas pelos scripts atuais não atende ao validate |
| Integração do login | PR #19 entrega login e identidade; #1141/Luis aproveita a PR #14 do frontend para provar aluno e responsável com banco e assinatura configurados |
| Metadados aceitos/obrigatórios no catálogo | Derivar dos booleanos no DTO; contrato alvo definido, ainda sem implementação |
| Formulário enviar pedido | #1147/Luis com H04.2/Mirele, após identidade e API/banco prontos |
| Data limite do formulário | Definir significado em recorte próprio; CT03 ainda recusa dataLimite |
| Estoque gravar a data com o saldo | #1154/Luis; rota da PR #8 segue desativada por padrão |
| Leitura de notificações | #1161 prepara CT07 com consulta simulada; ligar ao esquema H01.2 e provar o isolamento antes de habilitar |
| Geração de notificações | H09.1 produz os avisos; a consulta não cria nem marca avisos como lidos |

A PR #10 prepara as definições e scripts; aplicação no banco compartilhado e
conclusão funcional dos cards não são consequências automáticas da documentação.

## Questões acadêmicas e de produto separadas

- Datas de S1–S4: documentos citam sprint 14/10–22/11, entrega 15/11 e cronograma
  em semanas/dezembro. Não inventar datas de sprint.
- Confirmar PO e entrada de H12/H13 conforme o backlog; não retomar cadastro de
  materiais rejeitado no escopo sem decisão explícita.
- Revisão do backlog, página 4: pedido maior que saldo, baixa automática,
  cancelamento e aviso lido exigem tratamento próprio com responsáveis/professor.
  Não são motivo para deixar as seis colunas sem especificação.

## Fontes e documentação

Revisar esta PR com as fichas/fluxos E2/E3 da PR frontend #6. Esses documentos
foram recuperados de 51f9923 e atualizados para a consulta real do catálogo na
main ea8620f. Modal ainda sem envio HTTP; campos visuais não equivalem a contrato.
Fichas acadêmicas originais devem refletir decisões validadas, sem apagar a origem.
Nenhum card/tempo foi alterado no OpenProject nesta revisão.
