# Pendências do ArchFlow

Revisão #1136 conduzida pelo Luis. Definição das seis colunas entregue no
[dicionário](especificacao/colunas-materiais-e-solicitacoes.md); Lara implementa
H01.2, sem ter de elaborar regras funcionais que faltavam na especificação.

Conferência vigente de 15/11/2026: backend85d3296/frontendaf5c775. Login,
pedido e estoque pela tela integrados nas frontend24/25/26, com provas isoladas.
A PR28 corrigiu a carga de exemplos. Migração/habilitação do estoque no destino continuam pendentes; avisos integrados nas PRs37/38 e frontend28, com prova #1161. [Revisão documental](especificacao/revisao-integracoes-2026-11-14.md).

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
| Banco de usuários e exemplos | DDL na #24 e carga corrigida na #28; falta aplicação no destino e responsável de demonstração, sem reatribuir dados existentes |
| Integração do login | Concluída no #1141/frontend24, com dois perfis e banco isolado; preservar provas |
| Metadados aceitos/obrigatórios no catálogo | Derivar dos booleanos no DTO; contrato alvo definido, ainda sem implementação |
| Formulário enviar pedido | Concluído no #1147/frontend25: PENDENTE/aluno/data/opcionais comprovados |
| Data limite do formulário | Definir significado em recorte próprio; CT03 ainda recusa dataLimite |
| Leitura de notificações | #1161/PR22 conciliada com PR37: consulta JPA oficial, filtro estrito e prova HTTP/login/JWT/banco; sem flag antiga |
| Geração de notificações | H09.1/PR37 integrado; rollback em falha do aviso comprovado no #1161, sem marcar leitura |
| Validação da data de estoque | #1154/Luis: PR #23 integrada, releitura com JWT/PostgreSQL provada; verificar migração e integração no destino antes de habilitar a rota |
| Prova PostgreSQL do pedido | [PR #25](https://github.com/MatheusMansano06/pi4-0102-g7-backend-1/pull/25) corrige os cinco testes antigos para JWT e acrescenta recusas; 95 testes aprovados, incluindo nove PostgreSQL, em ambiente isolado |
| Login/histórico/prazos na tela | Integrações publicadas; E8/E9 usam CT08. CT09 separado é proposta, não chamada atual |
| Estoque na tela | Concluído no #1156/frontend26, com PATCH/GET e reflexo no catálogo em ambiente isolado; CT05 segue desligado por padrão |
| Ambiente de apresentação | Conciliar URL absoluta da API com proxy Nginx/CORS e configuração do segredo/CT05 no destino; imports welcome já corrigidos na frontend24 |

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
Modal já envia HTTP na frontend25; campos visuais não criam contrato de dataLimite.
Fichas acadêmicas originais devem refletir decisões validadas, sem apagar a origem.
Nenhum card/tempo foi alterado no OpenProject nesta revisão.
