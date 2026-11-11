# Pendências do ArchFlow

Revisão #1136 conduzida pelo Luis. Definição das seis colunas entregue no
[dicionário](especificacao/colunas-materiais-e-solicitacoes.md); Lara implementa
H01.2, sem ter de elaborar regras funcionais que faltavam na especificação.

Conferência de 11/11/2026, main bd733aa: usuários já estão no DDL pela PR #24;
saldo/data do estoque já foram integrados pela #23. A publicação dessas partes
não comprova carga de exemplos, migração no destino ou integração de telas.

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
| Banco de usuários e exemplos | DDL entregue na #24; H01.2/#1134 precisa conciliar carga com esquema, perfis, hashes BCrypt e IDs. [Divergências exatas](especificacao/requisitos-banco.md); falta comprovar migração no destino |
| Integração do login | PR #19 entrega login e identidade; #1141/Luis aproveita a PR #14 do frontend para provar aluno e responsável com banco e assinatura configurados |
| Metadados aceitos/obrigatórios no catálogo | Derivar dos booleanos no DTO; contrato alvo definido, ainda sem implementação |
| Formulário enviar pedido | #1147/Luis com H04.2/Mirele, após identidade e API/banco prontos |
| Data limite do formulário | Definir significado em recorte próprio; CT03 ainda recusa dataLimite |
| Leitura de notificações | #1161 prepara CT07 com consulta simulada; ligar ao esquema H01.2 e provar o isolamento antes de habilitar |
| Geração de notificações | H09.1 produz os avisos; a consulta não cria nem marca avisos como lidos |
| Validação da data de estoque | #1154/Luis: PR #23 integrada, releitura com JWT/PostgreSQL provada; verificar migração e integração no destino antes de habilitar a rota |
| Prova PostgreSQL do pedido | [PR #25](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/25) corrige os cinco testes antigos para JWT e acrescenta recusas; 95 testes aprovados, incluindo nove PostgreSQL, em ambiente isolado |
| Login e histórico na tela | PRs frontend #14/#15 abertas; #15 baseada na branch #14. Integrar login antes do histórico, mantendo os escopos de cada colega |
| Ambiente de apresentação | Conciliar URL absoluta da API com proxy Nginx/CORS, encaminhamento do segredo no Compose e imports de welcome com caixa diferente dos arquivos Git |

A PR #10 já integrou as definições e scripts; aplicação no banco compartilhado e
conclusão funcional dos cards não são consequências automáticas da documentação.

## Questões acadêmicas e de produto separadas

- Datas de S1–S4: documentos citam sprint 14/10–22/11, entrega 15/11 e cronograma
  em semanas/dezembro. Não inventar datas de sprint.
- Confirmar PO e a decisão sobre H12/aviso lido. H13/cadastro de materiais foi
  rejeitado na revisão do backlog; sua tela local não autoriza nova API de cadastro.
- Revisão do backlog, página 4: pedido maior que saldo, baixa automática,
  cancelamento e aviso lido exigem tratamento próprio com responsáveis/professor.
  Não são motivo para deixar as seis colunas sem especificação.
- A geração de aviso prevista por H09.1 ocorre quando o pedido muda para
  AGUARDANDO_COMPRA ou PRONTO_PARA_RETIRADA, não por qualquer ajuste de estoque.

## Fontes e documentação

Revisar junto das fichas/fluxos do frontend, cuja PR #6 já foi integrada na main
078f6dc. Os documentos foram recuperados de 51f9923 e seguem o ADR 0008.
Modal ainda sem envio HTTP; campos visuais não equivalem a contrato.
Fichas acadêmicas originais devem refletir decisões validadas, sem apagar a origem.
Nenhum card/tempo foi alterado no OpenProject nesta revisão.
